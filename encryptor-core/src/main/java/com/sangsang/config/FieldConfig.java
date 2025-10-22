package com.sangsang.config;

import com.sangsang.cache.SqlParseCache;
import com.sangsang.cache.fieldparse.TableCache;
import com.sangsang.config.properties.FieldProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

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
    public TableCache initTableCache(@Autowired(required = false) List<DataSource> dataSources,
                                     FieldProperties fieldProperties) {
        //1.初始化表结构字段信息到本地缓存
        TableCache.init(dataSources, fieldProperties);

        //2.初始化jsqlparser解析缓存
        SqlParseCache.init(fieldProperties);
        return new TableCache();
    }
}
