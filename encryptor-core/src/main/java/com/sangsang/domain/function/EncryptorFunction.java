package com.sangsang.domain.function;

import com.sangsang.domain.enums.EncryptorEnum;

/**
 * @author liutangqi
 * @date 2025/2/28 22:39
 */
@FunctionalInterface
public interface EncryptorFunction {
    /**
     * 根据当前列是否需要加解密返回 当前整体情况是否需要加解密
     *
     * @author liutangqi
     * @date 2025/2/28 22:41
     * @Param [curColumnEncrypt :true 需要加解密  false:不需要加解密]
     **/
    EncryptorEnum dispose(boolean curColumnEncrypt);
}
