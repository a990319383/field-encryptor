package com.sangsang.domain.function;

import com.sangsang.domain.enums.EncryptorEnum;

/**
 * 根据每个表达式是明文还是密文存储来判断是调用加密还是解密方法的实现
 *
 * @author liutangqi
 * @date 2025/2/28 22:46
 */
public class EncryptorFunctionScene {

    /**
     * 默认的加密情况
     * 当前列需要密文存储：使用加密算法
     * 当前列不需要密文存储：不处理
     *
     * @author liutangqi
     * @date 2025/2/28 22:49
     * @Param []
     **/
    public static final EncryptorFunction defaultEncryption() {
        return e -> e ? EncryptorEnum.ENCRYPTION : EncryptorEnum.WITHOUT;
    }

    /**
     * 默认的解密情况
     * 当前列需要密文存储：使用解密算法
     * 当前列不需要密文存储：不处理
     *
     * @author liutangqi
     * @date 2025/2/28 22:50
     * @Param []
     **/
    public static final EncryptorFunction defaultDecryption() {
        return d -> d ? EncryptorEnum.DECRYPTION : EncryptorEnum.WITHOUT;
    }


    /**
     * insert(select) 语句中insert需要密文存储的情况
     * select的语句中：
     * - 密文存储 ：不需要处理
     * - 明文存储： 加密处理
     *
     * @author liutangqi
     * @date 2025/2/28 22:52
     * @Param []
     **/
    public static final EncryptorFunction insertSecret() {
        return i -> i ? EncryptorEnum.WITHOUT : EncryptorEnum.ENCRYPTION;
    }

    /**
     * insert(select) 语句中insert不需要密文存储的情况
     * select的语句中：
     * - 密文存储 ：解密处理
     * - 明文存储： 加密处理
     *
     * @author liutangqi
     * @date 2025/2/28 22:58
     * @Param []
     **/
    public static final EncryptorFunction insertPlaintext() {
        return i -> i ? EncryptorEnum.DECRYPTION : EncryptorEnum.WITHOUT;
    }
}
