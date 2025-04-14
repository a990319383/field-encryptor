package com.sangsang.cache;

import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.domain.enums.PoJoAlgorithmEnum;
import com.sangsang.domain.exception.FieldEncryptorException;
import com.sangsang.encryptor.EncryptorProperties;
import com.sangsang.encryptor.db.DBFieldEncryptorPattern;
import com.sangsang.encryptor.db.DefaultDBFieldEncryptorPattern;
import com.sangsang.encryptor.pojo.PoJoFieldEncryptorPattern;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 缓存当前加解密算法的bean
 * 优先加载这个bean，避免有些@PostConstruct 加载数据库东西到redis时，此时还没处理完，导致redis中存储了密文
 * 目前db模式下，加解密算法只能支持一种，pojo模式下，可以同时支持多种加解密算法
 *
 * @author liutangqi
 * @date 2024/4/8 14:34
 */
public class FieldEncryptorPatternCache implements BeanPostProcessor {

    private DBFieldEncryptorPattern dbFieldEncryptorPattern;
    private List<PoJoFieldEncryptorPattern> poJoFieldEncryptorPatternList;

    public FieldEncryptorPatternCache(DBFieldEncryptorPattern dbFieldEncryptorPattern) {
        this.dbFieldEncryptorPattern = dbFieldEncryptorPattern;
    }

    public FieldEncryptorPatternCache(List<PoJoFieldEncryptorPattern> poJoFieldEncryptorPatternList) {
        this.poJoFieldEncryptorPatternList = poJoFieldEncryptorPatternList;
    }

    //缓存当前加解密的bean
    private static DBFieldEncryptorPattern dbFieldEncryptorPatternCache;

    //缓存当前pojo模式下所有的加解密算法
    private static Map<PoJoAlgorithmEnum, PoJoFieldEncryptorPattern> pojoFieldEncryptorPatternCacheMap;


    /**
     * 初始化本地缓存
     *
     * @author liutangqi
     * @date 2024/9/19 14:43
     * @Param []
     **/
    public void init() {
        //1.初始化项目DB模式的加解密算法
        if (dbFieldEncryptorPatternCache == null && this.dbFieldEncryptorPattern != null) {
            dbFieldEncryptorPatternCache = this.dbFieldEncryptorPattern;
        }

        //2.初始化pojo模式的加解密算法(pojo支持多种算法同时存在，所以下面多了些配置校验)
        if (pojoFieldEncryptorPatternCacheMap == null && this.poJoFieldEncryptorPatternList != null) {
            //2.1校验
            //2.1.1 校验是否存在默认的加解密算法
            this.poJoFieldEncryptorPatternList.stream().filter(f -> PoJoAlgorithmEnum.ALGORITHM_DEFAULT.equals(f.encryptorAlgorithm()))
                    .findAny()
                    .orElseThrow(() -> new FieldEncryptorException("pojo模式下，必须存在默认的加解密算法"));
            //2.1.2 校验每种算法枚举对应的具体实现是否是一对一的
            List<PoJoAlgorithmEnum> poJoAlgorithmEnumList = this.poJoFieldEncryptorPatternList.stream()
                    .map(PoJoFieldEncryptorPattern::encryptorAlgorithm)
                    .distinct()
                    .collect(Collectors.toList());
            if (poJoAlgorithmEnumList.size() != this.poJoFieldEncryptorPatternList.size()) {
                throw new FieldEncryptorException("pojo模式下，每种加密算法枚举只能对应一种算法");
            }

            //2.2.将所有的pojo加密算法按照枚举类型分组成Map
            pojoFieldEncryptorPatternCacheMap = this.poJoFieldEncryptorPatternList.stream()
                    .collect(Collectors.toMap(PoJoFieldEncryptorPattern::encryptorAlgorithm, k -> k, (v1, v2) -> v1));
        }
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


    //------------------------------------------------对外提供的方法------------------------------------------------

    /**
     * 初始化默认的加解密函数，仅用于测试
     *
     * @author liutangqi
     * @date 2024/4/8 15:04
     * @Param []
     **/
    public static void initDeafultInstance() {
        if (dbFieldEncryptorPatternCache == null) {
            EncryptorProperties encryptorProperties = new EncryptorProperties();
            encryptorProperties.setSecretKey(SymbolConstant.DEFAULT_SECRET_KEY);
            dbFieldEncryptorPatternCache = new DefaultDBFieldEncryptorPattern(encryptorProperties);
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
        return dbFieldEncryptorPatternCache;
    }


    /**
     * 获取java pojo 加解密模式下的实现类
     *
     * @author liutangqi
     * @date 2024/7/24 17:46
     * @Param []
     **/
    public static PoJoFieldEncryptorPattern getPoJoInstance(PoJoAlgorithmEnum poJoAlgorithmEnum) {
        PoJoFieldEncryptorPattern poJoFieldEncryptorPattern = pojoFieldEncryptorPatternCacheMap.get(poJoAlgorithmEnum);
        if (poJoFieldEncryptorPattern == null) {
            throw new FieldEncryptorException(String.format("未找到指定类型的加密算法 %s", poJoAlgorithmEnum));
        }
        return poJoFieldEncryptorPattern;
    }

}
