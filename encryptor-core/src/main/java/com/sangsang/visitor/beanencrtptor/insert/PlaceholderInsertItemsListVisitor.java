package com.sangsang.visitor.beanencrtptor.insert;

import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.domain.dto.PlaceholderFieldParseTable;
import com.sangsang.util.JsqlparserUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 处理Insert语句中占位符#{} 对应的表字段信息
 *
 * @author liutangqi
 * @date 2024/8/14 13:23
 */
public class PlaceholderInsertItemsListVisitor extends PlaceholderFieldParseTable implements ItemsListVisitor {

    /**
     * 当前insert语句的所有字段
     */
    private List<Column> columns;

    public PlaceholderInsertItemsListVisitor(List<Column> columns, int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap, Map<String, ColumnTableDto> placeholderColumnTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap, placeholderColumnTableMap);
        this.columns = columns;
    }

    @Override
    public void visit(SubSelect subSelect) {

    }

    /**
     * insert单个值
     *
     * @author liutangqi
     * @date 2024/8/14 15:14
     * @Param [expressionList]
     **/
    @Override
    public void visit(ExpressionList expressionList) {
        List<Expression> expressions = expressionList.getExpressions();
        for (int i = 0; i < expressions.size(); i++) {
            //处理#{}占位符对应的表字段
            JsqlparserUtil.parseWhereColumTable(
                    this.getLayer(),
                    this.getLayerFieldTableMap(),
                    expressions.get(i),
                    this.columns.get(i),
                    this.getPlaceholderColumnTableMap()
            );
        }
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {

    }

    /**
     * insert多个值
     *
     * @author liutangqi
     * @date 2024/8/14 15:58
     * @Param [multiExprList]
     **/
    @Override
    public void visit(MultiExpressionList multiExprList) {
        List<ExpressionList> exprList = multiExprList.getExprList();
        for (int i = 0; i < exprList.size(); i++) {
            exprList.get(i).accept(this);
        }
    }


}
