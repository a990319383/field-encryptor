package com.sangsang.interceptor;

import com.sangsang.domain.constants.PatternTypeConstant;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 注册拦截器
 *
 * @author liutangqi
 * @date 2024/9/10 15:01
 */
@Configuration
public class RegisterConfig {


    /**
     * 注册pojo模式加解密的拦截器
     *
     * @author liutangqi
     * @date 2024/9/10 14:49
     * @Param [sqlSessionFactory]
     **/
    @Bean
    @ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = PatternTypeConstant.POJO)
    public String pojoInterceptor(SqlSessionFactory sqlSessionFactory) {
        sqlSessionFactory.getConfiguration().addInterceptor(new PoJoParamEncrtptorInterceptor());
        sqlSessionFactory.getConfiguration().addInterceptor(new PoJoResultEncrtptorInterceptor());
        return "interceptor";
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
    public String dbInterceptor(SqlSessionFactory sqlSessionFactory) {
        sqlSessionFactory.getConfiguration().addInterceptor(new DBFieldEncryptorInterceptor());
        return "interceptor";
    }
}
