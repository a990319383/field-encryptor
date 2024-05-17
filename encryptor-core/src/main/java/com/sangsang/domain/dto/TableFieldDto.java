package com.sangsang.domain.dto;

import com.sangsang.domain.annos.FieldEncryptor;

import java.util.Objects;

/**
 * @author liutangqi
 * @date 2024/5/17 11:12
 */
public class TableFieldDto {
    /**
     * 字段名(小写)
     */
    private String fieldName;

    /**
     * 字段上拥有的@FieldEncryptor 注解
     */
    private FieldEncryptor fieldEncryptor;


    //------------------------lombok分割线（尽量少引依赖的原则，项目不引入lombok，下面拷贝lombok编译后的结果）---------------------------------------------------------


    public static TableFieldDto.TableFieldDtoBuilder builder() {
        return new TableFieldDto.TableFieldDtoBuilder();
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public FieldEncryptor getFieldEncryptor() {
        return this.fieldEncryptor;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setFieldEncryptor(FieldEncryptor fieldEncryptor) {
        this.fieldEncryptor = fieldEncryptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableFieldDto that = (TableFieldDto) o;
        return Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(fieldEncryptor, that.fieldEncryptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, fieldEncryptor);
    }

    protected boolean canEqual(Object other) {
        return other instanceof TableFieldDto;
    }

    @Override
    public String toString() {
        return "TableFieldDto(fieldName=" + this.getFieldName() + ", fieldEncryptor=" + this.getFieldEncryptor() + ")";
    }

    public TableFieldDto(String fieldName, FieldEncryptor fieldEncryptor) {
        this.fieldName = fieldName;
        this.fieldEncryptor = fieldEncryptor;
    }

    public TableFieldDto() {
    }

    public static class TableFieldDtoBuilder {
        private String fieldName;
        private FieldEncryptor fieldEncryptor;

        TableFieldDtoBuilder() {
        }

        public TableFieldDto.TableFieldDtoBuilder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public TableFieldDto.TableFieldDtoBuilder fieldEncryptor(FieldEncryptor fieldEncryptor) {
            this.fieldEncryptor = fieldEncryptor;
            return this;
        }

        public TableFieldDto build() {
            return new TableFieldDto(this.fieldName, this.fieldEncryptor);
        }

        @Override
        public String toString() {
            return "TableFieldDto.TableFieldDtoBuilder(fieldName=" + this.fieldName + ", fieldEncryptor=" + this.fieldEncryptor + ")";
        }
    }
}
