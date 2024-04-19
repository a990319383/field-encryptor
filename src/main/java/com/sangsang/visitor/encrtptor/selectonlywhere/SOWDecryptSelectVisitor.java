package com.sangsang.visitor.encrtptor.selectonlywhere;

import com.sangsang.domain.constants.NumberConstant;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.visitor.encrtptor.fieldparse.FieldParseParseTableSelectVisitor;
import com.sangsang.visitor.encrtptor.where.WhereDencryptExpressionVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.*;

/**
 * select 语句只针对where条件进行加解密 入口
 * 注意：区别于 com.sangsang.visitor.encrtptor.select.SDecryptSelectVisitor  此类只对where条件后面的数据进行加解密
 *
 * @author liutangqi
 * @date 2024/2/29 15:43
 */
public class SOWDecryptSelectVisitor extends BaseFieldParseTable implements SelectVisitor {


    public SOWDecryptSelectVisitor(int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
    }

    /**
     * 普通的select 查询
     *
     * @author liutangqi
     * @date 2024/4/12 17:51
     * @Param [plainSelect]
     **/
    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getWhere() != null) {
            Expression where = plainSelect.getWhere();
            WhereDencryptExpressionVisitor whereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(where, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
            where.accept(whereDencryptExpressionVisitor);
            //处理后的条件赋值
            plainSelect.setWhere(whereDencryptExpressionVisitor.getExpression());
        }
    }

    /**
     * union 查询 todo-ltq
     *
     * @author liutangqi
     * @date 2024/2/29 16:12
     * @Param [setOpList]
     **/
    @Override
    public void visit(SetOperationList setOpList) {
        List<SetOperation> operations = setOpList.getOperations();

        List<SelectBody> selects = setOpList.getSelects();
        for (int i = 0; i < selects.size(); i++) {
            SelectBody select = selects.get(i);
            //解析每个union的语句自己拥有的字段信息
            FieldParseParseTableSelectVisitor fieldParseTableSelectVisitor = new FieldParseParseTableSelectVisitor(NumberConstant.ONE, null, null);
            select.accept(fieldParseTableSelectVisitor);

            //需要加密的字段进行加密处理
            SOWDecryptSelectVisitor sDecryptSelectVisitor = new SOWDecryptSelectVisitor(NumberConstant.ONE, fieldParseTableSelectVisitor.getLayerSelectTableFieldMap(), fieldParseTableSelectVisitor.getLayerFieldTableMap());
            select.accept(sDecryptSelectVisitor);

        }
    }

    @Override
    public void visit(WithItem withItem) {

    }

    @Override
    public void visit(ValuesStatement aThis) {

    }
}
