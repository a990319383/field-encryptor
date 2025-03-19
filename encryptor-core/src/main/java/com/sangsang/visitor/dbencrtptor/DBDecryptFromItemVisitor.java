package com.sangsang.visitor.dbencrtptor;

import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * @author liutangqi
 * @date 2024/2/29 16:07
 */
public class DBDecryptFromItemVisitor extends BaseFieldParseTable implements FromItemVisitor {


    /**
     * 获取当前层的解析对象
     *
     * @author liutangqi
     * @date 2025/3/4 15:44
     * @Param [baseFieldParseTable]
     **/
    public static DBDecryptFromItemVisitor newInstanceCurLayer(BaseFieldParseTable baseFieldParseTable) {
        return new DBDecryptFromItemVisitor(baseFieldParseTable.getLayer(),
                baseFieldParseTable.getLayerSelectTableFieldMap(),
                baseFieldParseTable.getLayerFieldTableMap());
    }

    private DBDecryptFromItemVisitor(int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
    }

    @Override
    public void visit(Table table) {
    }


    /**
     * 嵌套子查询 再解析里层的内容
     *
     * @author liutangqi
     * @date 2024/2/29 16:13
     * @Param [subSelect]
     **/
    @Override
    public void visit(ParenthesedSelect subSelect) {
        //解密子查询内容（注意：这里是下一层的）
        Optional.ofNullable(subSelect.getPlainSelect())
                .ifPresent(p -> p.accept(DBDecryptSelectVisitor.newInstanceNextLayer(this)));
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {

    }


    @Override
    public void visit(TableFunction tableFunction) {

    }

    @Override
    public void visit(ParenthesedFromItem aThis) {

    }

}
