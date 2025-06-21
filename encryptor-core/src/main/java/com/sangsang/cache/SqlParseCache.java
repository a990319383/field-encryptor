package com.sangsang.cache;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LRUCache;
import com.sangsang.config.properties.FieldProperties;
import com.sangsang.domain.constants.NumberConstant;
import com.sangsang.util.StringUtils;
import net.sf.jsqlparser.statement.Statement;

import java.util.Optional;

/**
 * sql语句解析的本地缓存
 *
 * @author liutangqi
 * @date 2025/6/12 15:28
 */
public class SqlParseCache {

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

}
