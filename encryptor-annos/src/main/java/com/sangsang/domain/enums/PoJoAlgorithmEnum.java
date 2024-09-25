package com.sangsang.domain.enums;

/**
 * pojo模式下的加解密算法
 * 每个枚举最多只能匹配一种算法
 * 如果要自定义算法的话，ALGORITHM_DEFAULT这个算法必须有
 *
 * @author liutangqi
 * @date 2024/9/18 13:06
 */
public enum PoJoAlgorithmEnum {
    ALGORITHM_DEFAULT(true, "默认的加解密算法，整个项目中，有且只能配置一个"),
    ALGORITHM_1(false, "算法1"),
    ALGORITHM_2(false, "算法2"),
    ALGORITHM_3(false, "算法3"),
    ALGORITHM_4(false, "算法4"),
    ALGORITHM_5(false, "算法5"),
    ;


    /**
     * 是否是默认的加解密算法
     * 整个项目spring的bean中，默认的加解密算法只能有一个
     */
    private boolean defaultAlgorithm;

    /**
     * 描述
     */
    private String desc;

    PoJoAlgorithmEnum(boolean defaultAlgorithm, String desc) {
        this.defaultAlgorithm = defaultAlgorithm;
        this.desc = desc;
    }

    public boolean isDefaultAlgorithm() {
        return defaultAlgorithm;
    }

    public String getDesc() {
        return desc;
    }}
