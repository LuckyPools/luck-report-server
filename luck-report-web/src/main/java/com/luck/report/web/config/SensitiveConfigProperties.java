package com.luck.report.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 敏感配置落库加密（数据源密码、模型 API Key 等）。
 * <p>前缀：{@code luck.report.security.sensitive-config}
 * <p>整段可省略：默认开启加密，密钥走内置 luck 派生。
 */
@ConfigurationProperties(prefix = "luck.report.security.sensitive-config")
public class SensitiveConfigProperties {

    /**
     * 写入是否加密。默认 true。
     */
    private boolean enabled = true;

    /**
     * AES 密钥的 Base64；空则使用内置默认钥。
     */
    private String secretKeyBase64;

    /**
     * 轮换用旧密钥 Base64；可空。
     */
    private String previousSecretKeyBase64;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSecretKeyBase64() {
        return secretKeyBase64;
    }

    public void setSecretKeyBase64(String secretKeyBase64) {
        this.secretKeyBase64 = secretKeyBase64;
    }

    public String getPreviousSecretKeyBase64() {
        return previousSecretKeyBase64;
    }

    public void setPreviousSecretKeyBase64(String previousSecretKeyBase64) {
        this.previousSecretKeyBase64 = previousSecretKeyBase64;
    }
}
