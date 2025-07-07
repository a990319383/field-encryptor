package com.sangsang.config.properties;

import lombok.Data;

/**
 * 数据隔离的配置
 *
 * @author liutangqi
 * @date 2025/6/13 10:16
 */
@Data
public class IsolationProperties {
    /**
     * 是否开启数据隔离功能
     */
    private boolean enable = false;
}
