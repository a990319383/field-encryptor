package com.sangsang.domain.constants;

/**
 * 符号相关常量
 *
 * @author liutangqi
 * @date 2023/12/21 16:52
 */
public interface SymbolConstant {
    /**
     * 句号
     */
    String FULL_STOP = ".";
    /**
     * 单引号
     */
    String SINGLE_QUOTES = "'";
    /**
     * 双引号
     */
    String DOUBLE_QUOTES = "\"";

    /**
     * test as 取别名
     */
    String AS = " as ";

    /**
     * 默认秘钥
     */
    String DEFAULT_SECRET_KEY = "7uq?q8g3@q";

    /**
     * mysql AES 加密函数
     */
    String AES_ENCRYPT = "AES_ENCRYPT";

    /**
     * mysql AES 解密函数
     */
    String AES_DECRYPT = "AES_DECRYPT";

    /**
     * mysql 转base64
     */
    String TO_BASE64 = "TO_BASE64";

    /**
     * 从base64转码
     */
    String FROM_BASE64 = "FROM_BASE64";
}
