package com.sangsang.visitor.beanencrtptor.select;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.sangsang.domain.constants.NumberConstant;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.domain.dto.PlaceholderFieldParseTable;
import com.sangsang.util.JsqlparserUtil;
import com.sangsang.visitor.beanencrtptor.BeanEncrtptorStatementVisitor;
import com.sangsang.visitor.beanencrtptor.where.PlaceholderWhereExpressionVisitor;
import com.sangsang.visitor.encrtptor.fieldparse.FieldParseParseTableSelectVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.Collection;
import java.util.List;
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
        //1.获取select的每一项，将其中 select (select a from xxx) from 这种语法的#{}占位符进行解析
        PlaceholderSelectExpressionVisitor placeholderSelectExpressionVisitor = new PlaceholderSelectExpressionVisitor(this);
        plainSelect.getSelectItems()
                .stream()
                .forEach(f -> {
                    //因为每一项只有3种类型  (1)*   (2)别名.*  (3)xxx，只有第三种我们需要处理,所以这里直接类型判断，就不单独搞个访问者类了
                    if (f instanceof SelectExpressionItem) {
                        ((SelectExpressionItem) f).getExpression().accept(placeholderSelectExpressionVisitor);
                    }
                });


        //2.解析from 后面的 #{}占位符
        PlaceholderSelectFromItemVisitor placeholderSelectFromItemVisitor = new PlaceholderSelectFromItemVisitor(this);
        plainSelect.getFromItem().accept(placeholderSelectFromItemVisitor);

        //3.将where 条件中的#{} 占位符进行解析
        Expression where = plainSelect.getWhere();
        if (where != null) {
            PlaceholderWhereExpressionVisitor placeholderWhereExpressionVisitor = new PlaceholderWhereExpressionVisitor(this);
            where.accept(placeholderWhereExpressionVisitor);
        }

        //4.解析join on后面写死的#{}占位符
        List<Join> joins = plainSelect.getJoins();
        if (CollectionUtils.isNotEmpty(joins)) {
            for (Join join : joins) {
                for (Expression expression : join.getOnExpressions()) {
                    expression.accept(new PlaceholderWhereExpressionVisitor(this));
                }
            }
        }
    }

    /**
     * union  ，union all 语法，将每个sql分开解析，获取其中的#{}占位符
     *
     * @author liutangqi
     * @date 2024/7/17 17:26
     * @Param [setOperationList]
     **/
    @Override
    public void visit(SetOperationList setOperationList) {
        List<SelectBody> selects = setOperationList.getSelects();
        for (SelectBody select : selects) {
            //单独解析这条sql
            FieldParseParseTableSelectVisitor fieldParseParseTableSelectVisitor = new FieldParseParseTableSelectVisitor(NumberConstant.ONE, null, null);
            select.accept(fieldParseParseTableSelectVisitor);

            //用解析后的结果，去解析#{}占位符  (字段所属信息从上面解析结果中取，存放占位符的解析结果的Map用当前的 )
            PlaceholderSelectVisitor placeholderSelectVisitor = new PlaceholderSelectVisitor(fieldParseParseTableSelectVisitor, this.getPlaceholderColumnTableMap());
            select.accept(placeholderSelectVisitor);

        }
    }

    @Override
    public void visit(WithItem withItem) {

    }

    @Override
    public void visit(ValuesStatement valuesStatement) {

    }
}
