package com.sangsang.visitor.isolation;

import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author liutangqi
 * @date 2025/6/13 14:36
 */
public class IsolationFromItemVisitor extends BaseFieldParseTable implements FromItemVisitor {

    /**
     * 获取当前层实例
     *
     * @author liutangqi
     * @date 2025/6/13 14:36
     * @Param [baseFieldParseTable]
     **/
    public static IsolationFromItemVisitor newInstanceCurLayer(BaseFieldParseTable baseFieldParseTable) {
        return new IsolationFromItemVisitor(baseFieldParseTable.getLayer(),
                baseFieldParseTable.getLayerSelectTableFieldMap(),
                baseFieldParseTable.getLayerFieldTableMap());
    }

    private IsolationFromItemVisitor(int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
    }

    @Override
    public void visit(Table tableName) {

    }

    /**
     * 嵌套子查询
     *
     * @author liutangqi
     * @date 2025/6/13 14:37
     * @Param [selectBody]
     **/
    @Override
    public void visit(ParenthesedSelect selectBody) {
        //注意：这里存在嵌套，所以是下一层
        Optional.ofNullable(selectBody.getPlainSelect())
                .ifPresent(p -> p.accept(IsolationSelectVisitor.newInstanceNextLayer(this)));
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
