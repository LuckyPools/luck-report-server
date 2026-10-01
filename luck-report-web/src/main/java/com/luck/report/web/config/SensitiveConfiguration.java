package com.luck.report.web.config;

import com.luck.report.core.security.SensitiveConfigCipher;
import com.luck.report.web.config.properties.SensitiveConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 启动时初始化 {@link SensitiveConfigCipher}（兼容 Boot2/Boot3，避免 javax/jakarta PostConstruct 差异）。
 */
@Configuration
public class SensitiveConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SensitiveConfiguration.class);

    /**
     * 唯一注册 {@code bean.sensitiveConfigProperties}。
     */
    @Bean(name = "bean.sensitiveConfigProperties")
    @ConfigurationProperties(prefix = "luck.report.security.sensitive-config")
    public SensitiveConfigProperties sensitiveConfigProperties() {
        return new SensitiveConfigProperties();
    }

    /**
     * 属性绑定完成后初始化加解密密钥。
     */
    @Bean(name = "bean.sensitiveConfigCipherInitializer")
    public InitializingBean sensitiveConfigCipherInitializer(
            @Qualifier("bean.sensitiveConfigProperties") SensitiveConfigProperties properties) {
        return () -> {
            byte[] primary = SensitiveConfigCipher.decodeKeyBase64(properties.getSecretKeyBase64());
            byte[] previous = SensitiveConfigCipher.decodeKeyBase64(properties.getPreviousSecretKeyBase64());
            SensitiveConfigCipher.init(properties.isEnabled(), primary, previous);
            if (primary == null) {
                log.info("SensitiveConfigCipher initialized: enabled={}, key=built-in-default",
                        properties.isEnabled());
            } else {
                log.info("SensitiveConfigCipher initialized: enabled={}, key=configured",
                        properties.isEnabled());
            }
        };
    }
}
