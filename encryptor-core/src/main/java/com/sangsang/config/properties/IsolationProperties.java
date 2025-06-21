package com.sangsang.config.properties;

import com.sangsang.domain.enums.IsolationRelationEnum;

/**
 * 数据隔离的配置
 *
 * @author liutangqi
 * @date 2025/6/13 10:16
 */
public class IsolationProperties {
    /**
     * 数据隔离的表字段
     * 必须有值
     **/
    private String field;
    /**
     * 表字段和具体值之间的关系
     * IsolationRelationEnum 的 code
     * 默认是 =
     * 注意：不能使用IsolationRelationEnum.EMPTY
     **/
    private String relation = IsolationRelationEnum.EQUALS.getCode();

    /**
     * 默认的获取数据隔离的值的实现类的全限定名
     * 实现DataIsolationInterface接口的类的全限定名
     * 必须有值
     */
    private String isolationClass;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getIsolationClass() {
        return isolationClass;
    }

    public void setIsolationClass(String isolationClass) {
        this.isolationClass = isolationClass;
    }
}
