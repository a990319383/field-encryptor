package com.sangsang.domain.dto;


import java.util.Objects;

/**
 * @author liutangqi
 * @date 2024/2/2 10:33
 */
public class NameAliasDto {
    /**
     * 原字段名或者原表名
     */
    private String name;
    /**
     * 别名
     */
    private String alias;

    //------------------------lombok分割线（尽量少引依赖的原则，项目不引入lombok，下面拷贝lombok编译后的结果）---------------------------------------------------------
    public static NameAliasDtoBuilder builder() {
        return new NameAliasDtoBuilder();
    }

    public String getName() {
        return this.name;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof NameAliasDto)) {
            return false;
        } else {
            NameAliasDto other = (NameAliasDto) o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$name = this.getName();
                Object other$name = other.getName();
                if (this$name == null) {
                    if (other$name != null) {
                        return false;
                    }
                } else if (!this$name.equals(other$name)) {
                    return false;
                }

                Object this$alias = this.getAlias();
                Object other$alias = other.getAlias();
                if (this$alias == null) {
                    if (other$alias != null) {
                        return false;
                    }
                } else if (!this$alias.equals(other$alias)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof NameAliasDto;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, alias);
    }

    @Override
    public String toString() {
        return "NameAliasDto(name=" + this.getName() + ", alias=" + this.getAlias() + ")";
    }

    public NameAliasDto(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public NameAliasDto() {
    }

    public static class NameAliasDtoBuilder {
        private String name;
        private String alias;

        NameAliasDtoBuilder() {
        }

        public NameAliasDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public NameAliasDtoBuilder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public NameAliasDto build() {
            return new NameAliasDto(this.name, this.alias);
        }

        @Override
        public String toString() {
            return "NameAliasDto.NameAliasDtoBuilder(name=" + this.name + ", alias=" + this.alias + ")";
        }
    }
}
