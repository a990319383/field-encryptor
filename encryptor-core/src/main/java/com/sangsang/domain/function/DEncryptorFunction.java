package com.sangsang.domain.function;

import net.sf.jsqlparser.expression.Expression;

/**
 * @author liutangqi
 * @date 2025/3/1 12:22
 */
@FunctionalInterface
public interface DEncryptorFunction {
    /**
     * 加/解密处理
     *
     * @author liutangqi
     * @date 2025/3/1 12:23
     * @Param [oldExpression]
     **/
    Expression dEcryp(Expression oldExpression);
}
