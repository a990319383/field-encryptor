package com.sangsang.config.properties;

import com.sangsang.domain.constants.EncryptorPatternTypeConstant;
import com.sangsang.domain.constants.SymbolConstant;

/**
 * @author liutangqi
 * @date 2024/4/8 15:22
 */
public class EncryptorProperties {
    /**
     * 秘钥，下面是默认值
     */
    private String secretKey = SymbolConstant.DEFAULT_SECRET_KEY;

    /**
     * 加解密的模式类型
     *
     * @see EncryptorPatternTypeConstant
     */
    private String patternType;

    /**
     * 是否开启字段脱敏功能
     * 默认关闭
     */
    private boolean fieldDesensitize = false;


    public boolean isFieldDesensitize() {
        return fieldDesensitize;
    }

    public void setFieldDesensitize(boolean fieldDesensitize) {
        this.fieldDesensitize = fieldDesensitize;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getPatternType() {
        return patternType;
    }

    public void setPatternType(String patternType) {
        this.patternType = patternType;
    }

}
