package com.sangsang.visitor.dbencrtptor.insert;

import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.domain.enums.EncryptorFunctionEnum;
import com.sangsang.domain.function.EncryptorFunctionScene;
import com.sangsang.visitor.dbencrtptor.select.SDecryptExpressionVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * insert 语句中的select语句的where条件进行解密处理
 * 备注：select 里面的字段没有必要加解密，因为库里查出来直接插入的，原样查出来插入就行了
 * todo-ltq 后面改造来和select用同一个
 *
 * @author liutangqi
 * @date 2024/3/8 16:27
 */
public class IDecryptSelectVisitor extends BaseFieldParseTable implements SelectVisitor {

    //insert中 需要加密的字段的索引
    private List<String> needEncryptIndex;

    public IDecryptSelectVisitor(List<String> needEncryptIndex, int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
        this.needEncryptIndex = needEncryptIndex;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        //insert select 不支持里外加解密情况不同的场景，select的表是密文，insert的表字段必须是密文，这里对select字段不做处理
        //1.处理select（不处理，没必要）
        // 情况1：insert表的字段和select的表的字段都需要加解密，或者都不需要：两个都不做加解密处理
        // 情况2：insert表的字段需要加密，select的字段不需要加密：对select的字段进行加密处理
        // 情况3：insert表的字段不需要加密，select字段需要加密：对select的字段进行解密处理
//        List<SelectItem> selectItems = plainSelect.getSelectItems();
//        for (int i = 0; i < selectItems.size(); i++) {
//            SelectItem selectItem = selectItems.get(i);
//            //只处理select xxx字段，不处理 * ,别名.* （这两种写法没办法知道顺序，insert select 语句这种场景无法处理）
//            if (!(selectItem instanceof SelectExpressionItem)) {
//                continue;
//            }
//            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
//            selectExpressionItem.getExpression().accept();
//
//        }

        //2.处理where 条件
        Expression where = plainSelect.getWhere();
        if (where == null) {
            return;
        }

        //where 条件的相应字段进行解密处理
        SDecryptExpressionVisitor sDecryptExpressionVisitor = SDecryptExpressionVisitor.newInstanceCurLayer(this, EncryptorFunctionEnum.DEFAULT_DECRYPTION, where);
        where.accept(sDecryptExpressionVisitor);

        //替换原表达式
        plainSelect.setWhere(sDecryptExpressionVisitor.getExpression());
    }

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
