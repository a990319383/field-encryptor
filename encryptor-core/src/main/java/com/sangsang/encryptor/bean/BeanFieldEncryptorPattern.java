package com.sangsang.encryptor.bean;

/**
 * 加解密java bean的加解密算法
 *
 * @author liutangqi
 * @date 2024/7/24 17:38
 */
public interface BeanFieldEncryptorPattern {
    /**
     * 加密算法
     *
     * @author liutangqi
     * @date 2024/7/24 17:39
     * @Param [cleartext]
     **/
    String encryption(String cleartext);

    /**
     * 解密算法
     *
     * @author liutangqi
     * @date 2024/7/24 17:40
     * @Param [ciphertext]
     **/
    String decryption(String ciphertext);
}
