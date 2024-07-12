package com.sangsang.visitor.beanencrtptor.where;

import com.sangsang.domain.constants.NumberConstant;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.domain.dto.PlaceholderFieldParseTable;
import com.sangsang.util.JsqlparserUtil;
import com.sangsang.visitor.beanencrtptor.select.PlaceholderSelectVisitor;
import com.sangsang.visitor.encrtptor.fieldparse.FieldParseParseTableSelectVisitor;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.Map;
import java.util.Set;

/**
 * 解析where 条件中#{}条件入参条件所属的表以及字段
 *
 * @author liutangqi
 * @date 2024/7/11 10:47
 */
public class PlaceholderWhereExpressionVisitor extends PlaceholderFieldParseTable implements ExpressionVisitor {

    public PlaceholderWhereExpressionVisitor(PlaceholderFieldParseTable placeholderFieldParseTable) {
        super(placeholderFieldParseTable, placeholderFieldParseTable.getPlaceholderColumnTableMap());
    }

    public PlaceholderWhereExpressionVisitor(BaseFieldParseTable baseFieldParseTable, Map<String, ColumnTableDto> placeholderColumnTableMap) {
        super(baseFieldParseTable, placeholderColumnTableMap);
    }


    @Override
    public void visit(BitwiseRightShift bitwiseRightShift) {

    }

    @Override
    public void visit(BitwiseLeftShift bitwiseLeftShift) {

    }

    @Override
    public void visit(NullValue nullValue) {

    }

    @Override
    public void visit(Function function) {

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

    /**
     * 括起来的一堆条件
     *
     * @author liutangqi
     * @date 2024/7/12 14:41
     * @Param [parenthesis]
     **/
    @Override
    public void visit(Parenthesis parenthesis) {
        //解析括号括起来的表达式
        Expression exp = parenthesis.getExpression();
        exp.accept(this);
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
    public void visit(IntegerDivision integerDivision) {

    }

    @Override
    public void visit(Multiplication multiplication) {

    }

    @Override
    public void visit(Subtraction subtraction) {

    }

    @Override
    public void visit(AndExpression andExpression) {
        //类似递归解析左右两边的表达式
        Expression leftExpression = andExpression.getLeftExpression();
        leftExpression.accept(this);

        Expression rightExpression = andExpression.getRightExpression();
        rightExpression.accept(this);
    }

    @Override
    public void visit(OrExpression orExpression) {
        //类似递归解析左右两边的表达式
        Expression leftExpression = orExpression.getLeftExpression();
        leftExpression.accept(this);

        Expression rightExpression = orExpression.getRightExpression();
        rightExpression.accept(this);

    }

    @Override
    public void visit(XorExpression xorExpression) {
        //类似递归解析左右两边的表达式
        Expression leftExpression = xorExpression.getLeftExpression();
        leftExpression.accept(this);

        Expression rightExpression = xorExpression.getRightExpression();
        rightExpression.accept(this);
    }

    @Override
    public void visit(Between between) {

    }

    /**
     * 左右表达式，当有一边表达式是 我们替换的占位符，有一边是Column，则将他们对应关系维护进结果集中
     *
     * @author liutangqi
     * @date 2024/7/11 11:08
     * @Param [equalsTo]
     **/
    @Override
    public void visit(EqualsTo equalsTo) {
        //如果有一边表达式是 特殊的占位符，则维护占位符对应的表字段信息
        JsqlparserUtil.parseWhereColumTable(this.getLayer(),
                this.getLayerFieldTableMap(),
                equalsTo,
                this.getPlaceholderColumnTableMap());
    }

    @Override
    public void visit(GreaterThan greaterThan) {

    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {

    }


    @Override
    public void visit(InExpression inExpression) {
        //1.当前左边表达式是Column
        if (inExpression.getLeftExpression() instanceof Column) {
            //1.1 右边是 (aaa,bbb,ccc)  Column in (aaa,bbb,ccc) 这种 如果右边是预编译的参数 将右边的#{} 的关系维护到结果集map中
            if (inExpression.getRightItemsList() != null && inExpression.getRightItemsList() instanceof ExpressionList) {
                ExpressionList rightItemsList = (ExpressionList) inExpression.getRightItemsList();
                rightItemsList.getExpressions().forEach(f -> JsqlparserUtil.parseWhereColumTable(this.getLayer(),
                        this.getLayerFieldTableMap(),
                        inExpression.getLeftExpression(),
                        f,
                        this.getPlaceholderColumnTableMap()));

            }

            //1.2 右边是子查询 Column in (select xxx from xxx) 这种: 只需要解析子查询的where 中的占位符
            if (inExpression.getRightExpression() != null && inExpression.getRightExpression() instanceof SubSelect) {
                //1.2.1 取出右边的select语句
                SelectBody selectBody = ((SubSelect) inExpression.getRightExpression()).getSelectBody();
                //1.2.2 因为这个sql是一个完全独立的sql，所以单独解析这个sql拥有的字段信息
                FieldParseParseTableSelectVisitor fieldParseParseTableSelectVisitor = new FieldParseParseTableSelectVisitor(NumberConstant.ONE, null, null);
                selectBody.accept(fieldParseParseTableSelectVisitor);
                //1.2.3 利用这个单独的sql的解析结果，对这个sql的where的#{}占位符进行分析
                PlaceholderSelectVisitor placeholderSelectVisitor = new PlaceholderSelectVisitor(fieldParseParseTableSelectVisitor, this.getPlaceholderColumnTableMap());
                selectBody.accept(placeholderSelectVisitor);
            }
        } else {
            //2. 当左边的不是Column时（比如左边是列运算，就是Function，不是单纯的列） 栗子： where concat(a.phone,b.name) in ( 'xxx','xxx')
            // 这种情况不做处理，这种情况#{}占位符所属的字段信息是一个聚合结果，同时来源多张表，不支持此种写法，两个单独的字段聚合后，单独加密和整体加密密文肯定不同
            // 写出这种sql的时候请反省一下自己，硬要用这种写法的，请使用数据库函数加密的模式
        }


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
        //如果有一边表达式是 特殊的占位符，则维护占位符对应的表字段信息
        JsqlparserUtil.parseWhereColumTable(this.getLayer(),
                this.getLayerFieldTableMap(),
                likeExpression,
                this.getPlaceholderColumnTableMap());
    }

    @Override
    public void visit(MinorThan minorThan) {

    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {

    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        //如果有一边表达式是 特殊的占位符，则维护占位符对应的表字段信息
        JsqlparserUtil.parseWhereColumTable(this.getLayer(),
                this.getLayerFieldTableMap(),
                notEqualsTo,
                this.getPlaceholderColumnTableMap());
    }

    @Override
    public void visit(Column column) {

    }

    /**
     * 子查询
     * 当exist时会走子查询的逻辑
     *
     * @author liutangqi
     * @date 2024/7/12 10:18
     * @Param [subSelect]
     **/
    @Override
    public void visit(SubSelect subSelect) {
        // todo-ltq
        System.out.println("***************SubSelect********************");

    }

    @Override
    public void visit(CaseExpression caseExpression) {

    }

    @Override
    public void visit(WhenClause whenClause) {

    }

    @Override
    public void visit(ExistsExpression existsExpression) {

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
    public void visit(CastExpression castExpression) {

    }

    @Override
    public void visit(TryCastExpression tryCastExpression) {

    }

    @Override
    public void visit(Modulo modulo) {

    }

    @Override
    public void visit(AnalyticExpression analyticExpression) {

    }

    @Override
    public void visit(ExtractExpression extractExpression) {

    }

    @Override
    public void visit(IntervalExpression intervalExpression) {

    }

    @Override
    public void visit(OracleHierarchicalExpression oracleHierarchicalExpression) {

    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {

    }

    @Override
    public void visit(JsonExpression jsonExpression) {

    }

    @Override
    public void visit(JsonOperator jsonOperator) {

    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {

    }

    @Override
    public void visit(UserVariable userVariable) {

    }

    @Override
    public void visit(NumericBind numericBind) {

    }

    @Override
    public void visit(KeepExpression keepExpression) {

    }

    @Override
    public void visit(MySQLGroupConcat mySQLGroupConcat) {

    }

    @Override
    public void visit(ValueListExpression valueListExpression) {

    }

    @Override
    public void visit(RowConstructor rowConstructor) {

    }

    @Override
    public void visit(RowGetExpression rowGetExpression) {

    }

    @Override
    public void visit(OracleHint oracleHint) {

    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {

    }

    @Override
    public void visit(DateTimeLiteralExpression dateTimeLiteralExpression) {

    }

    @Override
    public void visit(NotExpression notExpression) {

    }

    @Override
    public void visit(NextValExpression nextValExpression) {

    }

    @Override
    public void visit(CollateExpression collateExpression) {

    }

    @Override
    public void visit(SimilarToExpression similarToExpression) {

    }

    @Override
    public void visit(ArrayExpression arrayExpression) {

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
