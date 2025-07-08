package com.sangsang.visitor.isolation;

import com.sangsang.cache.isolation.IsolationInstanceCache;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.domain.enums.IsolationRelationEnum;
import com.sangsang.domain.strategy.isolation.DataIsolationStrategy;
import com.sangsang.util.CollectionUtils;
import com.sangsang.util.ExpressionsUtil;
import com.sangsang.util.StringUtils;
import com.sangsang.visitor.fieldparse.FieldParseParseTableSelectVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;

import java.util.*;

/**
 * @author liutangqi
 * @date 2025/6/13 13:41
 */
public class IsolationSelectVisitor extends BaseFieldParseTable implements SelectVisitor {

    /**
     * 获取当前层实例
     *
     * @author liutangqi
     * @date 2025/6/13 13:43
     * @Param [baseFieldParseTable]
     **/
    public static IsolationSelectVisitor newInstanceCurLayer(BaseFieldParseTable baseFieldParseTable) {
        return new IsolationSelectVisitor(baseFieldParseTable.getLayer(), baseFieldParseTable.getLayerSelectTableFieldMap(), baseFieldParseTable.getLayerFieldTableMap());
    }

    /**
     * 获取下一层的实例
     *
     * @author liutangqi
     * @date 2025/6/13 14:34
     * @Param [baseFieldParseTable]
     **/
    public static IsolationSelectVisitor newInstanceNextLayer(BaseFieldParseTable baseFieldParseTable) {
        return new IsolationSelectVisitor(baseFieldParseTable.getLayer() + 1, baseFieldParseTable.getLayerSelectTableFieldMap(), baseFieldParseTable.getLayerFieldTableMap());
    }

    private IsolationSelectVisitor(int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
    }

    /**
     * 场景1：
     * select
     * (select xx from tb)as xxx -- 这种语法
     * from tb
     * 场景2：
     * xxx in (select xxx from) -- 括号里面的这种语法
     *
     * @author liutangqi
     * @date 2025/6/13 13:48
     * @Param [parenthesedSelect]
     **/
    @Override
    public void visit(ParenthesedSelect parenthesedSelect) {
        //注意：这里层数是当前层，这个的解析结果需要和外层在同一层级
        Optional.ofNullable(parenthesedSelect.getPlainSelect()).ifPresent(p -> p.accept(IsolationSelectVisitor.newInstanceCurLayer(this)));
    }

    /**
     * 普通的select 查询
     *
     * @author liutangqi
     * @date 2025/6/13 13:49
     * @Param [plainSelect]
     **/
    @Override
    public void visit(PlainSelect plainSelect) {
        //1.处理from的表（只处理嵌套查询）
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem != null) {
            fromItem.accept(IsolationFromItemVisitor.newInstanceCurLayer(this));
        }

        //2.处理selectItem中属于子查询的字段
        List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
        if (CollectionUtils.isNotEmpty(selectItems)) {
            for (SelectItem<?> selectItem : selectItems) {
                selectItem.accept(IsolationSelectItemVisitor.newInstanceCurLayer(this));
            }
        }

        //3.处理where条件（主要针对in 子查询和exist）
        Expression where = plainSelect.getWhere();
        if (where != null) {
            where.accept(IsolationExpressionVisitor.newInstanceCurLayer(this));
        }

        //4.处理当前层的数据隔离
        //4.1.存储当前拼接的权限过滤条件
        List<Expression> isolationExpressions = new ArrayList<>();
        //4.2.获取当前层字段信息
        Map<String, Set<FieldInfoDto>> fieldTableMap = this.getLayerFieldTableMap().get(String.valueOf(this.getLayer()));

        //4.3.判断其中是否存在数据隔离的表
        for (Map.Entry<String, Set<FieldInfoDto>> fieldTableEntry : fieldTableMap.entrySet()) {
            //4.3.1 随便获取一个字段，得到这个字段所属的真实表名（因为这些字段都是属于同一张真实表，所以随便获取一个即可）
            FieldInfoDto anyFieldInfo = fieldTableEntry.getValue().stream().findAny().get();
            //4.3.2 通过表名获取到当前的数据隔离的相关信息（外层获取，避免方法重复调用）
            DataIsolationStrategy dataIsolationStrategy = IsolationInstanceCache.getInstance(anyFieldInfo.getSourceTableName());
            String isolationField = dataIsolationStrategy.getIsolationField();
            IsolationRelationEnum isolationRelation = dataIsolationStrategy.getIsolationRelation();
            Object isolationData = dataIsolationStrategy.getIsolationData();
            //4.3.3 依次处理每个字段，判断这些字段是否需要数据隔离
            for (FieldInfoDto fieldInfo : fieldTableEntry.getValue()) {
                //4.3.3.1 此表不存在隔离字段，跳过这张表
                if (dataIsolationStrategy == null) {
                    break;
                }
                //4.3.3.2当前字段不是直接来自真实表的，跳过
                if (!fieldInfo.isFromSourceTable()) {
                    continue;
                }
                //4.3.3.3 当前字段和数据隔离字段不同，跳过
                if (!StringUtils.equalIgnoreFieldSymbol(fieldInfo.getColumnName(), isolationField)) {
                    continue;
                }
                //4.3.3.4 当前数据隔离值为空，跳过 （拼凑个为空的条件，sql肯定执行出问题，所以打个警告日志后跳过）
                if (isolationData == null) {
                    log.warn("【isolation】当前表的权限隔离字段获取为空，请留意是否正常 tableName:{} DataIsolationStrategy:{}", fieldInfo.getSourceTableName(), dataIsolationStrategy.getClass().getName());
                    break;
                }
                //4.3.3.5开拼
                Expression isolationExpression = IsolationInstanceCache.buildIsolationExpression(isolationField, fieldTableEntry.getKey(), isolationRelation, isolationData);
                isolationExpressions.add(isolationExpression);
                break;
            }
        }

        //4.没有需要额外新增的隔离字段吗，则不处理
        if (CollectionUtils.isEmpty(isolationExpressions)) {
            return;
        }

        //5.处理where条件
        //5.1 旧的存在where的话，就把这个也追加到需要处理的表达式里面，注意：旧表达式整体括号扩起来，避免里面存在or导致语义错误
        Optional.ofNullable(plainSelect.getWhere()).ifPresent(p -> isolationExpressions.add(ExpressionsUtil.buildParenthesis(p)));
        //5.2条件表达式只有一个则直接赋值
        if (isolationExpressions.size() == 1) {
            plainSelect.setWhere(isolationExpressions.get(0));
        }
        //5.3 条件表达式有多个，用and拼接
        else {
            Expression preExpression = isolationExpressions.get(0);
            for (int i = 1; i < isolationExpressions.size(); i++) {
                preExpression = ExpressionsUtil.buildAndExpression(preExpression, isolationExpressions.get(i));
            }
            plainSelect.setWhere(preExpression);
        }
    }

    /**
     * union
     *
     * @author liutangqi
     * @date 2025/6/13 13:53
     * @Param [setOpList]
     **/
    @Override
    public void visit(SetOperationList setOpList) {
        List<Select> selects = setOpList.getSelects();
        List<Select> resSelectBody = new ArrayList<>();
        for (int i = 0; i < selects.size(); i++) {
            Select select = selects.get(i);
            //解析每个union的语句自己拥有的字段信息
            FieldParseParseTableSelectVisitor fieldParseTableSelectVisitor = FieldParseParseTableSelectVisitor.newInstanceFirstLayer();
            select.accept(fieldParseTableSelectVisitor);

            //针对每个sql单独进行数据隔离处理
            IsolationSelectVisitor ilSelectVisitor = IsolationSelectVisitor.newInstanceCurLayer(fieldParseTableSelectVisitor);
            select.accept(ilSelectVisitor);

            //维护加解密之后的语句
            resSelectBody.add(select);
        }
        setOpList.setSelects(resSelectBody);
    }

    @Override
    public void visit(WithItem withItem) {

    }

    @Override
    public void visit(Values aThis) {

    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {

    }

    @Override
    public void visit(TableStatement tableStatement) {

    }
}
