package com.sangsang.domain.strategy.encryptor;

/**
 * 对pojo加解密的算法
 *
 * @author liutangqi
 * @date 2024/7/24 17:38
 */
public interface PoJoFieldEncryptorStrategy {
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
