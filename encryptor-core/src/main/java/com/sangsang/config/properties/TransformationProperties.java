package com.sangsang.config.properties;

import com.sangsang.domain.constants.TransformationPatternTypeConstant;

/**
 * @author liutangqi
 * @date 2025/5/21 16:13
 */
public class TransformationProperties {
    /**
     * 当前转换类型
     * 注意：做扩展时，这个命名必须和com.sangsang.transformation这个路径的下一级包名一致
     * 例如：想实现mysql2oracle的扩展，则在com.sangsang.transformation这个路径的下一级建立一个mysql2oracle包，然后在这个路径下实现所有转换器
     *
     * @see TransformationPatternTypeConstant
     */
    private String patternType;

    public String getPatternType() {
        return patternType;
    }

    public void setPatternType(String patternType) {
        this.patternType = patternType;
    }
}
