package com.sangsang.visitor.encrtptor.insert;

import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.visitor.encrtptor.where.WhereDencryptExpressionVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.Map;
import java.util.Set;

/**
 * insert 语句中的select语句的where条件进行解密处理
 * 备注：select 里面的字段没有必要加解密，因为库里查出来直接插入的，原样查出来插入就行了
 *
 * @author liutangqi
 * @date 2024/3/8 16:27
 */
public class IDecryptSelectVisitor extends BaseFieldParseTable implements SelectVisitor {

    public IDecryptSelectVisitor(int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        //只用处理where 条件即可
        Expression where = plainSelect.getWhere();
        if (where == null) {
            return;
        }

        //where 条件的相应字段进行解密处理
        WhereDencryptExpressionVisitor dencryptWhereFieldVisitor = new WhereDencryptExpressionVisitor(where, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        where.accept(dencryptWhereFieldVisitor);

        //替换原表达式
        plainSelect.setWhere(dencryptWhereFieldVisitor.getExpression());
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
