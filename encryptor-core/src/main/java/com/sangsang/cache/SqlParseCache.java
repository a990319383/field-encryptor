package com.sangsang.cache;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LRUCache;
import com.sangsang.config.properties.FieldProperties;
import com.sangsang.domain.constants.NumberConstant;
import com.sangsang.util.StringUtils;
import net.sf.jsqlparser.statement.Statement;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Optional;

/**
 * sql语句解析的本地缓存
 * 优先加载这个bean，避免有些@PostConstruct 处理逻辑中需要用到这个缓存，但是这个缓存还未初始化完成
 *
 * @author liutangqi
 * @date 2025/6/12 15:28
 */
public class SqlParseCache implements BeanPostProcessor {

    /**
     * 存储当前sql的解析结果的缓存
     * key: sql的长度_sha256  com.sangsang.util.StringUtils#getSqlUniqueKey()
     * value: 解析结果
     **/
    private static LRUCache<String, Statement> SQL_PARSE_CACHE;

    /**
     * 初始化缓存
     *
     * @author liutangqi
     * @date 2025/6/12 15:40
     * @Param [fieldProperties]
     **/
    public static void init(FieldProperties fieldProperties) {
        Integer lruCapacity = Optional.ofNullable(fieldProperties.getLruCapacity()).orElse(NumberConstant.HUNDRED);
        SQL_PARSE_CACHE = CacheUtil.newLRUCache(lruCapacity);
    }


    /**
     * 通过sql获取解析结果
     *
     * @author liutangqi
     * @date 2025/6/18 11:15
     * @Param [sql]
     **/
    public static Statement getSqlParseCache(String sql) {
        return SQL_PARSE_CACHE.get(StringUtils.getSqlUniqueKey(sql));
    }

    /**
     * 设置sql的解析结果到缓存
     *
     * @author liutangqi
     * @date 2025/6/18 11:16
     * @Param [sql, baseFieldParseTable]
     **/
    public static void setSqlParseCache(String sql, Statement statement) {
        SQL_PARSE_CACHE.put(StringUtils.getSqlUniqueKey(sql), statement);
    }


    /**
     * 实现父类default方法，避免低版本不兼容，找不到实现类
     *
     * @author liutangqi
     * @date 2025/5/21 10:28
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
     * @date 2025/5/21 10:28
     * @Param [bean, beanName]
     **/
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
