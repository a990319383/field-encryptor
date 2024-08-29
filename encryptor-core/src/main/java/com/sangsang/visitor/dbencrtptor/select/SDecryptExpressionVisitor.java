package com.sangsang.visitor.dbencrtptor.select;

import cn.hutool.core.util.ObjectUtil;
import com.sangsang.cache.FieldEncryptorPatternCache;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.util.JsqlparserUtil;
import com.sangsang.visitor.fieldparse.FieldParseParseTableSelectVisitor;
import com.sangsang.visitor.dbencrtptor.where.WhereDencryptExpressionVisitor;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 将select的每一项字段  如果需要加密的，则进行加密
 * 备注：目前主要对Column 类型进行了处理
 *
 * @author liutangqi
 * @date 2024/2/29 16:50
 */
public class SDecryptExpressionVisitor extends BaseFieldParseTable implements ExpressionVisitor {
    private static final Logger log = LoggerFactory.getLogger(SDecryptExpressionVisitor.class);
    /**
     * 当前查询项原来的别名
     */
    private Alias alias;

    /**
     * 加解密处理好后的表达式
     * 如果不需要处理，则这个值就是构造函数传入的旧表达式
     */
    private Expression expression;

    public SDecryptExpressionVisitor(Alias alias, Expression expression, int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
        this.alias = alias;
        this.expression = expression;
    }

    //处理之后的别名，如果别名不需要额外处理，则这里是原有的别名
    public Alias getAlias() {
        return alias;
    }

    public Expression getExpression() {
        return expression;
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

    /**
     * 对于function的进行加密
     *
     * @author liutangqi
     * @date 2024/3/15 10:05
     * @Param [function]
     **/
    @Override
    public void visit(Function function) {
        List<Expression> expressions = Optional.ofNullable(function.getParameters())
                .map(ExpressionList::getExpressions)
                .orElse(new ArrayList<>())
                .stream()
                .map(m -> {
                    SDecryptExpressionVisitor SDecryptExpressionVisitor = new SDecryptExpressionVisitor(this.alias, m, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
                    m.accept(SDecryptExpressionVisitor);
                    return SDecryptExpressionVisitor.getExpression();
                }).collect(Collectors.toList());

        if (function.getParameters() != null) {
            function.getParameters().setExpressions(expressions);
        }
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

    /**
     * select 语句中存在 case when 字段 = xxx then 这种语法的时候， 其中字段=xxx 会走这里的解析
     * 这种语法和where中的处理方式一样，所以走where的逻辑
     *
     * @author liutangqi
     * @date 2024/7/30 16:49
     * @Param [equalsTo]
     **/
    @Override
    public void visit(EqualsTo equalsTo) {
        // when  字段= xxx 这种时候，走where的解析
        equalsTo.accept(new WhereDencryptExpressionVisitor(equalsTo, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap()));
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

    /**
     * select的每一个查询项
     *
     * @author liutangqi
     * @date 2024/2/29 17:39
     * @Param [column]
     **/
    @Override
    public void visit(Column column) {
        //1.判断当前列是否需要加解密
        if (!JsqlparserUtil.needEncrypt(column, this.getLayer(), this.getLayerFieldTableMap())) {
            return;
        }

        //2.将该字段进行解密处理
        Expression decryptFunction = FieldEncryptorPatternCache.getInstance().decryption(column);
        this.expression = decryptFunction;

        //3.别名处理（字段经过加密函数后，如果之前没有别名的话，需要用之前的字段名作为别名，不然ORM映射的时候会无法匹配）
        this.alias = Optional.ofNullable(alias).orElse(new Alias(column.getColumnName()));
    }

    /**
     * select
     * (select 字段 from xx )
     * from
     * 这种语法
     *
     * @author liutangqi
     * @date 2024/3/6 17:19
     * @Param [subSelect]
     **/
    @Override
    public void visit(SubSelect subSelect) {
        //这种语法的里面都是单独的语句，所以这里将里层的语句单独解析一次
        //1.将现在的两个存储解析结果的map深克隆拷贝一份，用这两份数据去解析子查询的结果，避免这个子查询也拥有子查询，导致影响当前解析结果的map的下一层结果出错
        Map<String, Map<String, Set<FieldInfoDto>>> cloneLayerSelectTableFieldMap = ObjectUtil.cloneByStream(this.getLayerSelectTableFieldMap());
        Map<String, Map<String, Set<FieldInfoDto>>> cloneLayerFieldTableMap = ObjectUtil.cloneByStream(this.getLayerFieldTableMap());

        //2.单独解析当前子查询的语法 （单独解析是为了好提取，因为解析的两个Map值的别名需要单独修改）
        FieldParseParseTableSelectVisitor sFieldSelectItemVisitor = new FieldParseParseTableSelectVisitor(this.getLayer(), cloneLayerSelectTableFieldMap, cloneLayerFieldTableMap);
        subSelect.getSelectBody().accept(sFieldSelectItemVisitor);

        //3.利用解析后的表结构Map进行子查询解密处理
        SDecryptSelectVisitor SDecryptSelectVisitor = new SDecryptSelectVisitor(this.getLayer(), sFieldSelectItemVisitor.getLayerSelectTableFieldMap(), sFieldSelectItemVisitor.getLayerFieldTableMap());
        subSelect.getSelectBody().accept(SDecryptSelectVisitor);
    }

    /**
     * case 字段 when xxx then
     * case when 字段=xxx then
     *
     * @author liutangqi
     * @date 2024/7/30 15:35
     * @Param [caseExpression]
     **/
    @Override
    public void visit(CaseExpression caseExpression) {
        //case when 语句的处理 select 和where的处理方式一样，避免冗余代码，这里直接复用where条件的处理方式
        WhereDencryptExpressionVisitor whereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(caseExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        caseExpression.accept(whereDencryptExpressionVisitor);
    }

    @Override
    public void visit(WhenClause whenClause) {
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        //select查询中exist 解密暂未考虑到场景，不做处理，目前只兼容了where条件后面的exist
        System.out.println(existsExpression);

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
        Expression leftExpression = cast.getLeftExpression();
        SDecryptExpressionVisitor expressionVisitor = new SDecryptExpressionVisitor(this.alias, leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(expressionVisitor);
        cast.setLeftExpression(expressionVisitor.getExpression());
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
        //正则 语句的处理 select 和where的处理方式一样，避免冗余代码，这里直接复用where条件的处理方式
        WhereDencryptExpressionVisitor whereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(regExpMySQLOperator, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        regExpMySQLOperator.accept(whereDencryptExpressionVisitor);
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
        //将每一项进行解密处理
        ExpressionList expressionList = groupConcat.getExpressionList();
        List<Expression> newExpressions = new ArrayList<>();
        for (Expression exp : expressionList.getExpressions()) {
            SDecryptExpressionVisitor sDecryptExpressionVisitor = new SDecryptExpressionVisitor(null, exp, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
            exp.accept(sDecryptExpressionVisitor);
            newExpressions.add(sDecryptExpressionVisitor.getExpression());
        }

        //替换解密后的表达式
        expressionList.setExpressions(newExpressions);
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
