package com.sangsang.config;

import com.sangsang.aop.isolation.IsolationAspect;
import com.sangsang.cache.IsolationCache;
import com.sangsang.config.properties.FieldProperties;
import com.sangsang.interceptor.IsolationInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liutangqi
 * @date 2025/6/13 13:16
 */
@Configuration
public class IsolationConfig {

    @Bean
    @ConditionalOnProperty(name = "field.isolation.field")
    public IsolationCache initIsolationCache(FieldProperties fieldProperties) {
        IsolationCache isolationCache = new IsolationCache(fieldProperties);
        isolationCache.init();
        return isolationCache;
    }

    @Bean
    @ConditionalOnBean(IsolationCache.class)
    public IsolationInterceptor isolationInterceptor() {
        return new IsolationInterceptor();
    }

    @Bean
    @ConditionalOnBean(IsolationCache.class)
    public IsolationAspect isolationAspect() {
        return new IsolationAspect();
    }
}
