package com.sangsang.domain.dto;

import com.sangsang.domain.function.EncryptorFunction;

import java.util.Map;
import java.util.Set;

/**
 * 需要进行加解密函数调用的基类
 *
 * @author liutangqi
 * @date 2025/3/1 12:28
 */
public class BaseDEcryptParseTable extends BaseFieldParseTable {
    /**
     * 当前字段需要密文存储时，应该调用加密方法还是解密方法
     *
     * @author liutangqi
     * @date 2025/2/28 23:12
     * @Param
     **/
    private EncryptorFunction encryptorFunction;

    public BaseDEcryptParseTable(int layer,
                                 EncryptorFunction encryptorFunction,
                                 Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap,
                                 Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
        this.encryptorFunction = encryptorFunction;
    }

    public EncryptorFunction getEncryptorFunction() {
        return encryptorFunction;
    }
}
