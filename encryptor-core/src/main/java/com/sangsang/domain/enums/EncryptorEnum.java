package com.sangsang.domain.enums;

import com.sangsang.cache.FieldEncryptorPatternCache;
import com.sangsang.domain.function.DEncryptorFunction;

/**
 * 需要加密处理还是解密处理的枚举
 *
 * @author liutangqi
 * @date 2025/2/28 22:33
 */
public enum EncryptorEnum {

    ENCRYPTION("加密", (plainText) -> FieldEncryptorPatternCache.getInstance().encryption(plainText)),
    DECRYPTION("解密", (secret) -> FieldEncryptorPatternCache.getInstance().decryption(secret)),
    WITHOUT("不需要处理", (original) -> original),
    ;

    private String desc;

    /**
     * 具体处理的函数
     **/
    private DEncryptorFunction dEncryptorFunction;

    EncryptorEnum(String desc, DEncryptorFunction dEncryptorFunction) {
        this.desc = desc;
        this.dEncryptorFunction = dEncryptorFunction;
    }

    public DEncryptorFunction getdEncryptorFunction() {
        return dEncryptorFunction;
    }

    public String getDesc() {
        return desc;
    }
}
