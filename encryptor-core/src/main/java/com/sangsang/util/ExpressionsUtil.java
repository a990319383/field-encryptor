package com.sangsang.util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;

/**
 * 表达式工具类
 *
 * @author liutangqi
 * @date 2025/6/13 14:23
 */
public class ExpressionsUtil {

    /**
     * 构建and
     *
     * @author liutangqi
     * @date 2025/6/13 14:24
     * @Param [leftExpression, rightExpression]
     **/
    public static AndExpression buildAndExpression(Expression leftExpression, Expression rightExpression) {
        AndExpression andExpression = new AndExpression();
        andExpression.setLeftExpression(leftExpression);
        andExpression.setRightExpression(rightExpression);
        return andExpression;
    }

    /**
     * 构建括号
     *
     * @author liutangqi
     * @date 2025/6/13 16:29
     * @Param [expression]
     **/
    public static Parenthesis buildParenthesis(Expression expression) {
        Parenthesis parenthesis = new Parenthesis();
        parenthesis.setExpression(expression);
        return parenthesis;
    }
}
