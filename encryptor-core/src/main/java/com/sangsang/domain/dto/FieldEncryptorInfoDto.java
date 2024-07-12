package com.sangsang.domain.dto;

import com.sangsang.domain.annos.FieldEncryptor;

import java.util.Objects;

/**
 * 字段所属表信息，加密注解信息
 *
 * @author liutangqi
 * @date 2024/7/8 10:23
 */
public class FieldEncryptorInfoDto {
    /**
     * 字段的别名或者是原字段名
     * 注意：这个不转换为小写，转换为小写后会影响别名的驼峰
     **/
    private String columnName;
    /**
     * 该字段来源自哪个字段
     * 注意：小写
     */
    private String sourceColumn;
    /**
     * 字段取自数据库的那张表的表名
     * 注意：小写
     */
    private String sourceTableName;

    /**
     * 该字段上标准的注解
     */
    private FieldEncryptor fieldEncryptor;

    public static FieldEncryptorInfoDto.FieldEncryptorInfoDtoBuilder builder() {
        return new FieldEncryptorInfoDto.FieldEncryptorInfoDtoBuilder();
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

    public FieldEncryptor getFieldEncryptor() {
        return this.fieldEncryptor;
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

    public void setFieldEncryptor(FieldEncryptor fieldEncryptor) {
        this.fieldEncryptor = fieldEncryptor;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof FieldEncryptorInfoDto)) {
            return false;
        } else {
            FieldEncryptorInfoDto other = (FieldEncryptorInfoDto) o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label59:
                {
                    Object this$columnName = this.getColumnName();
                    Object other$columnName = other.getColumnName();
                    if (this$columnName == null) {
                        if (other$columnName == null) {
                            break label59;
                        }
                    } else if (this$columnName.equals(other$columnName)) {
                        break label59;
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

                Object this$fieldEncryptor = this.getFieldEncryptor();
                Object other$fieldEncryptor = other.getFieldEncryptor();
                if (this$fieldEncryptor == null) {
                    if (other$fieldEncryptor != null) {
                        return false;
                    }
                } else if (!this$fieldEncryptor.equals(other$fieldEncryptor)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof FieldEncryptorInfoDto;
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnName, sourceColumn, sourceTableName, fieldEncryptor);
    }

    @Override
    public String toString() {
        return "FieldEncryptorInfoDto(columnName=" + this.getColumnName() + ", sourceColumn=" + this.getSourceColumn() + ", sourceTableName=" + this.getSourceTableName() + ", fieldEncryptor=" + this.getFieldEncryptor() + ")";
    }

    public FieldEncryptorInfoDto(String columnName, String sourceColumn, String sourceTableName, FieldEncryptor fieldEncryptor) {
        this.columnName = columnName;
        this.sourceColumn = sourceColumn;
        this.sourceTableName = sourceTableName;
        this.fieldEncryptor = fieldEncryptor;
    }

    public FieldEncryptorInfoDto() {
    }

    public static class FieldEncryptorInfoDtoBuilder {
        private String columnName;
        private String sourceColumn;
        private String sourceTableName;
        private FieldEncryptor fieldEncryptor;

        FieldEncryptorInfoDtoBuilder() {
        }

        public FieldEncryptorInfoDto.FieldEncryptorInfoDtoBuilder columnName(String columnName) {
            this.columnName = columnName;
            return this;
        }

        public FieldEncryptorInfoDto.FieldEncryptorInfoDtoBuilder sourceColumn(String sourceColumn) {
            this.sourceColumn = sourceColumn;
            return this;
        }

        public FieldEncryptorInfoDto.FieldEncryptorInfoDtoBuilder sourceTableName(String sourceTableName) {
            this.sourceTableName = sourceTableName;
            return this;
        }

        public FieldEncryptorInfoDto.FieldEncryptorInfoDtoBuilder fieldEncryptor(FieldEncryptor fieldEncryptor) {
            this.fieldEncryptor = fieldEncryptor;
            return this;
        }

        public FieldEncryptorInfoDto build() {
            return new FieldEncryptorInfoDto(this.columnName, this.sourceColumn, this.sourceTableName, this.fieldEncryptor);
        }

        @Override
        public String toString() {
            return "FieldEncryptorInfoDto.FieldEncryptorInfoDtoBuilder(columnName=" + this.columnName + ", sourceColumn=" + this.sourceColumn + ", sourceTableName=" + this.sourceTableName + ", fieldEncryptor=" + this.fieldEncryptor + ")";
        }
    }
}
