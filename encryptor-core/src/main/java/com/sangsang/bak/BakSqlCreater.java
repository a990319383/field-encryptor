package com.sangsang.bak;

import com.sangsang.cache.encryptor.EncryptorInstanceCache;
import com.sangsang.cache.encryptor.TableCache;
import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.domain.dto.TableFieldMsgDto;
import com.sangsang.util.StringUtils;
import net.sf.jsqlparser.schema.Column;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 生成备份DDL,DML 的工具类
 *
 * @author liutangqi
 * @date 2024/8/22 13:29
 */
public class BakSqlCreater {

    /**
     * 根据后缀，生成备份表的建表语句
     *
     * @author liutangqi
     * @date 2024/8/26 16:12
     * @Param [tableFieldMsgList 从库中查询到的表，字段结构信息 ; suffix: 备份表的后缀 ;expansionMultiple 解密字段后的扩容倍数，建议值5]
     **/
    public void bakSql(GainTableFieldInterface gainTableFieldInterface, String suffix, Integer expansionMultiple) {
        //1.过滤出需要加解密的表的主键和需要加解密的字段
        List<TableFieldMsgDto> tableFieldMsgList = gainTableFieldInterface.getTableField().stream()
                .filter(f -> TableCache.getFieldEncryptTable().contains(f.getTableName().toLowerCase()))
                .filter(f -> StringUtils.equalCaseInsensitive(SymbolConstant.PRIMARY_KEY, f.getColumnKey()) //主键
                        ||
                        TableCache.getTableFieldEncryptInfo()
                                .getOrDefault(f.getTableName().toLowerCase(), new HashMap<>())
                                .getOrDefault(f.getColumnName().toLowerCase(), null) != null //需要加解密的字段
                ).peek(p -> p.setFieldEncryptor(TableCache.getTableFieldEncryptInfo().get(p.getTableName().toLowerCase()).get(p.getColumnName().toLowerCase())))
                .collect(Collectors.toList());

        //2.根据表名进行分组
        Map<String, List<TableFieldMsgDto>> tableFieldMsgeMap = tableFieldMsgList.stream().collect(Collectors.groupingBy(TableFieldMsgDto::getTableName));

        for (Map.Entry<String, List<TableFieldMsgDto>> tableFieldEntry : tableFieldMsgeMap.entrySet()) {
            //3.处理表名
            String bakTableName = tableFieldEntry.getKey() + "_bak_" + suffix;

            //4.创建备份表的ddl
            String ddlCreateBakTable = ddlCreateBakTable(tableFieldEntry, bakTableName, expansionMultiple);
            System.out.println(ddlCreateBakTable);

            //5.创建扩展原表字段长度的ddl
            String ddlExpansionField = ddlExpansionField(tableFieldEntry, expansionMultiple);
            System.out.println(ddlExpansionField);

            //6.创建备份表初始化的DML
            String dmlInitBakTable = dmlInitBakTable(tableFieldEntry, bakTableName);
            System.out.println(dmlInitBakTable);

            //7.创建根据备份表清洗原表数据DML
            String dmlUpdateTable = dmlUpdateTable(tableFieldEntry, bakTableName);
            System.out.println(dmlUpdateTable);

            //8.创建根据备份表回滚数据DML
            String dmlRollBackTable = dmlRollBackTable(tableFieldEntry, bakTableName);
            System.out.println(dmlRollBackTable);

            System.out.println("----------------------------");

        }
    }


    /**
     * 创建根据备份表回滚数据DML
     *
     * @author liutangqi
     * @date 2024/8/27 15:24
     * @Param [tableFieldEntry, bakTableName]
     **/
    private String dmlRollBackTable(Map.Entry<String, List<TableFieldMsgDto>> tableFieldEntry, String bakTableName) {
        //获取到主键字段
        String primaryKey = tableFieldEntry.getValue()
                .stream()
                .filter(f -> StringUtils.equalCaseInsensitive(SymbolConstant.PRIMARY_KEY, f.getColumnKey()))
                .findAny()
                .get()
                .getColumnName();

        String sql = "update " + tableFieldEntry.getKey() + " t \n\r  join " + bakTableName + " bak \n\r on t." + primaryKey + " = bak." + primaryKey + "\n\r set ";
        //过滤掉主键
        List<TableFieldMsgDto> tableFieldMsgDtos = tableFieldEntry.getValue().stream().filter(f -> !StringUtils.equalCaseInsensitive(SymbolConstant.PRIMARY_KEY, f.getColumnKey())).collect(Collectors.toList());
        for (TableFieldMsgDto tableFieldMsgDto : tableFieldMsgDtos) {
            sql = sql + "t." + tableFieldMsgDto.getColumnName() + " = bak." + tableFieldMsgDto.getColumnName() + ",";
        }

        //去除“,”
        sql = sql.substring(0, sql.lastIndexOf(","));
        return sql + ";";
    }

    /**
     * 创建根据备份表清洗原表数据DML
     *
     * @author liutangqi
     * @date 2024/8/27 15:24
     * @Param [tableFieldEntry, bakTableName]
     **/
    private String dmlUpdateTable(Map.Entry<String, List<TableFieldMsgDto>> tableFieldEntry, String bakTableName) {
        //获取到主键字段
        String primaryKey = tableFieldEntry.getValue()
                .stream()
                .filter(f -> StringUtils.equalCaseInsensitive(SymbolConstant.PRIMARY_KEY, f.getColumnKey()))
                .findAny()
                .get()
                .getColumnName();

        String sql = "update " + tableFieldEntry.getKey() + " t \n\r  join " + bakTableName + " bak \n\r on t." + primaryKey + " = bak." + primaryKey + "\n\r set ";
        //过滤掉主键
        List<TableFieldMsgDto> tableFieldMsgDtos = tableFieldEntry.getValue().stream().filter(f -> !StringUtils.equalCaseInsensitive(SymbolConstant.PRIMARY_KEY, f.getColumnKey())).collect(Collectors.toList());
        for (TableFieldMsgDto tableFieldMsgDto : tableFieldMsgDtos) {
            String encryptionField = EncryptorInstanceCache.getInstance(tableFieldMsgDto.getFieldEncryptor().value()).encryption(new Column("bak." + tableFieldMsgDto.getColumnName())).toString();
            sql = sql + "t." + tableFieldMsgDto.getColumnName() + " = " + encryptionField + ",";
        }

        //去除“,”
        sql = sql.substring(0, sql.lastIndexOf(","));
        return sql + ";";
    }

    /**
     * 生成备份表的初始化dml sql
     *
     * @author liutangqi
     * @date 2024/8/27 14:49
     * @Param [tableFieldEntry, bakTableName]
     **/
    private String dmlInitBakTable(Map.Entry<String, List<TableFieldMsgDto>> tableFieldEntry, String bakTableName) {
        String fieldList = "";
        for (TableFieldMsgDto tableFieldMsgDto : tableFieldEntry.getValue()) {
            fieldList = fieldList + tableFieldMsgDto.getColumnName() + ",";
        }
        //去除“,”
        fieldList = fieldList.substring(0, fieldList.lastIndexOf(","));

        String sql = "INSERT INTO " + bakTableName + "(" + fieldList + ")\n\r (select " + fieldList + " from " + tableFieldEntry.getKey() + ");";
        return sql;
    }


    /**
     * 生成创建备份表的ddl
     *
     * @author liutangqi
     * @date 2024/8/27 9:36
     * @Param [tableFieldEntry key:表名 value:表字段, bakTableName 备份表名字]
     **/
    private String ddlCreateBakTable(Map.Entry<String, List<TableFieldMsgDto>> tableFieldEntry, String bakTableName, Integer expansionMultiple) {
        //1.处理主键信息
        TableFieldMsgDto primaryKeyField = tableFieldEntry.getValue()
                .stream()
                .filter(f -> StringUtils.equalCaseInsensitive(SymbolConstant.PRIMARY_KEY, f.getColumnKey()))
                .findAny()
                .get();
        //1.1 主键类型
        String primaryKeyType = primaryKeyField.getDataType();
        if (StringUtils.equalCaseInsensitive(SymbolConstant.VARCHAR, primaryKeyType)) {
            primaryKeyType = "varchar(" + primaryKeyField.getFieldLength() + ")";
        }

        //1.2主键字段名
        String primaryKeyFieldName = primaryKeyField.getColumnName();

        //2.处理加密字段
        String encryptorFieldSql = "";
        //过滤主键
        List<TableFieldMsgDto> tableFieldMsgDtos = tableFieldEntry.getValue().stream().filter(f -> !StringUtils.equalCaseInsensitive(SymbolConstant.PRIMARY_KEY, f.getColumnKey())).collect(Collectors.toList());
        for (TableFieldMsgDto tableFieldMsgDto : tableFieldMsgDtos) {
            String dataType = tableFieldMsgDto.getDataType();
            if (tableFieldMsgDto.getFieldLength() != null) {
                dataType = dataType + "(" + tableFieldMsgDto.getFieldLength() * expansionMultiple + ") ";
            }
            encryptorFieldSql = encryptorFieldSql + "`" + tableFieldMsgDto.getColumnName() + "`  " + dataType + " DEFAULT NULL COMMENT " + "'" + tableFieldMsgDto.getColumnComment() + "',\n\r";
        }

        //3.拼接sql
        String ddl = "CREATE TABLE " + "`" + bakTableName + "` (\n\r"
                + "`" + primaryKeyFieldName + "` " + primaryKeyType + " NOT NULL COMMENT '主键',\n\r"
                + encryptorFieldSql
                + "PRIMARY KEY (`" + primaryKeyFieldName + "`)\n\r"
                + ")COMMENT='" + tableFieldEntry.getKey() + "备份表';";
        return ddl;
    }


    /**
     * 将原表字段进行扩大的ddl
     *
     * @author liutangqi
     * @date 2024/8/27 9:41
     * @Param [tableFieldEntry, encryptorField]
     **/
    private String ddlExpansionField(Map.Entry<String, List<TableFieldMsgDto>> tableFieldEntry, Integer expansionMultiple) {
        String tableName = tableFieldEntry.getKey();
        String sql = "ALTER TABLE " + tableName + " \n\r";

        //过滤主键
        List<TableFieldMsgDto> tableFieldMsgDtos = tableFieldEntry.getValue().stream().filter(f -> !StringUtils.equalCaseInsensitive(SymbolConstant.PRIMARY_KEY, f.getColumnKey())).collect(Collectors.toList());
        for (TableFieldMsgDto tableFieldMsgDto : tableFieldMsgDtos) {
            String dataType = tableFieldMsgDto.getDataType();
            if (tableFieldMsgDto.getFieldLength() != null) {
                dataType = dataType + "(" + tableFieldMsgDto.getFieldLength() * expansionMultiple + ") ";
            }
            sql = sql + " MODIFY COLUMN " + tableFieldMsgDto.getColumnName() + " " + dataType + " NULL COMMENT '" + tableFieldMsgDto.getColumnComment() + "',";
        }
        sql = sql.substring(0, sql.lastIndexOf(","));
        return sql + ";";
    }
}
