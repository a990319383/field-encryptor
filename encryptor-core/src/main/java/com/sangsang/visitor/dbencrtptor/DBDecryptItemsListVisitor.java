package com.sangsang.visitor.dbencrtptor;

import com.sangsang.cache.FieldEncryptorPatternCache;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 对insert的每一列需要加密的字段进行加密处理
 *
 * @author liutangqi
 * @date 2024/3/8 14:47
 */
public class DBDecryptItemsListVisitor implements ItemsListVisitor {
    /**
     * 上游需要加密的字段的索引
     */
    private List<Integer> upstreamNeedEncryptIndex;

    /**
     * 上游指定是否统一使用加/解密算法
     * 如果同时和上面的upstreamNeedEncryptIndex指定时，这个优先级最高
     **/
    private Boolean upstreamNeedEncrypt;

    /**
     * 获取当层解析对象
     *
     * @author liutangqi
     * @date 2025/3/4 15:54
     * @Param [upstreamNeedEncryptIndex]
     **/
    public static DBDecryptItemsListVisitor newInstanceCurLayer(List<Integer> upstreamNeedEncryptIndex) {
        return new DBDecryptItemsListVisitor(Optional.ofNullable(upstreamNeedEncryptIndex).orElse(new ArrayList<>()), null);
    }

    /**
     * 获取当层解析对象
     *
     * @author liutangqi
     * @date 2025/3/4 15:54
     * @Param [upstreamNeedEncryptIndex]
     **/
    public static DBDecryptItemsListVisitor newInstanceCurLayer(Boolean upstreamNeedEncrypt) {
        return new DBDecryptItemsListVisitor(new ArrayList<>(), upstreamNeedEncrypt);
    }

    private DBDecryptItemsListVisitor(List<Integer> upstreamNeedEncryptIndex, Boolean upstreamNeedEncrypt) {
        this.upstreamNeedEncryptIndex = upstreamNeedEncryptIndex;
        this.upstreamNeedEncrypt = upstreamNeedEncrypt;
    }

    @Override
    public void visit(SubSelect subSelect) {
    }

    /**
     * insert单个值
     * 这里全部都是常量
     * 根据上游是根据索引下标有不同的明密文存储情况，还是统一全部都是明/密文存储来决定当前字段的存储情况
     *
     * @author liutangqi
     * @date 2024/3/8 14:52
     * @Param [expressionList]
     **/
    @Override
    public void visit(ExpressionList expressionList) {
        List<Expression> expressions = expressionList.getExpressions();
        List<Expression> newExpressions = new ArrayList<>();

        //根据上游字段的明/密文情况，依次处理当前每一项
        for (int i = 0; i < expressions.size(); i++) {
            Expression expression = expressions.get(i);
            //1.上游指定了统一的加解密处理方式的话，采用上游指定的
            if (upstreamNeedEncrypt != null && upstreamNeedEncrypt) {
                //这里都是常量，上游字段需要加密时才处理
                expression = FieldEncryptorPatternCache.getInstance().encryption(expression);
            }

            //2.上游没有指定统一的加解密处理方式，根据上游传的需要密文存储的索引来判断当前是否需要加密（这里都是明文常量，只会加密）
            if (upstreamNeedEncrypt == null && upstreamNeedEncryptIndex.contains(i)) {
                //将原表达式进行加密
                expression = FieldEncryptorPatternCache.getInstance().encryption(expression);
            }
            newExpressions.add(expression);
        }

        //替换原有表达式
        expressionList.setExpressions(newExpressions);
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        System.out.println(namedExpressionList);
    }

    /**
     * insert多个值
     *
     * @author liutangqi
     * @date 2024/3/8 14:51
     * @Param [multiExprList]
     **/
    @Override
    public void visit(MultiExpressionList multiExprList) {
        DBDecryptItemsListVisitor DBDecryptItemsListVisitor = new DBDecryptItemsListVisitor(this.upstreamNeedEncryptIndex, this.upstreamNeedEncrypt);

        List<ExpressionList> exprList = multiExprList.getExprList();
        for (int i = 0; i < exprList.size(); i++) {
            exprList.get(i).accept(DBDecryptItemsListVisitor);
        }
    }
}
