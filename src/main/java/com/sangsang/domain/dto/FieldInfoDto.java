package com.sangsang.domain.dto;


import java.io.Serializable;
import java.util.Objects;

/**
 * @author liutangqi
 * @date 2024/3/6 10:30
 */
public class FieldInfoDto implements Serializable {
    /**
     * 字段的别名或者是原字段名
     * 注意：这个不转换为小写，转换为小写后会影响别名的驼峰
     **/
    private String columnName;
    /**
     * 该字段来源自哪个字段
     * 注意：小写
     * 注意：目前不兼容一个字段由几个字段的值聚合而成的写法
     */
    private String sourceColumn;
    /**
     * 字段取自数据库的那张表的表名
     * 注意：小写
     */
    private String sourceTableName;

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

    public static FieldInfoDto.FieldInfoDtoBuilder builder() {
        return new FieldInfoDto.FieldInfoDtoBuilder();
    }

    public FieldInfoDto(String columnName, String sourceColumn, String sourceTableName, boolean fromSourceTable) {
        this.columnName = columnName;
        this.sourceColumn = sourceColumn;
        this.sourceTableName = sourceTableName;
        this.fromSourceTable = fromSourceTable;
    }

    public FieldInfoDto() {
        this.fromSourceTable = $default$fromSourceTable();
    }

    public String getColumnName() {
        return this.columnName;
    }

    public String getSourceColumn() {
        return this.sourceColumn;
    }

    public String getSourceTableName() {
        return this.sourceTableName;
    }

    public boolean isFromSourceTable() {
        return this.fromSourceTable;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setSourceColumn(String sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }

    public void setFromSourceTable(boolean fromSourceTable) {
        this.fromSourceTable = fromSourceTable;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof FieldInfoDto)) {
            return false;
        } else {
            FieldInfoDto other = (FieldInfoDto) o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.isFromSourceTable() != other.isFromSourceTable()) {
                return false;
            } else {
                label49:
                {
                    Object this$columnName = this.getColumnName();
                    Object other$columnName = other.getColumnName();
                    if (this$columnName == null) {
                        if (other$columnName == null) {
                            break label49;
                        }
                    } else if (this$columnName.equals(other$columnName)) {
                        break label49;
                    }

                    return false;
                }

                Object this$sourceColumn = this.getSourceColumn();
                Object other$sourceColumn = other.getSourceColumn();
                if (this$sourceColumn == null) {
                    if (other$sourceColumn != null) {
                        return false;
                    }
                } else if (!this$sourceColumn.equals(other$sourceColumn)) {
                    return false;
                }

                Object this$sourceTableName = this.getSourceTableName();
                Object other$sourceTableName = other.getSourceTableName();
                if (this$sourceTableName == null) {
                    if (other$sourceTableName != null) {
                        return false;
                    }
                } else if (!this$sourceTableName.equals(other$sourceTableName)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof FieldInfoDto;
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnName, sourceColumn, sourceTableName, fromSourceTable);
    }

    @Override
    public String toString() {
        return "FieldInfoDto(columnName=" + this.getColumnName() + ", sourceColumn=" + this.getSourceColumn() + ", sourceTableName=" + this.getSourceTableName() + ", fromSourceTable=" + this.isFromSourceTable() + ")";
    }

    public static class FieldInfoDtoBuilder {
        private String columnName;
        private String sourceColumn;
        private String sourceTableName;
        private boolean fromSourceTable$set;
        private boolean fromSourceTable$value;

        FieldInfoDtoBuilder() {
        }

        public FieldInfoDto.FieldInfoDtoBuilder columnName(String columnName) {
            this.columnName = columnName;
            return this;
        }

        public FieldInfoDto.FieldInfoDtoBuilder sourceColumn(String sourceColumn) {
            this.sourceColumn = sourceColumn;
            return this;
        }

        public FieldInfoDto.FieldInfoDtoBuilder sourceTableName(String sourceTableName) {
            this.sourceTableName = sourceTableName;
            return this;
        }

        public FieldInfoDto.FieldInfoDtoBuilder fromSourceTable(boolean fromSourceTable) {
            this.fromSourceTable$value = fromSourceTable;
            this.fromSourceTable$set = true;
            return this;
        }

        public FieldInfoDto build() {
            boolean fromSourceTable$value = this.fromSourceTable$value;
            if (!this.fromSourceTable$set) {
                fromSourceTable$value = FieldInfoDto.$default$fromSourceTable();
            }

            return new FieldInfoDto(this.columnName, this.sourceColumn, this.sourceTableName, fromSourceTable$value);
        }

        @Override
        public String toString() {
            return "FieldInfoDto.FieldInfoDtoBuilder(columnName=" + this.columnName + ", sourceColumn=" + this.sourceColumn + ", sourceTableName=" + this.sourceTableName + ", fromSourceTable$value=" + this.fromSourceTable$value + ")";
        }
    }
}
