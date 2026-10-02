package com.luck.report.core.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 敏感配置落库加解密（AES-128-GCM）。用于数据源密码、模型 API Key 等需可逆解密的字段。密文格式：{@code __LUCK__} + Base64URL(nonce[12] ‖ ciphertext ‖ tag[16])。无魔数前缀视为历史明文，原样返回。选用 AES-128 而非 256：项目目标 JDK 8，避免部分环境未开启 JCE unlimited 时出现{@code Illegal key size}。
 *
 * @author luck
 */
public final class SensitiveConfigCipher {

    /**
     * 密文魔数前缀
     */
    public static final String MAGIC_PREFIX = "__LUCK__";

    /**
     * 内置默认种子（勿改，否则无法解密已有密文）
     */
    private static final String DEFAULT_SEED = "LuckReport@DatasourcePassword#Key-v1";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_BITS = 128;
    /**
     * AES-128 密钥长度
     */
    private static final int KEY_LENGTH = 16;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static volatile boolean enabled = true;
    private static volatile byte[] primaryKey = deriveDefaultKey();
    private static volatile byte[] previousKey;

    private SensitiveConfigCipher() {
    }

    /**
     * 初始化开关与密钥。
     *
     * @param encryptEnabled 写入是否加密；null 当作 true
     * @param primary        主密钥 16 字节；null/空则用内置默认钥
     * @param previous       轮换用旧密钥；可为 null
     */
    public static synchronized void init(Boolean encryptEnabled, byte[] primary, byte[] previous) {
        enabled = encryptEnabled == null || encryptEnabled;
        if (primary == null || primary.length == 0) {
            primaryKey = deriveDefaultKey();
        } else {
            requireKeyLength(primary, "primary");
            primaryKey = Arrays.copyOf(primary, primary.length);
        }
        if (previous == null || previous.length == 0) {
            previousKey = null;
        } else {
            requireKeyLength(previous, "previous");
            previousKey = Arrays.copyOf(previous, previous.length);
        }
    }

    /**
     * 由 Base64 配置值解析密钥；空串返回 null（表示走默认钥）。支持 16 字节（AES-128）或 32 字节（截取前 16 字节，便于误配 256 位密钥时仍可用）。
     *
     * @param base64 Base64 编码的密钥
     * @return 16 字节密钥；blank 时 null
     * @throws IllegalArgumentException 非空但解码后长度非法
     */
    public static byte[] decodeKeyBase64(String base64) {
        if (base64 == null) {
            return null;
        }
        String trimmed = base64.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        byte[] key;
        try {
            key = Base64.getDecoder().decode(trimmed);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("sensitive-config secret-key-base64 is not valid Base64", e);
        }
        if (key.length == 32) {
            return Arrays.copyOf(key, KEY_LENGTH);
        }
        requireKeyLength(key, "secret-key-base64");
        return key;
    }

    /**
     * 内置默认密钥：SHA-256(种子) 的前 16 字节
     */
    public static byte[] deriveDefaultKey() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] full = digest.digest(DEFAULT_SEED.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(full, KEY_LENGTH);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(MAGIC_PREFIX);
    }

    /**
     * 加密明文。已是密文则原样返回；enabled=false 时返回明文。
     *
     * @param plain 明文
     * @return 密文或原文
     */
    public static String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) {
            return plain;
        }
        if (isEncrypted(plain)) {
            return plain;
        }
        if (!enabled) {
            return plain;
        }
        try {
            byte[] nonce = new byte[NONCE_LENGTH];
            SECURE_RANDOM.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(primaryKey, "AES"),
                    new GCMParameterSpec(TAG_BITS, nonce));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] packed = new byte[nonce.length + cipherText.length];
            System.arraycopy(nonce, 0, packed, 0, nonce.length);
            System.arraycopy(cipherText, 0, packed, nonce.length, cipherText.length);
            return MAGIC_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(packed);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt sensitive config", e);
        }
    }

    /**
     * 解密：无魔数当明文；有魔数则用主钥解密，失败再试旧钥。
     *
     * @param stored 库中存值
     * @return 明文
     */
    public static String decrypt(String stored) {
        if (stored == null || stored.isEmpty()) {
            return stored;
        }
        if (!isEncrypted(stored)) {
            return stored;
        }
        String payload = stored.substring(MAGIC_PREFIX.length());
        byte[] packed;
        try {
            packed = Base64.getUrlDecoder().decode(payload);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("敏感配置解密失败：密文格式无效", e);
        }
        if (packed.length <= NONCE_LENGTH) {
            throw new IllegalStateException("敏感配置解密失败：密文过短");
        }
        byte[] nonce = Arrays.copyOfRange(packed, 0, NONCE_LENGTH);
        byte[] cipherText = Arrays.copyOfRange(packed, NONCE_LENGTH, packed.length);

        GeneralSecurityException primaryFailure = null;
        try {
            return doDecrypt(nonce, cipherText, primaryKey);
        } catch (GeneralSecurityException e) {
            primaryFailure = e;
        }
        if (previousKey != null) {
            try {
                return doDecrypt(nonce, cipherText, previousKey);
            } catch (GeneralSecurityException ignored) {
            }
        }
        throw new IllegalStateException("敏感配置解密失败", primaryFailure);
    }

    private static String doDecrypt(byte[] nonce, byte[] cipherText, byte[] key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                new GCMParameterSpec(TAG_BITS, nonce));
        byte[] plain = cipher.doFinal(cipherText);
        return new String(plain, StandardCharsets.UTF_8);
    }

    private static void requireKeyLength(byte[] key, String name) {
        if (key.length != KEY_LENGTH) {
            throw new IllegalArgumentException(
                    "sensitive-config " + name + " key must be " + KEY_LENGTH
                            + " bytes (or 32 bytes Base64 which will be truncated), got " + key.length);
        }
    }
}
