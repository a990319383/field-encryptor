package com.sangsang.visitor.pojoencrtptor;

import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.PlaceholderFieldParseTable;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.Map;

/**
 * 解析from 后面的 #{}占位符
 *
 * @author liutangqi
 * @date 2024/7/12 17:04
 */
public class PlaceholderSelectFromItemVisitor extends PlaceholderFieldParseTable implements FromItemVisitor {

    public PlaceholderSelectFromItemVisitor(BaseFieldParseTable baseFieldParseTable, Map<String, ColumnTableDto> placeholderColumnTableMap) {
        super(baseFieldParseTable, placeholderColumnTableMap);
    }

    public PlaceholderSelectFromItemVisitor(PlaceholderFieldParseTable placeholderFieldParseTable) {
        super(placeholderFieldParseTable, placeholderFieldParseTable.getPlaceholderColumnTableMap());
    }

    @Override
    public void visit(Table table) {

    }

    /**
     * 子查询
     *
     * @author liutangqi
     * @date 2024/7/12 17:09
     * @Param [subSelect]
     **/
    @Override
    public void visit(SubSelect subSelect) {
        PlaceholderSelectVisitor placeholderSelectVisitor = PlaceholderSelectVisitor.newInstanceNextLayer(this);
        subSelect.getSelectBody().accept(placeholderSelectVisitor);
    }

    @Override
    public void visit(SubJoin subJoin) {

    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {

    }

    @Override
    public void visit(ValuesList valuesList) {

    }

    @Override
    public void visit(TableFunction tableFunction) {

    }

    @Override
    public void visit(ParenthesisFromItem parenthesisFromItem) {

    }
}
