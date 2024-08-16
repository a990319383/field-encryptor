package com.sangsang.visitor.dbencrtptor.insert;

import com.sangsang.cache.FieldEncryptorPatternCache;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.ArrayList;
import java.util.List;

/**
 * 对insert的每一列需要加密的字段进行加密处理
 *
 * @author liutangqi
 * @date 2024/3/8 14:47
 */
public class IDecryptItemsListVisitor implements ItemsListVisitor {

    //需要加密的字段的索引
    private List<String> needEncryptIndex;

    public IDecryptItemsListVisitor(List<String> needEncryptIndex) {
        this.needEncryptIndex = needEncryptIndex;
    }

    @Override
    public void visit(SubSelect subSelect) {
        System.out.println(subSelect);
    }

    /**
     * insert单个值
     *
     * @author liutangqi
     * @date 2024/3/8 14:52
     * @Param [expressionList]
     **/
    @Override
    public void visit(ExpressionList expressionList) {
        //原表达式
        List<Expression> expressions = expressionList.getExpressions();

        //处理后的表达式
        List<Expression> newExpressions = new ArrayList<>();
        for (int i = 0; i < expressions.size(); i++) {
            Expression expression = expressions.get(i);
            //当前字段需要加密，并且当前字段是String类型的，才进行加密处理
            if (needEncryptIndex.contains(String.valueOf(i))) {
                //将原表达式进行加密
                Expression toBase64Function = FieldEncryptorPatternCache.getInstance().encryption(expression);

                expression = toBase64Function;
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
        IDecryptItemsListVisitor iDecryptItemsListVisitor = new IDecryptItemsListVisitor(this.needEncryptIndex);

        List<ExpressionList> exprList = multiExprList.getExprList();
        for (int i = 0; i < exprList.size(); i++) {
            exprList.get(i).accept(iDecryptItemsListVisitor);
        }
    }
}
