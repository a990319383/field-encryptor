package com.sangsang.config;

import com.sangsang.cache.TableCache;
import com.sangsang.config.properties.FieldProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 核心配置类
 *
 * @author liutangqi
 * @date 2025/5/26 13:55
 */
@Configuration
@EnableConfigurationProperties({FieldProperties.class})
public class FieldConfig {
    /**
     * 初始化表结构字段信息到本地缓存
     *
     * @author liutangqi
     * @date 2024/9/19 15:22
     * @Param [encryptorProperties]
     **/
    @Bean
    public TableCache initTableCache(FieldProperties fieldProperties) {
        TableCache tableCache = new TableCache(fieldProperties);
        tableCache.init();
        return tableCache;
    }
}
