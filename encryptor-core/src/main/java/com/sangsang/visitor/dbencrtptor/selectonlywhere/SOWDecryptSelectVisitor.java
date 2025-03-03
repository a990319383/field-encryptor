package com.sangsang.visitor.dbencrtptor.selectonlywhere;

import com.sangsang.domain.constants.NumberConstant;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.domain.function.EncryptorFunctionScene;
import com.sangsang.visitor.dbencrtptor.select.SDecryptExpressionVisitor;
import com.sangsang.visitor.fieldparse.FieldParseParseTableSelectVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * select 语句只针对where条件进行加解密 入口
 * 注意：区别于 SDecryptSelectVisitor  此类只对where条件后面的数据进行加解密，对查询的字段不加密
 *
 * @author liutangqi
 * @date 2024/2/29 15:43
 */
public class SOWDecryptSelectVisitor extends BaseFieldParseTable implements SelectVisitor {
    private static final Logger log = LoggerFactory.getLogger(SOWDecryptSelectVisitor.class);

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
            SDecryptExpressionVisitor sDecryptExpressionVisitor = SDecryptExpressionVisitor.newInstanceCurLayer(this, EncryptorFunctionScene.defaultDecryption(), where);
            where.accept(sDecryptExpressionVisitor);
            //处理后的条件赋值
            plainSelect.setWhere(sDecryptExpressionVisitor.getExpression());
        }
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

            //需要加密的where条件进行加密处理
            SOWDecryptSelectVisitor sDecryptSelectVisitor = new SOWDecryptSelectVisitor(NumberConstant.ONE, fieldParseTableSelectVisitor.getLayerSelectTableFieldMap(), fieldParseTableSelectVisitor.getLayerFieldTableMap());
            select.accept(sDecryptSelectVisitor);

            //维护加解密之后的语句
            resSelectBody.add(select);
        }
        setOpList.setSelects(resSelectBody);
    }

    @Override
    public void visit(WithItem withItem) {

    }

    @Override
    public void visit(ValuesStatement aThis) {

    }
}
