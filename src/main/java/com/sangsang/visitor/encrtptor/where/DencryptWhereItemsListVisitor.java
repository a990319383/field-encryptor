package com.sangsang.visitor.encrtptor.where;

import com.sangsang.cache.FieldEncryptorPatternCache;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 针对where条件 in (xxx,xxx,xxx) 这种语法，对 xxx进行加密
 *
 * @author liutangqi
 * @date 2024/4/12 15:00
 */
public class DencryptWhereItemsListVisitor extends BaseFieldParseTable implements ItemsListVisitor {

    public DencryptWhereItemsListVisitor(int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
    }

    @Override
    public void visit(SubSelect subSelect) {
        //目前暂未发现走这里的语法
    }

    @Override
    public void visit(ExpressionList expressionList) {
        List<Expression> expressions = expressionList.getExpressions()
                .stream()
                .map(m -> FieldEncryptorPatternCache.getInstance().encryption(m))
                .collect(Collectors.toList());
        expressionList.setExpressions(expressions);
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        //目前暂未发现走这里的语法
    }

    @Override
    public void visit(MultiExpressionList multiExpressionList) {
        //目前暂未发现走这里的语法
    }
}
