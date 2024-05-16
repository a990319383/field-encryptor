package com.sangsang.cache;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sangsang.domain.annos.FieldEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author liutangqi
 * @date 2024/2/1 13:27
 */
@Configuration
public class TableCache {
    private static final Logger log = LoggerFactory.getLogger(TableCache.class);

    /**
     * key: 表名小写  value: (key:字段名小写  value: 实体类上标注的@FieldEncryptor注解)
     */
    private static final Map<String, Map<String, FieldEncryptor>> TABLE_ENTITY_CACHE = new HashMap<>();

    /**
     * 有字段需要加解密的表名的集合（小写）
     */
    private static final Set<String> FIELD_ENCRYPT_TABLE = new HashSet<>();

    /**
     * key: 表名 小写
     * value: 改表实体类上所有的字段 小写
     */
    private static final Map<String, Set<String>> TABLE_FIELD_MAP = new HashMap<>();

    /**
     * 初始化当前表结构信息
     *
     * @author liutangqi
     * @date 2024/2/1 13:27
     * @Param []
     **/
    @PostConstruct
    public void init() {
        //获取所有的表结构信息
        List<TableInfo> tableInfos = TableInfoHelper.getTableInfos();

        for (TableInfo tableInfo : tableInfos) {
            //表名
            String tableName = tableInfo.getTableName();

            //获取所有的字段（主键外）
            List<TableFieldInfo> tableFieldInfos = tableInfo.getFieldList();

            //维护每个字段上的@FieldEncryptor
            Map<String, FieldEncryptor> fieldEncryptMap = new HashMap<>();

            //该表的所有字段
            Set<String> tableFiledSet = new HashSet<>();
            //维护主键
            Optional.ofNullable(tableInfo.getKeyColumn())
                    .map(String::toLowerCase)
                    .ifPresent(id -> tableFiledSet.add(id));

            for (TableFieldInfo tableFieldInfo : tableFieldInfos) {
                tableFiledSet.add(tableFieldInfo.getColumn().toLowerCase());

                //@FieldEncryptor 只对String 类型的字段有效
                if (!String.class.equals(tableFieldInfo.getPropertyType())) {
                    continue;
                }

                FieldEncryptor fieldEncryptor = tableFieldInfo.getField().getAnnotation(FieldEncryptor.class);
                fieldEncryptMap.put(tableFieldInfo.getColumn().toLowerCase(), fieldEncryptor);

                if (fieldEncryptor != null) {
                    FIELD_ENCRYPT_TABLE.add(tableInfo.getTableName().toLowerCase());
                }
            }

            TABLE_FIELD_MAP.put(tableName.toLowerCase(), tableFiledSet);
            TABLE_ENTITY_CACHE.put(tableName.toLowerCase(), fieldEncryptMap);
        }

        log.info("初始化表字段加密信息完毕");
    }


    /**
     * 获取当前项目所有表字段是否加密的信息
     *
     * @author liutangqi
     * @date 2024/2/1 14:07
     * @Param []
     **/
    public static Map<String, Map<String, FieldEncryptor>> getTableFieldEncryptInfo() {
        return TABLE_ENTITY_CACHE;
    }

    /**
     * 获取当前有字段需要加密的所有表
     *
     * @author liutangqi
     * @date 2024/2/20 10:36
     * @Param []
     **/
    public static Set<String> getFieldEncryptTable() {
        return FIELD_ENCRYPT_TABLE;
    }

    /**
     * 获取当前表拥有的所有字段
     *
     * @author liutangqi
     * @date 2024/2/20 10:48
     * @Param []
     **/
    public static Map<String, Set<String>> getTableFieldMap() {
        return TABLE_FIELD_MAP;
    }
}
