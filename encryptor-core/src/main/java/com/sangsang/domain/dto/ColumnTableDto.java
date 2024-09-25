package com.sangsang.domain.dto;


import java.util.Objects;

/**
 * @author liutangqi
 * @date 2024/3/6 14:46
 */
public class ColumnTableDto {
    /**
     * 字段所属的表的别名(from 后面接的表的别名)
     */
    private String tableAliasName;
    /**
     * 字段所属表的真实表的名字
     */
    private String sourceTableName;
    /**
     * 字段所属真实字段名
     */
    private String sourceColumn;
    /**
     * 该字段是否直接从真实的表中关联获取的
     * 栗子：select user_name   from tb_user    user_name 这个字段真实属于tb_user的，这个值就是ture
     * select a.userName from (select user_name   from tb_user )a    userName这个字段是来自于表a 的，表a不是真实的数据来源表，所以这个值是false
     **/
    private boolean fromSourceTable = false;


    //------------------------lombok分割线（尽量少引依赖的原则，项目不引入lombok，下面拷贝lombok编译后的结果）---------------------------------------------------------


    private static boolean $default$fromSourceTable() {
        return false;
    }

    public static ColumnTableDtoBuilder builder() {
        return new ColumnTableDtoBuilder();
    }

    public String getTableAliasName() {
        return this.tableAliasName;
    }

    public String getSourceTableName() {
        return this.sourceTableName;
    }

    public String getSourceColumn() {
        return this.sourceColumn;
    }

    public boolean isFromSourceTable() {
        return this.fromSourceTable;
    }

    public void setTableAliasName(String tableAliasName) {
        this.tableAliasName = tableAliasName;
    }

    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }

    public void setSourceColumn(String sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public void setFromSourceTable(boolean fromSourceTable) {
        this.fromSourceTable = fromSourceTable;
    }

    protected boolean canEqual(Object other) {
        return other instanceof ColumnTableDto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnTableDto that = (ColumnTableDto) o;
        return fromSourceTable == that.fromSourceTable &&
                Objects.equals(tableAliasName, that.tableAliasName) &&
                Objects.equals(sourceTableName, that.sourceTableName) &&
                Objects.equals(sourceColumn, that.sourceColumn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableAliasName, sourceTableName, sourceColumn, fromSourceTable);
    }

    @Override
    public String toString() {
        return "ColumnTableDto(tableAliasName=" + this.getTableAliasName() + ", sourceTableName=" + this.getSourceTableName() + ", sourceColumn=" + this.getSourceColumn() + ", fromSourceTable=" + this.isFromSourceTable() + ")";
    }

    public ColumnTableDto(String tableAliasName, String sourceTableName, String sourceColumn, boolean fromSourceTable) {
        this.tableAliasName = tableAliasName;
        this.sourceTableName = sourceTableName;
        this.sourceColumn = sourceColumn;
        this.fromSourceTable = fromSourceTable;
    }

    public ColumnTableDto() {
        this.fromSourceTable = $default$fromSourceTable();
    }

    public static class ColumnTableDtoBuilder {
        private String tableAliasName;
        private String sourceTableName;
        private String sourceColumn;
        private boolean fromSourceTable$set;
        private boolean fromSourceTable$value;

        ColumnTableDtoBuilder() {
        }

        public ColumnTableDtoBuilder tableAliasName(String tableAliasName) {
            this.tableAliasName = tableAliasName;
            return this;
        }

        public ColumnTableDtoBuilder sourceTableName(String sourceTableName) {
            this.sourceTableName = sourceTableName;
            return this;
        }

        public ColumnTableDtoBuilder sourceColumn(String sourceColumn) {
            this.sourceColumn = sourceColumn;
            return this;
        }

        public ColumnTableDtoBuilder fromSourceTable(boolean fromSourceTable) {
            this.fromSourceTable$value = fromSourceTable;
            this.fromSourceTable$set = true;
            return this;
        }

        public ColumnTableDto build() {
            boolean fromSourceTable$value = this.fromSourceTable$value;
            if (!this.fromSourceTable$set) {
                fromSourceTable$value = ColumnTableDto.$default$fromSourceTable();
            }

            return new ColumnTableDto(this.tableAliasName, this.sourceTableName, this.sourceColumn, fromSourceTable$value);
        }

        @Override
        public String toString() {
            return "ColumnTableDto.ColumnTableDtoBuilder(tableAliasName=" + this.tableAliasName + ", sourceTableName=" + this.sourceTableName + ", sourceColumn=" + this.sourceColumn + ", fromSourceTable$value=" + this.fromSourceTable$value + ")";
        }
    }
}
