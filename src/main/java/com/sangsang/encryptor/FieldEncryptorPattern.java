package com.sangsang.encryptor;

import net.sf.jsqlparser.expression.Expression;

/**
 * 加解密接口
 * 如果想要用其他数据库的加解密函数的话，自己实现这个接口，做扩展
 *
 * @author liutangqi
 * @date 2024/4/8 14:09
 */
public interface FieldEncryptorPattern {

    /**
     * 加密算法
     *
     * @author liutangqi
     * @date 2024/4/8 14:12
     * @Param [oldExpression]
     **/
    Expression encryption(Expression oldExpression);

    /**
     * 解密算法
     *
     * @author liutangqi
     * @date 2024/4/8 14:13
     * @Param [oldExpression]
     **/
    Expression decryption(Expression oldExpression);
}
