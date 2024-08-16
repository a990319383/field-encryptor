package com.sangsang.visitor.fieldparse;

import com.sangsang.cache.TableCache;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.util.JsqlparserUtil;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 解析select 语句中每一层的sql中用到的表和该表的全部字段
 *
 * @author liutangqi
 * @date 2024/3/4 10:11
 */
public class FieldParseParseTableFromItemVisitor extends BaseFieldParseTable implements FromItemVisitor {

    public FieldParseParseTableFromItemVisitor(int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
    }

    @Override
    public void visit(Table table) {
        //1.当前表表名信息
        String tableName = table.getName().toLowerCase();
        String aliasTable = Optional.ofNullable(table.getAlias()).map(Alias::getName).map(String::toLowerCase).orElse(tableName);

        //2.获取当前表的全部字段信息
        Set<FieldInfoDto> fieldInfoSet = Optional.ofNullable(TableCache.getTableFieldMap().get(tableName))
                .orElse(new HashSet<>())
                .stream()
                .map(m -> FieldInfoDto.builder().columnName(m.toLowerCase()).sourceTableName(tableName).fromSourceTable(true).sourceColumn(m.toLowerCase()).build())
                .collect(Collectors.toSet());

        //3.将这些字段信息维护到 layerFieldTableMap 中
        JsqlparserUtil.putFieldInfo(this.getLayerFieldTableMap(), this.getLayer(), aliasTable, fieldInfoSet);
    }

    /**
     * 子查询当前层的表的全部字段，就是下一层的select的全部字段
     *
     * @author liutangqi
     * @date 2024/3/5 15:53
     * @Param [subSelect]
     **/
    @Override
    public void visit(SubSelect subSelect) {
//        int layer = this.getLayer(); 注意：这里不能使用这样写，必须用this.getLayer() 存在类似递归的操作，这里的变量layer可能是上一层的，而另外两个Map是所有层级共享的
        //子查询的别名，作为当前层字段的表名
        String aliasTable = subSelect.getAlias().getName().toLowerCase();

        //1.解析子查询下一层，层数 + 1
        FieldParseParseTableSelectVisitor fieldParseTableSelectVisitor = new FieldParseParseTableSelectVisitor(this.getLayer() + 1, this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        subSelect.getSelectBody().accept(fieldParseTableSelectVisitor);

        //2.解析这一层涉及到的表的全部字段，子查询的时，本层的表的全部字段就是下一层的全部select的字段，本层的表名就是别名
        Map<String, Set<FieldInfoDto>> selectTableFieldMap = this.getLayerSelectTableFieldMap().getOrDefault(String.valueOf(this.getLayer() + 1), new HashMap<>());
        //本层的字段都是来源于嵌套查询的结果集，不是真实表，所以将 fromSourceTable设置为false
        Set<FieldInfoDto> fieldInfoSet = selectTableFieldMap
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(m -> FieldInfoDto.builder()
                        .fromSourceTable(false)
                        .columnName(m.getColumnName())
                        .sourceColumn(m.getSourceColumn())
                        .sourceTableName(m.getSourceTableName())
                        .build())
                .collect(Collectors.toSet());

        //3. 将当前层的全部字段维护进 layerFieldTableMap 中
        JsqlparserUtil.putFieldInfo(this.getLayerFieldTableMap(), this.getLayer(), aliasTable, fieldInfoSet);
    }

    @Override
    public void visit(SubJoin subjoin) {
        System.out.println("当前语法未适配");
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        System.out.println("当前语法未适配");
    }

    @Override
    public void visit(ValuesList valuesList) {
        System.out.println("当前语法未适配");
    }

    @Override
    public void visit(TableFunction tableFunction) {
        System.out.println("当前语法未适配");
    }

    @Override
    public void visit(ParenthesisFromItem aThis) {
        System.out.println("当前语法未适配");
    }
}
