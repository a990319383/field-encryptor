package com.sangsang.domain.dto;

import java.util.Objects;
import java.util.Set;

/**
 * @author liutangqi
 * @date 2024/5/17 10:59
 */
public class TableInfoDto {
    /**
     * 表名（小写）
     */
    private String tableName;

    /**
     * 该表拥有的全部字段（小写）
     */
    private Set<TableFieldDto> tableFields;


    //------------------------lombok分割线（尽量少引依赖的原则，项目不引入lombok，下面拷贝lombok编译后的结果）---------------------------------------------------------

    public static TableInfoDto.TableInfoDtoBuilder builder() {
        return new TableInfoDto.TableInfoDtoBuilder();
    }

    public String getTableName() {
        return this.tableName;
    }

    public Set<TableFieldDto> getTableFields() {
        return this.tableFields;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTableFields(Set<TableFieldDto> tableFields) {
        this.tableFields = tableFields;
    }

    protected boolean canEqual(Object other) {
        return other instanceof TableInfoDto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableInfoDto that = (TableInfoDto) o;
        return Objects.equals(tableName, that.tableName) &&
                Objects.equals(tableFields, that.tableFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, tableFields);
    }

    @Override
    public String toString() {
        return "TableInfoDto(tableName=" + this.getTableName() + ", tableFields=" + this.getTableFields() + ")";
    }

    public TableInfoDto(String tableName, Set<TableFieldDto> tableFields) {
        this.tableName = tableName;
        this.tableFields = tableFields;
    }

    public TableInfoDto() {
    }

    public static class TableInfoDtoBuilder {
        private String tableName;
        private Set<TableFieldDto> tableFields;

        TableInfoDtoBuilder() {
        }

        public TableInfoDto.TableInfoDtoBuilder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public TableInfoDto.TableInfoDtoBuilder tableFields(Set<TableFieldDto> tableFields) {
            this.tableFields = tableFields;
            return this;
        }

        public TableInfoDto build() {
            return new TableInfoDto(this.tableName, this.tableFields);
        }

        @Override
        public String toString() {
            return "TableInfoDto.TableInfoDtoBuilder(tableName=" + this.tableName + ", tableFields=" + this.tableFields + ")";
        }
    }


}
