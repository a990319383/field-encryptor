package com.sangsang.visitor.fieldparse;

import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.util.JsqlparserUtil;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 维护当前层所有的查询字段
 *
 * @author liutangqi
 * @date 2024/3/4 11:20
 */
public class FieldParseParseSelectItemVisitor extends BaseFieldParseTable implements SelectItemVisitor {


    public FieldParseParseSelectItemVisitor(int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
    }


    /**
     * select *
     * 当没有别名直接* 的时候，此时同层肯定只有一张表，找同层的表的全部字段，作为查询的全部字段
     *
     * @author liutangqi
     * @date 2024/3/5 11:01
     * @Param [allColumns]
     **/
    @Override
    public void visit(AllColumns allColumns) {
        //本层的全部字段
        Map<String, Set<FieldInfoDto>> fieldMap = Optional.ofNullable(this.getLayerFieldTableMap().get(String.valueOf(this.getLayer()))).orElse(new HashMap<>());

        //将本层全部字段放到 select的map中
        for (Map.Entry<String, Set<FieldInfoDto>> fieldInfoEntry : fieldMap.entrySet()) {
            JsqlparserUtil.putFieldInfo(this.getLayerSelectTableFieldMap(), this.getLayer(), fieldInfoEntry.getKey(), fieldInfoEntry.getValue());
        }

    }

    /**
     * select 别名.*
     * 从本层中找到别名的这张表的全部字段
     *
     * @author liutangqi
     * @date 2024/3/5 11:01
     * @Param [allTableColumns]
     **/
    @Override
    public void visit(AllTableColumns allTableColumns) {
        //获取本层涉及到的表的全部字段
        Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap = this.getLayerFieldTableMap();
        Map<String, Set<FieldInfoDto>> fieldTableMap = Optional.ofNullable(layerFieldTableMap.get(String.valueOf(this.getLayer()))).orElse(new HashMap<>());

        //获取其中叫这个别名的全部字段
        String tableName = allTableColumns.getTable().getName().toLowerCase();
        Map<String, Set<FieldInfoDto>> fieldMap = fieldTableMap.entrySet().stream()
                .filter(f -> Objects.equals(f.getKey(), tableName))
                .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));

        //将本层全部字段放到 select的map中
        for (Map.Entry<String, Set<FieldInfoDto>> fieldInfoEntry : fieldMap.entrySet()) {
            JsqlparserUtil.putFieldInfo(this.getLayerSelectTableFieldMap(), this.getLayer(), fieldInfoEntry.getKey(), fieldInfoEntry.getValue());
        }
    }

    /**
     * select 具体的字段
     *
     * @author liutangqi
     * @date 2024/3/5 11:01
     * @Param [selectExpressionItem]
     **/
    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        Expression expression = selectExpressionItem.getExpression();

        Alias alias = selectExpressionItem.getAlias();
        FieldParseParseExpressionVisitor fieldParseExpressionVisitor = new FieldParseParseExpressionVisitor(alias, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        expression.accept(fieldParseExpressionVisitor);

    }
}
