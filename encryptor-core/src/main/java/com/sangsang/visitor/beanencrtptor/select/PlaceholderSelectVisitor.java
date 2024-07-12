package com.sangsang.visitor.beanencrtptor.select;

import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.domain.dto.PlaceholderFieldParseTable;
import com.sangsang.visitor.beanencrtptor.where.PlaceholderWhereExpressionVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.Map;

/**
 * 将select语句中的#{}占位符和数据库表字段对应起来
 *
 * @author liutangqi
 * @date 2024/7/12 10:34
 */
public class PlaceholderSelectVisitor extends PlaceholderFieldParseTable implements SelectVisitor {


    public PlaceholderSelectVisitor(PlaceholderFieldParseTable placeholderFieldParseTable) {
        super(placeholderFieldParseTable, placeholderFieldParseTable.getPlaceholderColumnTableMap());
    }

    public PlaceholderSelectVisitor(BaseFieldParseTable baseFieldParseTable, Map<String, ColumnTableDto> placeholderColumnTableMap) {
        super(baseFieldParseTable, placeholderColumnTableMap);
    }


    @Override
    public void visit(PlainSelect plainSelect) {
        //1.解析from 后面的 #{}占位符
        PlaceholderSelectFromItemVisitor placeholderSelectFromItemVisitor = new PlaceholderSelectFromItemVisitor(this);
        plainSelect.getFromItem().accept(placeholderSelectFromItemVisitor);

        //2.将where 条件中的#{} 占位符进行解析
        Expression where = plainSelect.getWhere();
        if (where == null) {
            return;
        }
        PlaceholderWhereExpressionVisitor placeholderWhereExpressionVisitor = new PlaceholderWhereExpressionVisitor(this);
        where.accept(placeholderWhereExpressionVisitor);
    }

    @Override
    public void visit(SetOperationList setOperationList) {

    }

    @Override
    public void visit(WithItem withItem) {

    }

    @Override
    public void visit(ValuesStatement valuesStatement) {

    }
}
