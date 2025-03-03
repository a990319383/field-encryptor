package com.sangsang.visitor.dbencrtptor.select;

import com.sangsang.domain.constants.NumberConstant;
import com.sangsang.domain.dto.BaseDEcryptParseTable;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.domain.function.EncryptorFunction;
import com.sangsang.domain.function.EncryptorFunctionScene;
import com.sangsang.util.CollectionUtils;
import com.sangsang.util.JsqlparserUtil;
import com.sangsang.visitor.fieldparse.FieldParseParseTableSelectVisitor;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.*;
import java.util.stream.Collectors;

/**
 * select 语句  字段解密 入口
 * 备注：包含 select 和 where 语句一同加解密
 *
 * @author liutangqi
 * @date 2024/2/29 15:43
 */
public class SDecryptSelectVisitor extends BaseDEcryptParseTable implements SelectVisitor {

    private String resultSql;


    /**
     * 获取当前层解析实例
     *
     * @author liutangqi
     * @date 2025/3/2 22:22
     * @Param [baseFieldParseTable, encryptorFunction]
     **/
    public static SDecryptSelectVisitor newInstanceCurLayer(BaseFieldParseTable baseFieldParseTable,
                                                            EncryptorFunction encryptorFunction) {
        return new SDecryptSelectVisitor(baseFieldParseTable.getLayer(),
                encryptorFunction,
                baseFieldParseTable.getLayerSelectTableFieldMap(),
                baseFieldParseTable.getLayerFieldTableMap());

    }

    /**
     * 获取下一层解析实例
     *
     * @author liutangqi
     * @date 2025/3/2 22:22
     * @Param [baseFieldParseTable, encryptorFunction]
     **/
    public static SDecryptSelectVisitor newInstanceNextLayer(BaseFieldParseTable baseFieldParseTable,
                                                             EncryptorFunction encryptorFunction) {
        return new SDecryptSelectVisitor((baseFieldParseTable.getLayer() + 1),
                encryptorFunction,
                baseFieldParseTable.getLayerSelectTableFieldMap(),
                baseFieldParseTable.getLayerFieldTableMap());

    }


    private SDecryptSelectVisitor(int layer,
                                  EncryptorFunction encryptorFunction,
                                  Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap,
                                  Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, encryptorFunction, layerSelectTableFieldMap, layerFieldTableMap);
    }

    public String getResultSql() {
        return resultSql;
    }

    /**
     * 普通的select 查询
     *
     * @author liutangqi
     * @date 2024/2/29 16:12
     * @Param [plainSelect]
     **/
    @Override
    public void visit(PlainSelect plainSelect) {
        //1.解密 from 的表 （解密所有内层的语句）
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem != null) {
            SDecryptFromItemVisitor sDecryptFromItemVisitor = new SDecryptFromItemVisitor(this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
            fromItem.accept(sDecryptFromItemVisitor);
        }

        //2.将 select *  select 别名.* 转换为select 字段
        List<SelectItem> selectItems = plainSelect.getSelectItems().stream()
                .map(m -> JsqlparserUtil.perfectAllColumns(m, this.getLayerFieldTableMap().get(String.valueOf(this.getLayer()))))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        //3.将其中的 select 的每一项 如果需要解密的进行解密处理 （不需要处理的 * ，别名.* 原样返回）
        List<SelectItem> DencryptSelectItems = selectItems.stream()
                .peek(p -> {
                    if (p instanceof SelectExpressionItem) {
                        SelectExpressionItem se = (SelectExpressionItem) p;
                        Alias alias = se.getAlias();
                        Expression expression = se.getExpression();
                        SDecryptExpressionVisitor sDecryptExpressionVisitor = SDecryptExpressionVisitor.newInstanceCurLayer(this, expression);
                        expression.accept(sDecryptExpressionVisitor);
                        //之前有别名就用之前的，之前没有的话，采用处理后的别名
                        se.setAlias(Optional.ofNullable(alias).orElse(sDecryptExpressionVisitor.getAlias()));
                        se.setExpression(sDecryptExpressionVisitor.getExpression());
                    }
                }).collect(Collectors.toList());

        //4.修改原sql查询项
        plainSelect.setSelectItems(DencryptSelectItems);

        //5.对where条件后的进行解密
        if (plainSelect.getWhere() != null) {
            Expression where = plainSelect.getWhere();
            SDecryptExpressionVisitor sDecryptExpressionVisitor = SDecryptExpressionVisitor.newInstanceCurLayer(this, where);
            where.accept(sDecryptExpressionVisitor);
            //处理后的条件赋值
            plainSelect.setWhere(sDecryptExpressionVisitor.getExpression());
        }

        //6.对join  的on后面的参数进行加解密处理
        List<Join> joins = plainSelect.getJoins();
        if (CollectionUtils.isNotEmpty(joins)) {
            for (Join join : joins) {
                List<Expression> dencryptExpressions = new ArrayList<>();
                for (Expression expression : join.getOnExpressions()) {
                    SDecryptExpressionVisitor sDecryptExpressionVisitor = SDecryptExpressionVisitor.newInstanceCurLayer(this, expression);
                    expression.accept(sDecryptExpressionVisitor);
                    dencryptExpressions.add(sDecryptExpressionVisitor.getExpression());
                }
                //处理后的结果赋值
                join.setOnExpressions(dencryptExpressions);
            }
        }

        //7.维护解析后的sql
        this.resultSql = plainSelect.toString();
    }

    /**
     * union 查询
     *
     * @author liutangqi
     * @date 2024/2/29 16:12
     * @Param [setOpList]
     **/
    @Override
    public void visit(SetOperationList setOpList) {
        List<SelectBody> selects = setOpList.getSelects();

        List<SelectBody> resSelectBody = new ArrayList<>();
        for (int i = 0; i < selects.size(); i++) {
            SelectBody select = selects.get(i);
            //解析每个union的语句自己拥有的字段信息
            FieldParseParseTableSelectVisitor fieldParseTableSelectVisitor = new FieldParseParseTableSelectVisitor(NumberConstant.ONE, null, null);
            select.accept(fieldParseTableSelectVisitor);

            //需要加密的字段进行加密处理
            SDecryptSelectVisitor sDecryptSelectVisitor = SDecryptSelectVisitor.newInstanceCurLayer(fieldParseTableSelectVisitor, EncryptorFunctionScene.defaultDecryption());
            select.accept(sDecryptSelectVisitor);

            //维护加解密之后的语句
            resSelectBody.add(select);
        }
        setOpList.setSelects(resSelectBody);
        this.resultSql = setOpList.toString();
    }

    @Override
    public void visit(WithItem withItem) {

    }

    @Override
    public void visit(ValuesStatement aThis) {

    }
}
