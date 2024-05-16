package com.sangsang.cache;

import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.encryptor.DefaultFieldEncryptorPattern;
import com.sangsang.encryptor.EncryptorProperties;
import com.sangsang.encryptor.FieldEncryptorPattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 缓存当前加解密算法的bean
 *
 * @author liutangqi
 * @date 2024/4/8 14:34
 */
@Configuration
public class FieldEncryptorPatternCache {

    @Autowired
    private ApplicationContext applicationContext;

    //缓存当前加解密的bean
    private static FieldEncryptorPattern fieldEncryptorPattern;

    @PostConstruct
    public void init() {
        if (fieldEncryptorPattern == null) {
            fieldEncryptorPattern = applicationContext.getBean(FieldEncryptorPattern.class);
        }
    }

    /**
     * 初始化默认的加解密函数，仅用于测试
     *
     * @author liutangqi
     * @date 2024/4/8 15:04
     * @Param []
     **/
    public static void initDeafultInstance() {
        if (fieldEncryptorPattern == null) {
            EncryptorProperties encryptorProperties = new EncryptorProperties();
            encryptorProperties.setSecretKey(SymbolConstant.DEFAULT_SECRET_KEY);
            fieldEncryptorPattern = new DefaultFieldEncryptorPattern(encryptorProperties);
        }
    }

    /**
     * 获取当前的加解密的bean
     *
     * @author liutangqi
     * @date 2024/4/8 14:37
     * @Param []
     **/
    public static FieldEncryptorPattern getInstance() {
        return fieldEncryptorPattern;
    }

}
