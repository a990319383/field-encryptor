package com.sangsang.cache;

import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.encryptor.bean.BeanFieldEncryptorPattern;
import com.sangsang.encryptor.db.DefaultFieldEncryptorPattern;
import com.sangsang.encryptor.EncryptorProperties;
import com.sangsang.encryptor.db.FieldEncryptorPattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 缓存当前加解密算法的bean
 * 优先加载这个bean，避免有些@PostConstruct 加载数据库东西到redis时，此时还没处理完，导致redis中存储了密文
 *
 * @author liutangqi
 * @date 2024/4/8 14:34
 */
@Configuration
public class FieldEncryptorPatternCache implements BeanPostProcessor {

    @Autowired
    private ApplicationContext applicationContext;

    //缓存当前加解密的bean
    private static FieldEncryptorPattern fieldEncryptorPattern;

    //缓存当前java bean加解密模式下的bean
    private static BeanFieldEncryptorPattern beanFieldEncryptorPattern;

    @PostConstruct
    public void init() {
        if (fieldEncryptorPattern == null) {
            fieldEncryptorPattern = applicationContext.getBean(FieldEncryptorPattern.class);
        }
        if (beanFieldEncryptorPattern == null) {
            beanFieldEncryptorPattern = applicationContext.getBean(BeanFieldEncryptorPattern.class);
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
     * 获取数据库加解密模式下实现类
     *
     * @author liutangqi
     * @date 2024/4/8 14:37
     * @Param []
     **/
    public static FieldEncryptorPattern getInstance() {
        return fieldEncryptorPattern;
    }


    /**
     * 获取java bean 加解密模式下的实现类
     *
     * @author liutangqi
     * @date 2024/7/24 17:46
     * @Param []
     **/
    public static BeanFieldEncryptorPattern getBeanInstance() {
        return beanFieldEncryptorPattern;
    }
}
