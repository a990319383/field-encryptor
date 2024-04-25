package com.sangsang.visitor.encrtptor.selectonlywhere;

import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.visitor.encrtptor.where.WhereDencryptExpressionVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * select 语句只针对where条件进行加解密 入口
 * 注意：区别于 com.sangsang.visitor.encrtptor.select.SDecryptSelectVisitor  此类只对where条件后面的数据进行加解密
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
            WhereDencryptExpressionVisitor whereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(where, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
            where.accept(whereDencryptExpressionVisitor);
            //处理后的条件赋值
            plainSelect.setWhere(whereDencryptExpressionVisitor.getExpression());
        }
    }

    /**
     * union 查询
     * todo-ltq 备注：只加解密where后面的，用到union查询的情况非常小，这样用的时候，sql非常难看，暂不支持，后续有空了再实现
     *
     * @author liutangqi
     * @date 2024/2/29 16:12
     * @Param [setOpList]
     **/
    @Override
    public void visit(SetOperationList setOpList) {
        log.warn("【SOWDecryptSelectVisitor】暂不支持 union查询");
    }

    @Override
    public void visit(WithItem withItem) {

    }

    @Override
    public void visit(ValuesStatement aThis) {

    }
}
