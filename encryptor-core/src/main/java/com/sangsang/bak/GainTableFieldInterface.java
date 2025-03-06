package com.sangsang.bak;

import com.sangsang.domain.dto.TableFieldMsgDto;

import java.util.List;

/**
 * 想要获取默认备份表相关的DDL，DML语句时，实现此接口，将下列sql从库里面查询出来
 * 将下面方法的结果作为参数传给com.sangsang.bak.BakSqlCreater#bakSql()
 *
 * @author liutangqi
 * @date 2024/8/27 10:54
 */
public interface GainTableFieldInterface {

    /**
     * 想要获取备份表的ddl语句时从自己的库里面执行下面sql
     * select
     * TABLE_NAME as tableName,
     * COLUMN_NAME as columnName,
     * DATA_TYPE as dataType,
     * COLUMN_KEY as columnKey,
     * COLUMN_COMMENT as columnComment,
     * case
     * when DATA_TYPE like 'char%' then CHARACTER_MAXIMUM_LENGTH
     * when DATA_TYPE like 'varchar%' then CHARACTER_MAXIMUM_LENGTH
     * when DATA_TYPE like 'text%' then CHARACTER_OCTET_LENGTH / CHAR_LENGTH(CHARACTER_SET_NAME)
     * else null
     * end as fieldLength     -- 备注：这个字段长度是一个大概值
     * from
     * INFORMATION_SCHEMA.COLUMNS
     * where
     * TABLE_SCHEMA = '你自己的库名'
     *
     * @author liutangqi
     * @date 2024/8/27 10:55
     * @Param []
     **/
    List<TableFieldMsgDto> getTableField();
}
