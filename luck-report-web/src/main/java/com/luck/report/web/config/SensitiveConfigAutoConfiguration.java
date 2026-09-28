package com.luck.report.web.config;

import com.luck.report.core.security.SensitiveConfigCipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 启动时初始化 {@link SensitiveConfigCipher}（兼容 Boot2/Boot3，避免 javax/jakarta PostConstruct 差异）。
 */
@Configuration
@EnableConfigurationProperties(SensitiveConfigProperties.class)
public class SensitiveConfigAutoConfiguration implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(SensitiveConfigAutoConfiguration.class);

    private final SensitiveConfigProperties properties;

    public SensitiveConfigAutoConfiguration(SensitiveConfigProperties properties) {
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() {
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
    }
}
