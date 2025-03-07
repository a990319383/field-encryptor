package com.sangsang.domain.enums;

import com.sangsang.domain.function.EncryptorFunction;
import com.sangsang.domain.function.EncryptorFunctionScene;

/**
 * @author liutangqi
 * @date 2025/3/3 10:18
 */
public enum EncryptorFunctionEnum {

    DEFAULT_ENCRYPTION("默认加密", EncryptorFunctionScene.defaultEncryption()),
    DEFAULT_DECRYPTION("默认解密", EncryptorFunctionScene.defaultDecryption()),
    UPSTREAM_SECRET("上游密文", EncryptorFunctionScene.upstreamSecret()),
    UPSTREAM_PLAINTEXT("上游明文", EncryptorFunctionScene.upstreamPlaintext()),

    ;
    /**
     * 描述
     */
    private String desc;

    /**
     * 具体加解密执行的逻辑
     */
    private EncryptorFunction fun;

    EncryptorFunctionEnum(String desc, EncryptorFunction fun) {
        this.desc = desc;
        this.fun = fun;
    }

    public String getDesc() {
        return desc;
    }

    public EncryptorFunction getFun() {
        return fun;
    }}
