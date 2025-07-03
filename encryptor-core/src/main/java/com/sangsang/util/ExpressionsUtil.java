package com.sangsang.util;

import com.sangsang.domain.exception.IsolationException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * 构建常量，仅支持String  Integer Long
     *
     * @author liutangqi
     * @date 2025/6/21 22:21
     * @Param [obj]
     **/
    public static Expression buildConstant(Object obj) {
        if (obj instanceof String) {
            return new StringValue((String) obj);
        }
        if (obj instanceof Integer) {
            return new LongValue((Integer) obj);
        }
        if (obj instanceof Long) {
            return new LongValue((Long) obj);
        }
        throw new IsolationException("不支持此类型");
    }

    /**
     * 构建表达式列表，仅支持String Integer Long
     *
     * @author liutangqi
     * @date 2025/6/21 22:25
     * @Param [objList]
     **/
    public static List<Expression> buildExpressionList(List<Object> objList) {
        List<Expression> res = new ArrayList<>();
        for (Object obj : objList) {
            res.add(buildConstant(obj));
        }
        return res;
    }
}
