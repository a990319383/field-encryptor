package com.sangsang.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liutangqi
 * @date 2025/5/26 11:28
 */
@ConfigurationProperties(prefix = "field")
public class FieldProperties {
    /**
     * 扫描的实体类的包路径
     * 如需使用数据库加解密功能，sql语法转换功能，需要配置
     */
    private List<String> scanEntityPackage = new ArrayList<>();
    /**
     * 加解密相关的配置
     **/
    private EncryptorProperties encryptor;
    /**
     * 脱敏相关的配置
     **/
    private DesensitizeProperties desensitize;
    /**
     * sql语法转换相关的配置
     */
    private TransformationProperties transformation;


    public List<String> getScanEntityPackage() {
        return scanEntityPackage;
    }

    public void setScanEntityPackage(List<String> scanEntityPackage) {
        this.scanEntityPackage = scanEntityPackage;
    }

    public EncryptorProperties getEncryptor() {
        return encryptor;
    }

    public void setEncryptor(EncryptorProperties encryptor) {
        this.encryptor = encryptor;
    }

    public DesensitizeProperties getDesensitize() {
        return desensitize;
    }

    public void setDesensitize(DesensitizeProperties desensitize) {
        this.desensitize = desensitize;
    }

    public TransformationProperties getTransformation() {
        return transformation;
    }

    public void setTransformation(TransformationProperties transformation) {
        this.transformation = transformation;
    }
}
