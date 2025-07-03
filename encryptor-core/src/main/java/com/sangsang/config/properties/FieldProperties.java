package com.sangsang.config.properties;

import com.sangsang.domain.constants.NumberConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;


/**
 * @author liutangqi
 * @date 2025/5/26 11:28
 */
@Data
@ConfigurationProperties(prefix = "field")
public class FieldProperties {
    /**
     * 扫描的实体类的包路径
     * 如需使用数据库加解密功能，sql语法转换功能，需要配置
     */
    private List<String> scanEntityPackage = new ArrayList<>();
    /**
     * sql语法解析的LRU缓存长度
     * 默认100
     */
    private Integer lruCapacity = NumberConstant.HUNDRED;
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

    /**
     * 数据隔离的相关配置
     **/
    private IsolationProperties isolation;
}
