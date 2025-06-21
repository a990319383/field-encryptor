package com.sangsang.isolation;

import com.sangsang.domain.interfaces.DataIsolationInterface;

/**
 * 测试时获取当前数据隔离的值
 *
 * @author liutangqi
 * @date 2025/6/13 15:12
 */
public class TestDataIsolation implements DataIsolationInterface {

    @Override
    public String getIsolationData() {
        return "1111111111111111";
    }

}
