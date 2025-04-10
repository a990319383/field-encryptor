package com.sangsang.encryptor;

import com.sangsang.domain.constants.PatternTypeConstant;
import com.sangsang.domain.constants.SymbolConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author liutangqi
 * @date 2024/4/8 15:22
 */
@ConfigurationProperties(prefix = "field.encryptor")
public class EncryptorProperties {
    /**
     * 秘钥，下面是默认值
     */
    private String secretKey = SymbolConstant.DEFAULT_SECRET_KEY;

    /**
     * 扫描的实体类的包路径
     * 如果没有配置此路径的话，就默认取当前myabtis-plus加载的所有实体类
     */
    private List<String> scanEntityPackage;

    /**
     * 加解密的模式类型
     *
     * @see PatternTypeConstant
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

    public List<String> getScanEntityPackage() {
        return scanEntityPackage;
    }

    public void setScanEntityPackage(List<String> scanEntityPackage) {
        this.scanEntityPackage = scanEntityPackage;
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
