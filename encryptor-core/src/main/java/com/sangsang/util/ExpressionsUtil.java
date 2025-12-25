package com.sangsang.util;

import cn.hutool.core.date.DateUtil;
import com.sangsang.cache.fielddefault.FieldDefaultInstanceCache;
import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.domain.exception.FieldDefaultException;
import com.sangsang.domain.exception.IsolationException;
import com.sangsang.domain.strategy.fielddefault.FieldDefaultStrategy;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
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
     * 构建or表达式
     *
     * @author liutangqi
     * @date 2025/8/15 14:41
     * @Param [leftExpression, rightExpression]
     **/
    public static OrExpression buildOrExpression(Expression leftExpression, Expression rightExpression) {
        OrExpression orExpression = new OrExpression();
        orExpression.setLeftExpression(leftExpression);
        orExpression.setRightExpression(rightExpression);
        return orExpression;
    }

    /**
     * 多个表达式之间使用and连接起来
     *
     * @author liutangqi
     * @date 2025/8/15 17:32
     * @Param [expressions]
     **/
    public static Expression buildAndExpression(List<Expression> expressions) {
        if (CollectionUtils.isEmpty(expressions)) {
            return null;
        }
        if (expressions.size() == 1) {
            return expressions.get(0);
        }
        Expression preExp = expressions.get(0);
        for (int i = 1; i < expressions.size(); i++) {
            preExp = buildAndExpression(preExp, expressions.get(i));
        }
        return preExp;
    }

    /**
     * 多个表达式之间使用or连接起来
     * 注意：请根据使用场景，判断是否需要使用括号包裹起来
     *
     * @author liutangqi
     * @date 2025/8/15 17:32
     * @Param [expressions]
     **/
    public static Expression buildOrExpression(List<Expression> expressions) {
        if (CollectionUtils.isEmpty(expressions)) {
            return null;
        }
        if (expressions.size() == 1) {
            return expressions.get(0);
        }
        Expression preExp = expressions.get(0);
        for (int i = 1; i < expressions.size(); i++) {
            preExp = buildOrExpression(preExp, expressions.get(i));
        }
        return preExp;
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


    /**
     * 构建Column
     *
     * @author liutangqi
     * @date 2025/7/18 11:21
     * @Param [columnName, tableName]
     **/
    public static Column buildColumn(String columnName, String tableName) {
        Column column = new Column(columnName);
        if (StringUtils.isNotBlank(tableName)) {
            Table table = new Table(tableName);
            column.setTable(table);
        }
        return column;
    }


    /**
     * 根据获取字段默认值的算法构建返回值表达者
     *
     * @author liutangqi
     * @date 2025/7/18 15:00
     * @Param [clazz]
     **/
    public static Expression buildFieldDefaultExp(Class<? extends FieldDefaultStrategy> clazz) {
        FieldDefaultStrategy instance = FieldDefaultInstanceCache.getInstance(clazz);
        Object defaultValue = instance.getDefaultValue();
        //当前获取的默认值为null，则构建一个null
        if (defaultValue == null) {
            return new NullValue();
        }

        if (defaultValue instanceof String) {
            return new StringValue((String) defaultValue);
        }

        if (defaultValue instanceof Integer) {
            return new LongValue((Integer) defaultValue);
        }

        if (defaultValue instanceof Long) {
            return new LongValue((Long) defaultValue);
        }

        if (defaultValue instanceof Date) {
            String time = DateUtil.format((Date) defaultValue, SymbolConstant.DEFAULT_TIME_FORMAT);
            return new StringValue(time);
        }

        if (defaultValue instanceof LocalDateTime) {
            String time = DateUtil.format((LocalDateTime) defaultValue, SymbolConstant.DEFAULT_TIME_FORMAT);
            return new StringValue(time);
        }

        if (defaultValue instanceof LocalDate) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(SymbolConstant.DEFAULT_DAY_FORMAT);
            String strDate = ((LocalDate) defaultValue).format(dateFormatter);
            return new StringValue(strDate);
        }

        throw new FieldDefaultException(String.format("不支持的默认值类型 %s", defaultValue.getClass()));
    }

    /**
     * 构建单表达式的update语句
     *
     * @author liutangqi
     * @date 2025/7/18 15:18
     * @Param [column, expression]
     **/
    public static UpdateSet buildUpdateSet(Column column, Expression expression) {
        UpdateSet updateSet = new UpdateSet();
        updateSet.setColumns(new ExpressionList(column));
        updateSet.setValues(new ExpressionList(expression));
        return updateSet;
    }

    /**
     * 构建 if(condition,leftExp,rightExp) 函数
     *
     * @author liutangqi
     * @date 2025/7/21 9:47
     * @Param [leftExp, rightExp]
     **/
    public static Function buildIf(Expression condition, Expression leftExp, Expression rightExp) {
        Function function = new Function();
        function.setName("if");
        function.setParameters(condition, leftExp, rightExp);
        return function;
    }


    /**
     * 保留condition条件，构建出的结果值肯定是 exp
     * if(condition is null ,exp,exp)
     *
     * @author liutangqi
     * @date 2025/7/24 10:43
     * @Param [condition, exp]
     **/
    public static Function buildAffirmativeIf(Expression condition, Expression exp) {
        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(condition);
        return buildIf(isNullExpression, exp, exp);
    }


    /**
     * 构建in 表达式 ，注意：in 的后边是括号括起来的表达式集合
     *
     * @author liutangqi
     * @date 2025/9/29 10:51
     * @Param [column, valueExpression]
     **/
    public static InExpression buildInExpression(Column column, ParenthesedExpressionList valueExpression) {
        InExpression inExpression = new InExpression();
        inExpression.setLeftExpression(column);
        inExpression.setRightExpression(valueExpression);
        return inExpression;
    }

}
