package com.sangsang.visitor.pojoencrtptor.select;

import com.sangsang.util.CollectionUtils;
import com.sangsang.domain.constants.DecryptConstant;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.PlaceholderFieldParseTable;
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

import java.util.Map;

/**
 * 将select的每一项中需要进行#{}占位符的进行解析
 *
 * @author liutangqi
 * @date 2024/7/14 22:09
 */
public class PlaceholderSelectExpressionVisitor extends PlaceholderFieldParseTable implements ExpressionVisitor {

    /**
     * 当处理    case 字段  when xxx
     * 的when语句的时候，需要知道这个when语句所属的case后面的字段，这个变量是存储这种情况的case字段的
     */
    private Expression switchExpression;

    public void setCaseExpression(Expression switchExpression) {
        this.switchExpression = switchExpression;
    }

    public PlaceholderSelectExpressionVisitor(PlaceholderFieldParseTable placeholderFieldParseTable) {
        super(placeholderFieldParseTable, placeholderFieldParseTable.getPlaceholderColumnTableMap());
    }

    public PlaceholderSelectExpressionVisitor(BaseFieldParseTable baseFieldParseTable, Map<String, ColumnTableDto> placeholderColumnTableMap) {
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
    public void visit(Column column) {

    }

    /**
     * select (select xxx from tb_table) as xxx from
     *
     * @author liutangqi
     * @date 2024/7/14 22:31
     * @Param [subSelect]
     **/
    @Override
    public void visit(SubSelect subSelect) {
        //处理占位符
        subSelect.getSelectBody().accept(new PlaceholderSelectVisitor(this));
    }

    /**
     * case 字段 when xxx then
     * case when 字段=xxx then
     * 只有下面情况的占位符才有字段对应，需要进行处理
     * 情况1: case 表字段  when ?占位符 then xxx
     * 情况2: case when 表字段=? then （这种情况条件在when里面）
     * 情况3：... then 表字段>= ?占位符
     *
     * @author liutangqi
     * @date 2024/7/30 15:39
     * @Param [caseExpression]
     **/
    @Override
    public void visit(CaseExpression caseExpression) {
        //记录当前的case 后面的所属字段，如果when 语句后面是表达式的话，需要知道case后面的所属字段，才知道是否需要加密
        this.switchExpression = caseExpression.getSwitchExpression();

        //处理when条件
        if (CollectionUtils.isNotEmpty(caseExpression.getWhenClauses())) {
            for (WhenClause whenClause : caseExpression.getWhenClauses()) {
                //这里处理的逻辑在下面的public void visit(WhenClause whenClause)会处理
                whenClause.accept(this);
            }
        }

        //else条件 只用处理else中是 表达式的场景  栗子：  case 字段 when xxx then  字段 >= ?占位符  else 字段 >= ?占位符，只有这种情况下，占位符才有对应的表字段信息
        Expression elseExpression = caseExpression.getElseExpression();
        if (elseExpression instanceof BinaryExpression) {
            //如果有一边表达式是 特殊的占位符，则维护占位符对应的表字段信息
            JsqlparserUtil.parseWhereColumTable(this.getLayer(),
                    this.getLayerFieldTableMap(),
                    (BinaryExpression) elseExpression,
                    this.getPlaceholderColumnTableMap());
        }

    }

    /**
     * 上面case when 中的when语句会走这里
     *
     * @author liutangqi
     * @date 2024/7/30 17:35
     * @Param [whenClause]
     **/
    @Override
    public void visit(WhenClause whenClause) {
        //对应case when的情况1： case 表字段  when ?占位符 then xxx
        if (this.switchExpression instanceof Column && whenClause.getWhenExpression().toString().contains(DecryptConstant.PLACEHOLDER)) {
            ColumnTableDto columnTableDto = JsqlparserUtil.parseColumn((Column) this.switchExpression, this.getLayer(), this.getLayerFieldTableMap());
            this.getPlaceholderColumnTableMap().put(whenClause.getWhenExpression().toString(), columnTableDto);
        }

        //对应case when 的情况2： case  when 表字段=?占位符 then
        if (whenClause.getWhenExpression() instanceof BinaryExpression) {
            //如果有一边表达式是 特殊的占位符，则维护占位符对应的表字段信息
            JsqlparserUtil.parseWhereColumTable(this.getLayer(),
                    this.getLayerFieldTableMap(),
                    (BinaryExpression) whenClause.getWhenExpression(),
                    this.getPlaceholderColumnTableMap());
        }

        //对应case when 的情况3  ：... then 表字段>= ?占位符
        Expression thenExpression = whenClause.getThenExpression();
        if (thenExpression instanceof BinaryExpression) {
            //如果有一边表达式是 特殊的占位符，则维护占位符对应的表字段信息
            JsqlparserUtil.parseWhereColumTable(this.getLayer(),
                    this.getLayerFieldTableMap(),
                    (BinaryExpression) thenExpression,
                    this.getPlaceholderColumnTableMap());
        }

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
        JsqlparserUtil.parseWhereColumTable(this.getLayer(),
                this.getLayerFieldTableMap(),
                regExpMySQLOperator,
                this.getPlaceholderColumnTableMap());
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
