package com.sangsang.domain.dto;

/**
 * 从数据库查到的表结构字段信息
 *
 * @author liutangqi
 * @date 2024/8/27 10:26
 **/
public class TableFieldMsgDto {
    /**
     * 表名
     */
    private String tableName;
    /**
     * 字段名
     */
    private String columnName;
    /**
     * 字段类型
     */
    private String dataType;
    /**
     * 字段索引
     */
    private String columnKey;
    /**
     * 字段大概长度
     */
    private Integer fieldLength;

    /**
     * 字段的备注
     */
    private String columnComment;


    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }

    public Integer getFieldLength() {
        return fieldLength;
    }

    public void setFieldLength(Integer fieldLength) {
        this.fieldLength = fieldLength;
    }
}
