package com.sangsang.config;

import com.sangsang.interceptor.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 脱敏的注册配置
 *
 * @author liutangqi
 * @date 2025/5/26 13:45
 */
@Configuration
public class DesensitizeConfig {
    /**
     * 注册开启脱敏功能的拦截器
     * 下面注入的bean主要是控制拦截器注册的顺序，所以看到下面的bean都没有使用
     *
     * @author liutangqi
     * @date 2025/4/8 10:48
     * @Param [poJoResultEncrtptorInterceptor, dbFieldEncryptorInterceptor]
     **/
    @Bean
    @ConditionalOnProperty(name = "field.desensitize.enable", havingValue = "true")
    public FieldDesensitizeInterceptor fieldDesensitizeInterceptor() {
        return new FieldDesensitizeInterceptor();
    }

}
