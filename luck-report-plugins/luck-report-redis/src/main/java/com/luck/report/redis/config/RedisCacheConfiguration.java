package com.luck.report.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.luck.report.infra.modules.cache.service.ReportCache;
import com.luck.report.redis.cache.RedisCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 缓存配置
 *
 * @author luckyPools
 * @since 2017年3月8日
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
@AutoConfigureAfter(name = {
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",    // Boot 2/3
        "org.springframework.boot.data.redis.autoconfigure.RedisAutoConfiguration"     // Boot 4
})
public class RedisCacheConfiguration {

    /**
     * 配置 RedisTemplate Bean
     *
     * @param connectionFactory Redis 连接工厂，由 Spring Boot RedisAutoConfiguration 自动注入，不能为空
     * @return 配置好的 RedisTemplate 实例
     */
    @Bean("bean.remoteRedisTemplate")
    @ConditionalOnMissingBean(name = "bean.remoteRedisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置 RedisCache Bean
     *
     * @param redisTemplate       Redis 操作模板，通过 @Qualifier 指定使用 bean.remoteRedisTemplate，不能为空
     * @param cacheExpireSeconds  默认过期时间（秒），对应 luck-report.cacheExpireSeconds
     * @return RedisCache 实例
     */
    @Bean("bean.redisCache")
    @ConditionalOnMissingBean(name = "bean.redisCache")
    public ReportCache redisCache(
            @Qualifier("bean.remoteRedisTemplate") RedisTemplate<String, Object> redisTemplate,
            @Value("${luck-report.cacheExpireSeconds:900}") long cacheExpireSeconds) {
        RedisCache cache = new RedisCache(redisTemplate);
        cache.setDefaultExpireSeconds(cacheExpireSeconds);
        return cache;
    }
}
