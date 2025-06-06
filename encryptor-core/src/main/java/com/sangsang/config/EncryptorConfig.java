package com.sangsang.config;

import com.sangsang.cache.FieldEncryptorPatternCache;
import com.sangsang.config.properties.FieldProperties;
import com.sangsang.domain.constants.EncryptorPatternTypeConstant;
import com.sangsang.encryptor.db.DBFieldEncryptorPattern;
import com.sangsang.encryptor.db.DefaultDBFieldEncryptorPattern;
import com.sangsang.encryptor.pojo.DefaultPoJoFieldEncryptorPattern;
import com.sangsang.encryptor.pojo.PoJoFieldEncryptorPattern;
import com.sangsang.interceptor.DBFieldEncryptorInterceptor;
import com.sangsang.interceptor.PoJoParamEncrtptorInterceptor;
import com.sangsang.interceptor.PoJoResultEncrtptorInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 加解密的配置
 *
 * @author liutangqi
 * @date 2025/5/26 13:47
 */
@Configuration
public class EncryptorConfig {

    /**
     * 注册pojo模式入参加解密的拦截器
     *
     * @author liutangqi
     * @date 2024/9/10 14:49
     * @Param [sqlSessionFactory]
     **/
    @Bean
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = EncryptorPatternTypeConstant.POJO)
    public PoJoParamEncrtptorInterceptor pojoParamInterceptor() {
        return new PoJoParamEncrtptorInterceptor();
    }

    /**
     * 注册pojo模式响应加解密的拦截器
     *
     * @author liutangqi
     * @date 2024/9/10 14:49
     * @Param [sqlSessionFactory]
     **/
    @Bean
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = EncryptorPatternTypeConstant.POJO)
    public PoJoResultEncrtptorInterceptor pojoResultInterceptor() {
        return new PoJoResultEncrtptorInterceptor();
    }

    /**
     * 注册db模式加解密的拦截器
     *
     * @author liutangqi
     * @date 2024/9/10 14:49
     * @Param [sqlSessionFactory]
     **/
    @Bean
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = EncryptorPatternTypeConstant.DB)
    public DBFieldEncryptorInterceptor dbInterceptor() {
        return new DBFieldEncryptorInterceptor();
    }


    /**
     * 默认的pojo加解密算法
     *
     * @author liutangqi
     * @date 2024/9/19 13:40
     * @Param []
     **/
    @Bean
    @ConditionalOnMissingBean(PoJoFieldEncryptorPattern.class)
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = EncryptorPatternTypeConstant.POJO)
    public PoJoFieldEncryptorPattern defaultPoJoFieldEncryptorPattern(FieldProperties fieldProperties) {
        return new DefaultPoJoFieldEncryptorPattern(fieldProperties);
    }


    /**
     * 默认的db模式下的加解密算法
     *
     * @author liutangqi
     * @date 2024/9/19 13:45
     * @Param [encryptorProperties]
     **/
    @Bean
    @ConditionalOnMissingBean(DBFieldEncryptorPattern.class)
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = EncryptorPatternTypeConstant.DB)
    public DBFieldEncryptorPattern defaultDBFieldEncryptorPattern(FieldProperties fieldProperties) {
        return new DefaultDBFieldEncryptorPattern(fieldProperties);
    }

    /**
     * pojo模式下缓存当前项目配置的加密算法
     *
     * @author liutangqi
     * @date 2024/9/19 14:34
     **/
    @Bean
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = EncryptorPatternTypeConstant.POJO)
    public FieldEncryptorPatternCache pojoFieldEncryptorPatternCache(List<PoJoFieldEncryptorPattern> poJoFieldEncryptorPatternLists) {
        FieldEncryptorPatternCache fieldEncryptorPatternCache = new FieldEncryptorPatternCache(poJoFieldEncryptorPatternLists);
        fieldEncryptorPatternCache.init();
        return fieldEncryptorPatternCache;
    }


    /**
     * db模式下缓存当前项目配置的加密算法
     *
     * @author liutangqi
     * @date 2024/9/19 14:34
     **/
    @Bean
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = EncryptorPatternTypeConstant.DB)
    public FieldEncryptorPatternCache dbFieldEncryptorPatternCache(DBFieldEncryptorPattern dbFieldEncryptorPattern) {
        FieldEncryptorPatternCache fieldEncryptorPatternCache = new FieldEncryptorPatternCache(dbFieldEncryptorPattern);
        fieldEncryptorPatternCache.init();
        return fieldEncryptorPatternCache;
    }


}
