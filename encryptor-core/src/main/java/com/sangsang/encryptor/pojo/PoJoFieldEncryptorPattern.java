package com.sangsang.encryptor.pojo;

import com.sangsang.domain.enums.PoJoAlgorithmEnum;

/**
 * 对pojo加解密的算法
 *
 * @author liutangqi
 * @date 2024/7/24 17:38
 */
public interface PoJoFieldEncryptorPattern {

    /**
     * 当前算法对应的枚举类型
     *
     * @author liutangqi
     * @date 2024/9/18 13:20
     * @Param []
     **/
    PoJoAlgorithmEnum encryptorAlgorithm();

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
