package com.sangsang.encryptor;

import com.sangsang.domain.constants.PatternTypeConstant;
import com.sangsang.domain.constants.SymbolConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
     * 加解密的模式类型，默认使用数据库的库函数进行加解密
     *
     * @see PatternTypeConstant
     */
    private String patternType;

    /**
     * pojo模式下，是否支持同一#{}入参，拥有不同的值
     * 栗子： select * from tb_user where phone = #{ph} and encrypt_phone = #{ph} 其中 phone 和 encrypt_phone 两个字段加密算法不同，或者一个加密，一个不加密
     * 如果需要兼容上述场景的话，则将此配置设置为true
     * 注意：设置为true会在拦截器中将parameterMappings 里面的字段名进行修改，如果其它拦截器有对这个进行应用的话，可能会导致不兼容
     */
    private boolean pojoReplaceParameterMapping = false;


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

    public boolean isPojoReplaceParameterMapping() {
        return pojoReplaceParameterMapping;
    }

    public void setPojoReplaceParameterMapping(boolean pojoReplaceParameterMapping) {
        this.pojoReplaceParameterMapping = pojoReplaceParameterMapping;
    }
}
