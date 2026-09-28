package com.luck.report.core.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 敏感字段加解密：数据源密码与模型 API Key 共用。
 */
class SensitiveConfigCipherTest {

    @Test
    void encryptDecrypt_roundTripForApiKey() {
        String plain = "sk-test-api-key-12345";
        String encrypted = SensitiveConfigCipher.encrypt(plain);

        assertTrue(SensitiveConfigCipher.isEncrypted(encrypted));
        assertTrue(encrypted.startsWith(SensitiveConfigCipher.MAGIC_PREFIX));
        assertFalse(encrypted.contains(plain));
        assertEquals(plain, SensitiveConfigCipher.decrypt(encrypted));
    }

    @Test
    void decrypt_plainLegacyValue_returnsAsIs() {
        String legacy = "sk-legacy-plain-key";
        assertEquals(legacy, SensitiveConfigCipher.decrypt(legacy));
        assertFalse(SensitiveConfigCipher.isEncrypted(legacy));
    }

    @Test
    void encrypt_alreadyEncrypted_idempotent() {
        String once = SensitiveConfigCipher.encrypt("sk-once");
        assertEquals(once, SensitiveConfigCipher.encrypt(once));
    }
}
