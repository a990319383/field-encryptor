package com.sangsang.visitor.encrtptor.fieldparse;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.sangsang.domain.constants.DecryptConstant;
import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.util.JsqlparserUtil;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 解析sql select中出现过的字段及所属真实表
 *
 * @author liutangqi
 * @date 2024/3/5 14:58
 */
public class FieldParseParseExpressionVisitor extends BaseFieldParseTable implements ExpressionVisitor {
    /**
     * 当前字段拥有的别名
     */
    private Alias alias;

    public FieldParseParseExpressionVisitor(Alias alias, int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
        this.alias = alias;
    }


    @Override
    public void visit(BitwiseRightShift aThis) {

    }

    @Override
    public void visit(BitwiseLeftShift aThis) {

    }

    @Override
    public void visit(NullValue nullValue) {

    }

    @Override
    public void visit(Function function) {
        //别名如果不存在的话，就用Function的ToString的结果作为别名
        alias = Optional.ofNullable(alias)
                .orElse(new Alias(SymbolConstant.FLOAT + function.toString() + SymbolConstant.FLOAT));

        //将这个别名的字段归属在 DecryptConstant.FUNCTION_TMP 这张虚拟的表别名中
        //有些嵌套查询时会有* ，* 时需要包含此处理结果，所以需要把这个维护进去（所以搜索DecryptConstant.FUNCTION_TMP 这个key值没有其它取的地方，因为是*的时候用到，不需要key）
        String aliasColumName = alias.getName();
        //function处理后的结果，放的结果的key是这个
        String tableAliasName = DecryptConstant.FUNCTION_TMP;

        FieldInfoDto fieldInfoDto = FieldInfoDto.builder().columnName(aliasColumName).sourceTableName(null).sourceColumn(null).fromSourceTable(false).build();

        //将当前字段存入layerSelectTableFieldMap 中
        JsqlparserUtil.putFieldInfo(this.getLayerSelectTableFieldMap(), this.getLayer(), tableAliasName, fieldInfoDto);
    }

    @Override
    public void visit(SignedExpression signedExpression) {

    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {

    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {

    }

    @Override
    public void visit(DoubleValue doubleValue) {

    }

    @Override
    public void visit(LongValue longValue) {

    }

    @Override
    public void visit(HexValue hexValue) {

    }

    @Override
    public void visit(DateValue dateValue) {

    }

    @Override
    public void visit(TimeValue timeValue) {

    }

    @Override
    public void visit(TimestampValue timestampValue) {

    }

    @Override
    public void visit(Parenthesis parenthesis) {

    }

    @Override
    public void visit(StringValue stringValue) {

    }

    @Override
    public void visit(Addition addition) {

    }

    @Override
    public void visit(Division division) {

    }

    @Override
    public void visit(IntegerDivision division) {

    }

    @Override
    public void visit(Multiplication multiplication) {

    }

    @Override
    public void visit(Subtraction subtraction) {

    }

    @Override
    public void visit(AndExpression andExpression) {

    }

    @Override
    public void visit(OrExpression orExpression) {

    }

    @Override
    public void visit(XorExpression xorExpression) {

    }

    @Override
    public void visit(Between between) {

    }

    @Override
    public void visit(EqualsTo equalsTo) {

    }

    @Override
    public void visit(GreaterThan greaterThan) {

    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {

    }

    @Override
    public void visit(InExpression inExpression) {

    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {

    }

    @Override
    public void visit(IsNullExpression isNullExpression) {

    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {

    }

    @Override
    public void visit(LikeExpression likeExpression) {

    }

    @Override
    public void visit(MinorThan minorThan) {

    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {

    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {

    }

    @Override
    public void visit(Column tableColumn) {
        //1.解析当前字段所属的表信息
        ColumnTableDto columnTableDto = JsqlparserUtil.parseColumn(tableColumn, this.getLayer(), this.getLayerFieldTableMap());
        //当前字段别名，别名没有取库字段名
        String aliasColumName = Optional.ofNullable(alias).map(Alias::getName).orElse(tableColumn.getColumnName());

        //2.匹配到了真实表名，则将此字段存入 layerSelectTableFieldMap
        if (StringUtils.isNotBlank(columnTableDto.getSourceTableName())) {
            FieldInfoDto fieldInfoDto = FieldInfoDto.builder()
                    .columnName(aliasColumName)
                    .sourceTableName(columnTableDto.getSourceTableName())
                    .sourceColumn(columnTableDto.getSourceColumn())
                    .fromSourceTable(columnTableDto.isFromSourceTable())
                    .build();

            //将此字段存入 layerSelectTableFieldMap 中
            JsqlparserUtil.putFieldInfo(this.getLayerSelectTableFieldMap(), this.getLayer(), columnTableDto.getTableAliasName(), fieldInfoDto);
        }
    }

    /**
     * select
     * (select 字段 from xxx where)
     * from
     * 这种语法
     * 这种语法内层肯定只能有一个值，这个值的别名有点特殊，不是来自内层select的别名，那个是无意义的，真正的别名是整个内层select的别名
     * 备注：内层的select 查询的字段现在已经兼容可以查询内层关联的表字段，但是这种写法不建议，甚至有点呆
     *
     * @author liutangqi
     * @date 2024/3/6 13:27
     * @Param [subSelect]
     **/
    @Override
    public void visit(SubSelect subSelect) {
        //1.将现在的两个存储解析结果的map深克隆拷贝一份，用这两份数据去解析子查询的结果，避免这个子查询也拥有子查询，导致影响当前解析结果的map的下一层结果出错
        Map<String, Map<String, Set<FieldInfoDto>>> cloneLayerSelectTableFieldMap = ObjectUtil.cloneByStream(this.getLayerSelectTableFieldMap());
        Map<String, Map<String, Set<FieldInfoDto>>> cloneLayerFieldTableMap = ObjectUtil.cloneByStream(this.getLayerFieldTableMap());

        //2.单独解析当前子查询的语法 （单独解析是为了好提取，因为解析的两个Map值的别名需要单独修改）
        FieldParseParseTableSelectVisitor sFieldSelectItemVisitor = new FieldParseParseTableSelectVisitor(this.getLayer(), cloneLayerSelectTableFieldMap, cloneLayerFieldTableMap);
        subSelect.getSelectBody().accept(sFieldSelectItemVisitor);

        //3.找出上面新解析出的结果，只取这一层的，其它层的结果不需要关心（因为这个结果需要单独处理别名 这种语法下select （select ） 内层select的别名是没有意义的，是以外层的select语句为准的,select 的内层关联的表字段也是没有意义的）
        Map<String, Set<FieldInfoDto>> newSelectTableFieldMap = JsqlparserUtil.parseNewlyIncreased(this.getLayerSelectTableFieldMap().getOrDefault(String.valueOf(this.getLayer()), new HashMap<>()),
                cloneLayerSelectTableFieldMap.getOrDefault(String.valueOf(this.getLayer()), new HashMap<>()));

        //4.结果合并 (注意：新增加的结果的别名需要修改成外层select的别名)
        for (Map.Entry<String, Set<FieldInfoDto>> fieldInfoEntry : newSelectTableFieldMap.entrySet()) {
            //4.1将这个字段的别名重新设置（这种语法下select （select ） 内层select的别名是没有意义的，是以外层的select语句为准的）
            Set<FieldInfoDto> fieldInfoDtos = fieldInfoEntry.getValue()
                    .stream()
                    .map(m -> FieldInfoDto.builder()
                            .sourceTableName(m.getSourceTableName())
                            .sourceColumn(m.getSourceColumn())
                            .fromSourceTable(m.isFromSourceTable())
                            .columnName(Optional.ofNullable(this.alias).map(Alias::getName).orElse(subSelect.toString()))
                            .build())
                    .collect(Collectors.toSet());
            //4.2 结果合并
            JsqlparserUtil.putFieldInfo(this.getLayerSelectTableFieldMap(), this.getLayer(), fieldInfoEntry.getKey(), fieldInfoDtos);
        }
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        // 解析当前sql的查询字段不用管 case
    }

    @Override
    public void visit(WhenClause whenClause) {
        // 解析当前sql的查询字段不用管 when
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        // 解析当前sql的查询字段不用管 exists
    }


    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {

    }

    @Override
    public void visit(Concat concat) {

    }

    @Override
    public void visit(Matches matches) {

    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {

    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {

    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {

    }

    @Override
    public void visit(CastExpression cast) {

    }

    @Override
    public void visit(TryCastExpression tryCastExpression) {

    }

    @Override
    public void visit(Modulo modulo) {

    }

    @Override
    public void visit(AnalyticExpression aexpr) {

    }

    @Override
    public void visit(ExtractExpression eexpr) {

    }

    @Override
    public void visit(IntervalExpression iexpr) {

    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {

    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {

    }

    @Override
    public void visit(JsonExpression jsonExpr) {

    }

    @Override
    public void visit(JsonOperator jsonExpr) {

    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {

    }

    @Override
    public void visit(UserVariable var) {

    }

    @Override
    public void visit(NumericBind bind) {

    }

    @Override
    public void visit(KeepExpression aexpr) {

    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {

    }

    @Override
    public void visit(ValueListExpression valueList) {

    }

    @Override
    public void visit(RowConstructor rowConstructor) {

    }

    @Override
    public void visit(RowGetExpression rowGetExpression) {

    }

    @Override
    public void visit(OracleHint hint) {

    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {

    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {

    }

    @Override
    public void visit(NotExpression aThis) {

    }

    @Override
    public void visit(NextValExpression aThis) {

    }

    @Override
    public void visit(CollateExpression aThis) {

    }

    @Override
    public void visit(SimilarToExpression aThis) {

    }

    @Override
    public void visit(ArrayExpression aThis) {

    }

    @Override
    public void visit(ArrayConstructor arrayConstructor) {

    }

    @Override
    public void visit(VariableAssignment variableAssignment) {

    }

    @Override
    public void visit(XMLSerializeExpr xmlSerializeExpr) {

    }

    @Override
    public void visit(TimezoneExpression timezoneExpression) {

    }

    @Override
    public void visit(JsonAggregateFunction jsonAggregateFunction) {

    }

    @Override
    public void visit(JsonFunction jsonFunction) {

    }

    @Override
    public void visit(ConnectByRootOperator connectByRootOperator) {

    }

    @Override
    public void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter) {

    }

    @Override
    public void visit(AllColumns allColumns) {

    }

    @Override
    public void visit(AllTableColumns allTableColumns) {

    }

    @Override
    public void visit(AllValue allValue) {

    }

    @Override
    public void visit(IsDistinctExpression isDistinctExpression) {

    }

    @Override
    public void visit(GeometryDistance geometryDistance) {

    }
}
