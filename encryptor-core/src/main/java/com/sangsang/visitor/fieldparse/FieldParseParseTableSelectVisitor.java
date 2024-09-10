package com.sangsang.visitor.fieldparse;

import com.sangsang.util.CollectionUtils;
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
     * 在使用数据库本身的函数加解密的模式下，这种语法的解析没有必要，不会使用这个解析结果的，union的几条sql都是单独解析，进行加解密处理的
     * 在使用java pojo 进行加解密的模式下，我们需要知道每个字段对应的表，才知道是否需要加解密，这里只解析union的第一个sql,因为对于标准的数据安全的场景，只要这个字段是需要加密的，那这个字段涉及的所有表都是应该加解密的，所以我们只解析第一张表的字段归属
     *
     * @author liutangqi
     * @date 2024/3/6 13:58
     * @Param [setOpList]
     **/
    @Override
    public void visit(SetOperationList setOpList) {
        List<SelectBody> selects = setOpList.getSelects();
        if (CollectionUtils.isEmpty(selects)) {
            return;
        }
        SelectBody selectBody = selects.get(0);
        FieldParseParseTableSelectVisitor fieldParseParseTableSelectVisitor = new FieldParseParseTableSelectVisitor(this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        selectBody.accept(fieldParseParseTableSelectVisitor);
    }

    @Override
    public void visit(WithItem withItem) {

    }

    @Override
    public void visit(ValuesStatement aThis) {

    }
}
