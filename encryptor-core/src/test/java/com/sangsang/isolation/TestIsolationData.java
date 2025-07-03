package com.sangsang.isolation;

import com.sangsang.domain.enums.IsolationRelationEnum;
import com.sangsang.domain.strategy.isolation.IsolationDataStrategy;

import java.util.Arrays;
import java.util.List;

/**
 * 测试时获取当前数据隔离的值
 *
 * @author liutangqi
 * @date 2025/6/13 15:12
 */
public class TestIsolationData implements IsolationDataStrategy<List<String>> {

    @Override
    public String getIsolationField() {
        return "org_seq";
    }

    @Override
    public IsolationRelationEnum getIsolationRelation() {
        return IsolationRelationEnum.IN;
    }

    @Override
    public List<String> getIsolationData() {
        return Arrays.asList("1111111111111111", "222222222", "3333333333");
    }

}
