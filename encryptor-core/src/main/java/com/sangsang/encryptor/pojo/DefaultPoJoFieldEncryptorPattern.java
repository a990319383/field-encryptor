package com.sangsang.encryptor.pojo;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.symmetric.DES;
import com.sangsang.domain.enums.PoJoAlgorithmEnum;
import com.sangsang.encryptor.EncryptorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * java pojo 加密方式下的默认加解密算法
 * 默认采用DES算法
 *
 * @author liutangqi
 * @date 2024/7/24 17:41
 */
public class DefaultPoJoFieldEncryptorPattern implements PoJoFieldEncryptorPattern {
    private static final Logger log = LoggerFactory.getLogger(DefaultPoJoFieldEncryptorPattern.class);

    private EncryptorProperties encryptorProperties;

    public DefaultPoJoFieldEncryptorPattern(EncryptorProperties encryptorProperties) {
        this.encryptorProperties = encryptorProperties;
    }

    private DES des;

    @Override
    public PoJoAlgorithmEnum encryptorAlgorithm() {
        return PoJoAlgorithmEnum.ALGORITHM_DEFAULT;
    }

    @Override
    public String encryption(String cleartext) {
        //注意：这里值处理null的情况，空字符串也需要进行加密处理
        if (cleartext == null) {
            return cleartext;
        }

        String ciphertext = cleartext;
        try {
            if (des == null) {
                des = new DES(encryptorProperties.getSecretKey().getBytes());
            }
            byte[] encryptBytes = des.encrypt(cleartext.getBytes());
            ciphertext = HexUtil.encodeHexStr(encryptBytes);
        } catch (Exception e) {
            log.error("【field-encryptor】pojo模式加密失败 cleartext:{}", cleartext, e);
        }
        return ciphertext;
    }

    @Override
    public String decryption(String ciphertext) {
        //注意：这里值处理null的情况，空字符串也需要进行加密处理
        if (ciphertext == null) {
            return ciphertext;
        }

        String cleartext = ciphertext;
        try {
            if (des == null) {
                des = new DES(encryptorProperties.getSecretKey().getBytes());
            }

            byte[] decryptBytes = des.decrypt(HexUtil.decodeHex(ciphertext));
            return new String(decryptBytes);
        } catch (Exception e) {
            log.error("【field-encryptor】pojo模式解密失败 ciphertext:{}", ciphertext, e);
        }
        return cleartext;
    }

}