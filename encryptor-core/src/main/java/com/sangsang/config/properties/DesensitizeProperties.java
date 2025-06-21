package com.sangsang.config.properties;

/**
 * 脱敏相关的配置
 *
 * @author liutangqi
 * @date 2025/5/26 11:30
 */
public class DesensitizeProperties {
    /**
     * 是否开启脱敏功能
     */
    private boolean enable = false;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
