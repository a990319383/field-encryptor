package com.sangsang.encryptor;

import com.sangsang.domain.constants.SymbolConstant;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;


/**
 * 默认的加解密算法
 *
 * @author liutangqi
 * @date 2024/4/8 14:13
 */
@Component
@ConditionalOnMissingBean(FieldEncryptorPattern.class)
public class DefaultFieldEncryptorPattern implements FieldEncryptorPattern {

    //加密秘钥
    @Autowired
    private EncryptorProperties encryptorProperties;

    public DefaultFieldEncryptorPattern(EncryptorProperties encryptorProperties) {
        this.encryptorProperties = encryptorProperties;
    }

    @Override
    public Expression encryption(Expression oldExpression) {
        Function aesEncryptFunction = new Function();
        aesEncryptFunction.setName(SymbolConstant.AES_ENCRYPT);
        aesEncryptFunction.setParameters(new ExpressionList(oldExpression, new StringValue(encryptorProperties.getSecretKey())));
        Function toBase64Function = new Function();
        toBase64Function.setName(SymbolConstant.TO_BASE64);
        toBase64Function.setParameters(new ExpressionList(aesEncryptFunction));
        return toBase64Function;
    }

    @Override
    public Expression decryption(Expression oldExpression) {
        Function base64Function = new Function();
        base64Function.setName(SymbolConstant.FROM_BASE64);
        base64Function.setParameters(new ExpressionList(oldExpression));
        Function decryptFunction = new Function();
        decryptFunction.setName(SymbolConstant.AES_DECRYPT);
        decryptFunction.setParameters(new ExpressionList(base64Function, new StringValue(encryptorProperties.getSecretKey())));

        //类型转换，避免上面解密函数出现中文乱码
        CastExpression castExpression = new CastExpression();
        castExpression.setLeftExpression(decryptFunction);
        castExpression.setType(SymbolConstant.COLDATATYPE_HCAR);
        return castExpression;
    }
}
