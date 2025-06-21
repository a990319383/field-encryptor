package com.sangsang.domain.dto;

import com.sangsang.domain.enums.IsolationRelationEnum;
import com.sangsang.domain.interfaces.DataIsolationInterface;

/**
 * @author liutangqi
 * @date 2025/6/13 15:40
 */
public class DataIsolationDto {
    /**
     * 用于数据隔离的字段
     **/
    private String field;

    /**
     * 隔离字段与具体值之间的关系
     **/
    private IsolationRelationEnum relation;

    /**
     * 获取当前用户隔离的值的具体方法的全限定名
     **/
    private String isolationClass;


    public DataIsolationDto(String field, IsolationRelationEnum relation, String isolationClass) {
        this.field = field;
        this.relation = relation;
        this.isolationClass = isolationClass;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public IsolationRelationEnum getRelation() {
        return relation;
    }

    public void setRelation(IsolationRelationEnum relation) {
        this.relation = relation;
    }

    public String getisolationClass() {
        return isolationClass;
    }

    public void setisolationClass(String isolationClass) {
        this.isolationClass = isolationClass;
    }
}
