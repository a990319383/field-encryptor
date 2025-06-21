package com.sangsang.domain.dto;

import net.sf.jsqlparser.schema.Column;

/**
 * @author liutangqi
 * @date 2025/5/23 9:51
 */
public class ColumnTransformationDto {
    /**
     * 当前具体的字段
     */
    private Column column;

    /**
     * 是否是表字段
     * true: 当前Column属于表字段
     * false: 当前Column不属于表字段，属于常量
     */
    private boolean tableFiled;

    public ColumnTransformationDto(Column column, boolean tableFiled) {
        this.column = column;
        this.tableFiled = tableFiled;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public boolean isTableFiled() {
        return tableFiled;
    }

    public void setTableFiled(boolean tableFiled) {
        this.tableFiled = tableFiled;
    }
}
