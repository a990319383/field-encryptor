package com.sangsang.interceptor;

import com.sangsang.cache.FieldEncryptorPatternCache;
import com.sangsang.cache.TableCache;
import com.sangsang.domain.constants.PatternTypeConstant;
import com.sangsang.encryptor.EncryptorProperties;
import com.sangsang.encryptor.db.DBFieldEncryptorPattern;
import com.sangsang.encryptor.db.DefaultDBFieldEncryptorPattern;
import com.sangsang.encryptor.pojo.DefaultPoJoFieldEncryptorPattern;
import com.sangsang.encryptor.pojo.PoJoFieldEncryptorPattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 注册bean
 *
 * @author liutangqi
 * @date 2024/9/10 15:01
 */
@Configuration
@EnableConfigurationProperties({EncryptorProperties.class})
public class RegisterConfig {
    /**
     * 初始化表结构字段信息到本地缓存
     *
     * @author liutangqi
     * @date 2024/9/19 15:22
     * @Param [encryptorProperties]
     **/
    @Bean
    public TableCache initTableCache(EncryptorProperties encryptorProperties) {
        TableCache tableCache = new TableCache(encryptorProperties);
        tableCache.init();
        return tableCache;
    }

    /**
     * 注册pojo模式入参加解密的拦截器
     *
     * @author liutangqi
     * @date 2024/9/10 14:49
     * @Param [sqlSessionFactory]
     **/
    @Bean
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = PatternTypeConstant.POJO)
    public PoJoParamEncrtptorInterceptor pojoParamInterceptor(EncryptorProperties encryptorProperties) {
        return new PoJoParamEncrtptorInterceptor(encryptorProperties);
    }

    /**
     * 注册pojo模式响应加解密的拦截器
     *
     * @author liutangqi
     * @date 2024/9/10 14:49
     * @Param [sqlSessionFactory]
     **/
    @Bean
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = PatternTypeConstant.POJO)
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
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = PatternTypeConstant.DB)
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
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = PatternTypeConstant.POJO)
    public PoJoFieldEncryptorPattern defaultPoJoFieldEncryptorPattern(EncryptorProperties encryptorProperties) {
        return new DefaultPoJoFieldEncryptorPattern(encryptorProperties);
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
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = PatternTypeConstant.DB)
    public DBFieldEncryptorPattern defaultDBFieldEncryptorPattern(EncryptorProperties encryptorProperties) {
        return new DefaultDBFieldEncryptorPattern(encryptorProperties);
    }

    /**
     * pojo模式下缓存当前项目配置的加密算法
     *
     * @author liutangqi
     * @date 2024/9/19 14:34
     **/
    @Bean
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = PatternTypeConstant.POJO)
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
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = PatternTypeConstant.DB)
    public FieldEncryptorPatternCache dbFieldEncryptorPatternCache(DBFieldEncryptorPattern dbFieldEncryptorPattern) {
        FieldEncryptorPatternCache fieldEncryptorPatternCache = new FieldEncryptorPatternCache(dbFieldEncryptorPattern);
        fieldEncryptorPatternCache.init();
        return fieldEncryptorPatternCache;
    }


    /**
     * 注册开启脱敏功能的拦截器
     * 注意：这里两个入参是为了使者两个bean加载完毕后才加载这个bean,使这个拦截器晚于这两个注册，所以看到这两个入参的bean并没有被使用
     *
     * @author liutangqi
     * @date 2025/4/8 10:48
     * @Param [poJoResultEncrtptorInterceptor, dbFieldEncryptorInterceptor]
     **/
    @Bean
    @ConditionalOnProperty(name = "field.encryptor.fieldDesensitize", havingValue = "true")
    public FieldDesensitizeInterceptor fieldDesensitizeInterceptor(@Autowired(required = false) PoJoResultEncrtptorInterceptor poJoResultEncrtptorInterceptor,
                                                                   @Autowired(required = false) DBFieldEncryptorInterceptor dbFieldEncryptorInterceptor) {
        return new FieldDesensitizeInterceptor();
    }

}
