package com.sangsang.cache;

import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.encryptor.pojo.PoJoFieldEncryptorPattern;
import com.sangsang.encryptor.db.DefaultDBFieldEncryptorPattern;
import com.sangsang.encryptor.EncryptorProperties;
import com.sangsang.encryptor.db.DBFieldEncryptorPattern;
import org.springframework.beans.BeansException;
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
    private static DBFieldEncryptorPattern DBFieldEncryptorPattern;

    //缓存当前java pojo加解密模式下的bean
    private static PoJoFieldEncryptorPattern pojoFieldEncryptorPattern;

    @PostConstruct
    public void init() {
        if (DBFieldEncryptorPattern == null) {
            DBFieldEncryptorPattern = applicationContext.getBean(DBFieldEncryptorPattern.class);
        }
        if (pojoFieldEncryptorPattern == null) {
            pojoFieldEncryptorPattern = applicationContext.getBean(PoJoFieldEncryptorPattern.class);
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
        if (DBFieldEncryptorPattern == null) {
            EncryptorProperties encryptorProperties = new EncryptorProperties();
            encryptorProperties.setSecretKey(SymbolConstant.DEFAULT_SECRET_KEY);
            DBFieldEncryptorPattern = new DefaultDBFieldEncryptorPattern(encryptorProperties);
        }
    }

    /**
     * 获取数据库加解密模式下实现类
     *
     * @author liutangqi
     * @date 2024/4/8 14:37
     * @Param []
     **/
    public static DBFieldEncryptorPattern getInstance() {
        return DBFieldEncryptorPattern;
    }


    /**
     * 获取java pojo 加解密模式下的实现类
     *
     * @author liutangqi
     * @date 2024/7/24 17:46
     * @Param []
     **/
    public static PoJoFieldEncryptorPattern getBeanInstance() {
        return pojoFieldEncryptorPattern;
    }

    /**
     * 实现父类default方法，避免低版本不兼容，找不到实现类
     *
     * @author liutangqi
     * @date 2024/9/10 11:36
     * @Param [bean, beanName]
     **/
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 实现父类default方法，避免低版本不兼容，找不到实现类
     *
     * @author liutangqi
     * @date 2024/9/10 11:36
     * @Param [bean, beanName]
     **/
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
