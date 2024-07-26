package com.sangsang.encryptor.bean;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * java bean 加密方式下的默认加解密算法
 *
 * @author liutangqi
 * @date 2024/7/24 17:41
 */
@Component
@ConditionalOnMissingBean(BeanFieldEncryptorPattern.class)
public class DefaultBeanFieldEncryptorPattern implements BeanFieldEncryptorPattern {

    @Override
    public String encryption(String cleartext) {
        return cleartext + "加密";
    }

    @Override
    public String decryption(String ciphertext) {
        return ciphertext.replaceAll("加密", "");
    }
}