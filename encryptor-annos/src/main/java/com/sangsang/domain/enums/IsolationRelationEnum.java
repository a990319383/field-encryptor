package com.sangsang.domain.enums;

import com.sangsang.domain.exception.IsolationException;

import java.util.HashMap;
import java.util.Map;

/**
 * 组织权限过滤的时候的关系
 *
 * @author liutangqi
 * @date 2025/6/12 17:10
 */
public enum IsolationRelationEnum {

    EMPTY("", "以配置文件配置的为准"),
    EQUALS("equals", "权限过滤时使用= 栗如: org_seq = 'xxx'"),
    LIKE_PREFIX("likePrefix", "权限过滤时使用前缀匹配 栗如: org_seq like 'xxx%'"),
    ;

    /**
     * 唯一的标识符
     * 配置文件就是通过这个code匹配具体的策略的
     */
    private String code;

    /**
     * 描述
     */
    private String desc;

    IsolationRelationEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    private static Map<String, IsolationRelationEnum> enumMap = new HashMap<>();

    static {
        for (IsolationRelationEnum isolationRelationEnum : IsolationRelationEnum.values()) {
            enumMap.put(isolationRelationEnum.getCode(), isolationRelationEnum);
        }
    }

    /**
     * 通过code获取整个枚举
     *
     * @author liutangqi
     * @date 2025/6/13 11:08
     * @Param [code]
     **/
    public static IsolationRelationEnum getByCode(String code) {
        IsolationRelationEnum isolationRelationEnum = enumMap.get(code);
        if (isolationRelationEnum == null) {
            throw new IsolationException(String.format("IsolationRelationEnum枚举值错误，错误code:%s", code));
        }
        return isolationRelationEnum;
    }

}
