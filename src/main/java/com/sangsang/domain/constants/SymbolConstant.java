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
     * sql as 取别名
     */
    String AS = " as ";

    /**
     * mysql解密函数前缀
     */
    String DECODE = "AES_DECRYPT(FROM_BASE64(";

    /**
     * mysql 加密函数前缀
     */
    String ENCODE = "TO_BASE64(AES_ENCRYPT(";

    /**
     * mysql aes 加密函数
     */
    String AES_ENCRYPT = "AES_ENCRYPT";

    /**
     * mysql 转base64
     */
    String TO_BASE64 = "TO_BASE64";
}
