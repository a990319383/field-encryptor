package com.sangsang.test;

import com.sangsang.cache.SqlParseCache;
import com.sangsang.cache.encryptor.EncryptorInstanceCache;
import com.sangsang.cache.isolation.IsolationInstanceCache;
import com.sangsang.cache.encryptor.TableCache;
import com.sangsang.config.properties.EncryptorProperties;
import com.sangsang.config.properties.FieldProperties;
import com.sangsang.config.properties.IsolationProperties;
import com.sangsang.encryptor.db.DefaultDBFieldEncryptorPattern;
import com.sangsang.isolation.TestIsolationData;

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
    public static void initTable() {
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
    public static void initIsolation() throws Exception {
        FieldProperties fieldProperties = new FieldProperties();
        fieldProperties.setScanEntityPackage(Arrays.asList("com.sangsang.mockentity"));
        IsolationProperties isolationProperties = new IsolationProperties();
        isolationProperties.setEnable(true);
        fieldProperties.setIsolation(isolationProperties);
        new IsolationInstanceCache().init(fieldProperties, Arrays.asList(new TestIsolationData()));
    }


    /**
     * 初始化db模式的加密算法
     *
     * @author liutangqi
     * @date 2025/6/25 17:31
     * @Param []
     **/
    public static void initDBEncryptor() {
        try {
            FieldProperties fieldProperties = new FieldProperties();
            fieldProperties.setEncryptor(new EncryptorProperties());
            EncryptorInstanceCache.mockInstance("com.sangsang.mockentity", new DefaultDBFieldEncryptorPattern(fieldProperties));
        } catch (Exception e) {
            System.out.println("异常" + e.getMessage());
        }
    }


}