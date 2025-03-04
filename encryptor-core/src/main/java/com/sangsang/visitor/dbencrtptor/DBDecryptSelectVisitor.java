package com.sangsang.visitor.dbencrtptor;

import com.sangsang.domain.constants.NumberConstant;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.domain.enums.EncryptorFunctionEnum;
import com.sangsang.util.CollectionUtils;
import com.sangsang.util.JsqlparserUtil;
import com.sangsang.visitor.fieldparse.FieldParseParseTableSelectVisitor;
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
public class DBDecryptSelectVisitor extends BaseFieldParseTable implements SelectVisitor {

    private String resultSql;

    /**
     * 当涉及到上游不同字段需要进行不同的加解密场景时。这个字段传上游的需要加密的项的索引下标
     * 例如：
     * 场景1：insert(a,b,c) select( x,y,z) 当 a,c需要密文存储，b 不需要时，这值就是['0','2']
     * 场景2：xxx in(select x,y,z) ，当xxx 需要密文存储时，这个值就是['0']
     * 不涉及到这个项有不同的加密解密场景的话，这个字段为null
     */
    private List<Integer> upstreamNeedEncryptIndex;

    /**
     * 获取当前层解析实例
     *
     * @author liutangqi
     * @date 2025/3/2 22:22
     * @Param [baseFieldParseTable, encryptorFunction]
     **/
    public static DBDecryptSelectVisitor newInstanceCurLayer(BaseFieldParseTable baseFieldParseTable,
                                                             List<Integer> upstreamNeedEncryptIndex) {
        return new DBDecryptSelectVisitor(upstreamNeedEncryptIndex,
                baseFieldParseTable.getLayer(),
                baseFieldParseTable.getLayerSelectTableFieldMap(),
                baseFieldParseTable.getLayerFieldTableMap());

    }

    /**
     * 获取当前层解析实例
     *
     * @author liutangqi
     * @date 2025/3/2 22:22
     * @Param [baseFieldParseTable, encryptorFunction]
     **/
    public static DBDecryptSelectVisitor newInstanceCurLayer(BaseFieldParseTable baseFieldParseTable) {
        return new DBDecryptSelectVisitor(null,
                baseFieldParseTable.getLayer(),
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
    public static DBDecryptSelectVisitor newInstanceNextLayer(BaseFieldParseTable baseFieldParseTable) {
        return new DBDecryptSelectVisitor(null,
                (baseFieldParseTable.getLayer() + 1),
                baseFieldParseTable.getLayerSelectTableFieldMap(),
                baseFieldParseTable.getLayerFieldTableMap());

    }


    private DBDecryptSelectVisitor(List<Integer> upstreamNeedEncryptIndex,
                                   int layer,
                                   Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap,
                                   Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
        this.upstreamNeedEncryptIndex = Optional.ofNullable(upstreamNeedEncryptIndex).orElse(new ArrayList<>());
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
            DBDecryptFromItemVisitor sDecryptFromItemVisitor = DBDecryptFromItemVisitor.newInstanceCurLayer(this);
            fromItem.accept(sDecryptFromItemVisitor);
        }

        //2.将 select *  select 别名.* 转换为select 字段
        List<SelectItem> selectItems = plainSelect.getSelectItems().stream()
                .map(m -> JsqlparserUtil.perfectAllColumns(m, this.getLayerFieldTableMap().get(String.valueOf(this.getLayer()))))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        //3.将其中的 select 的每一项 如果需要解密的进行解密处理 （不需要处理的 * ，别名.* 原样返回）
        for (int i = 0; i < selectItems.size(); i++) {
            SelectItem sItem = selectItems.get(i);
            if (sItem instanceof SelectExpressionItem) {
                SelectExpressionItem se = (SelectExpressionItem) sItem;
                Expression expression = se.getExpression();
                //根据当前项对应的上游字段是否密文存储来决定下游使用加密还是使用解密
                EncryptorFunctionEnum encryptorFunctionEnum = this.upstreamNeedEncryptIndex.contains(i) ? EncryptorFunctionEnum.UPSTREAM_SECRET : EncryptorFunctionEnum.UPSTREAM_PLAINTEXT;
                DBDecryptExpressionVisitor sDecryptExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this, encryptorFunctionEnum);
                expression.accept(sDecryptExpressionVisitor);
                //之前有别名就用之前的，之前没有的话，采用处理后的别名
                se.setAlias(Optional.ofNullable(se.getAlias()).orElse(sDecryptExpressionVisitor.getAlias()));
                se.setExpression(Optional.ofNullable(sDecryptExpressionVisitor.getExpression()).orElse(expression));
            }
        }

        //4.修改原sql查询项
        plainSelect.setSelectItems(selectItems);

        //5.对where条件后的进行解密
        if (plainSelect.getWhere() != null) {
            Expression where = plainSelect.getWhere();
            DBDecryptExpressionVisitor sDecryptExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this, EncryptorFunctionEnum.DEFAULT_DECRYPTION);
            where.accept(sDecryptExpressionVisitor);
            //处理后的条件赋值
            plainSelect.setWhere(Optional.ofNullable(sDecryptExpressionVisitor.getExpression()).orElse(where));
        }

        //6.对join  的on后面的参数进行加解密处理
        List<Join> joins = plainSelect.getJoins();
        if (CollectionUtils.isNotEmpty(joins)) {
            for (Join join : joins) {
                List<Expression> dencryptExpressions = new ArrayList<>();
                for (Expression expression : join.getOnExpressions()) {
                    DBDecryptExpressionVisitor sDecryptExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this, EncryptorFunctionEnum.DEFAULT_DECRYPTION);
                    expression.accept(sDecryptExpressionVisitor);
                    dencryptExpressions.add(Optional.ofNullable(sDecryptExpressionVisitor.getExpression()).orElse(expression));
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
            FieldParseParseTableSelectVisitor fieldParseTableSelectVisitor = FieldParseParseTableSelectVisitor.newInstanceFirstLayer();
            select.accept(fieldParseTableSelectVisitor);

            //需要加密的字段进行加密处理
            DBDecryptSelectVisitor sDecryptSelectVisitor = DBDecryptSelectVisitor.newInstanceCurLayer(fieldParseTableSelectVisitor);
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
