package com.sangsang.encryptor;

import com.sangsang.domain.constants.SymbolConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author liutangqi
 * @date 2024/4/8 15:22
 */
@Component
@ConfigurationProperties(prefix = "field.encryptor")
public class EncryptorProperties {
    /**
     * 秘钥，下面是默认值
     */
    private String secretKey = SymbolConstant.DEFAULT_SECRET_KEY;


    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
