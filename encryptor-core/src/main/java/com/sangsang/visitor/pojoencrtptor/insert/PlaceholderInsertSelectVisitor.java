package com.sangsang.visitor.pojoencrtptor.insert;

import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.PlaceholderFieldParseTable;
import com.sangsang.visitor.pojoencrtptor.where.PlaceholderWhereExpressionVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.Map;

/**
 * 处理Insert 语句中 where 条件中的占位符信息
 *
 * @author liutangqi
 * @date 2024/8/14 16:01
 */
public class PlaceholderInsertSelectVisitor extends PlaceholderFieldParseTable implements SelectVisitor {

    public PlaceholderInsertSelectVisitor(BaseFieldParseTable baseFieldParseTable, Map<String, ColumnTableDto> placeholderColumnTableMap) {
        super(baseFieldParseTable, placeholderColumnTableMap);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        //只用处理where 条件即可
        Expression where = plainSelect.getWhere();
        if (where == null) {
            return;
        }

        //处理where条件的占位符信息
        PlaceholderWhereExpressionVisitor placeholderWhereExpressionVisitor = new PlaceholderWhereExpressionVisitor(this);
        where.accept(placeholderWhereExpressionVisitor);

    }

    @Override
    public void visit(SetOperationList setOpList) {

    }

    @Override
    public void visit(WithItem withItem) {

    }

    @Override
    public void visit(ValuesStatement aThis) {

    }
}
