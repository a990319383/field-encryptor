package com.sangsang.test;

import com.sangsang.cache.SqlParseCache;
import com.sangsang.cache.IsolationCache;
import com.sangsang.cache.TableCache;
import com.sangsang.config.properties.FieldProperties;
import com.sangsang.config.properties.IsolationProperties;

import java.util.*;

/**
 * @author liutangqi
 * @date 2024/4/2 15:58
 */
public class InitTableInfo {

    /**
     * 将TableCache的数据做个mock
     * 实体类是 com.sangsang.mockentity.MenuEntity  com.sangsang.mockentity.UserEntity
     * 其中，只有tb_user表的phone字段需要进行加密
     *
     * @author liutangqi
     * @date 2024/4/2 15:58
     * @Param []
     **/
    public static void initTable() throws NoSuchFieldException {
        FieldProperties fieldProperties = new FieldProperties();
        fieldProperties.setScanEntityPackage(Arrays.asList("com.sangsang.mockentity"));
        TableCache tableCache = new TableCache(fieldProperties);
        tableCache.init();
        //初始化数据解析缓存
        SqlParseCache.init(new FieldProperties());
    }

    /**
     * 初始化数据隔离
     *
     * @author liutangqi
     * @date 2025/6/13 15:14
     * @Param []
     **/
    public static void initIsolation() {
        FieldProperties fieldProperties = new FieldProperties();
        fieldProperties.setScanEntityPackage(Arrays.asList("com.sangsang.mockentity"));
        IsolationProperties isolationProperties = new IsolationProperties();
        isolationProperties.setField("org_seq");
        isolationProperties.setIsolationClass("com.sangsang.isolation.TestDataIsolation");
        isolationProperties.setRelation("likePrefix");
        fieldProperties.setIsolation(isolationProperties);

        new IsolationCache(fieldProperties).init();

    }
}