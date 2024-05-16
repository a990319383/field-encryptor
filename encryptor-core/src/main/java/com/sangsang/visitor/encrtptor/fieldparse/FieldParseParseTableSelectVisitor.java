package com.sangsang.visitor.encrtptor.fieldparse;

import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.*;

/**
 * select 语句解析每一层拥有的表和拥有的全部字段解析入口
 *
 * @author liutangqi
 * @date 2024/3/4 10:32
 */
public class FieldParseParseTableSelectVisitor extends BaseFieldParseTable implements SelectVisitor {


    public FieldParseParseTableSelectVisitor(int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        // from 的表
        FromItem fromItem = plainSelect.getFromItem();
        FieldParseParseTableFromItemVisitor fieldParseTableFromItemVisitor = new FieldParseParseTableFromItemVisitor(this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        fromItem.accept(fieldParseTableFromItemVisitor);

        //join 的表
        List<Join> joins = Optional.ofNullable(plainSelect.getJoins()).orElse(new ArrayList<>());
        for (Join join : joins) {
            FromItem rightItem = join.getRightItem();
            FieldParseParseTableFromItemVisitor joinFieldTableFromItemVisitor = new FieldParseParseTableFromItemVisitor(this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
            rightItem.accept(joinFieldTableFromItemVisitor);
        }

        //查询的全部字段
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        for (SelectItem selectItem : selectItems) {
            FieldParseParseSelectItemVisitor fieldParseSelectItemVisitor = new FieldParseParseSelectItemVisitor(this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
            selectItem.accept(fieldParseSelectItemVisitor);
        }
    }

    /**
     * union all
     * union
     * 解析字段是为了字段加密服务的，  这里应该没必要解析，所以这里注释掉   union的字段加密，都是将每条sql单独解析的，所以这里不用处理
     *
     * @author liutangqi
     * @date 2024/3/6 13:58
     * @Param [setOpList]
     **/
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
