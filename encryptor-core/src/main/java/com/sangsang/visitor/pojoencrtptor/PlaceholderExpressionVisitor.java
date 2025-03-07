package com.sangsang.visitor.pojoencrtptor;

import com.sangsang.domain.constants.DecryptConstant;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.PlaceholderFieldParseTable;
import com.sangsang.util.CollectionUtils;
import com.sangsang.util.JsqlparserUtil;
import com.sangsang.visitor.fieldparse.FieldParseParseTableSelectVisitor;
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

/**
 * 解析where 条件中#{}条件入参条件所属的表以及字段
 *
 * @author liutangqi
 * @date 2024/7/11 10:47
 */
public class PlaceholderExpressionVisitor extends PlaceholderFieldParseTable implements ExpressionVisitor {

    /**
     * 当此表达式和上游表达式相关联时，这个是传的上游的表达式
     * 例如：  case 字段 when xxx    的when语句的时候，需要知道这个when语句所属的case后面的字段，这个变量是存储这种情况的case字段的
     */
    private Expression upstreamExpression;

    /**
     * 获取当前层解析对象
     *
     * @author liutangqi
     * @date 2025/3/5 10:40
     * @Param [placeholderFieldParseTable]
     **/
    public static PlaceholderExpressionVisitor newInstanceCurLayer(PlaceholderFieldParseTable placeholderFieldParseTable, Expression upstreamExpression) {
        return new PlaceholderExpressionVisitor(placeholderFieldParseTable, placeholderFieldParseTable.getPlaceholderColumnTableMap(), upstreamExpression);
    }

    /**
     * 获取当前层解析对象
     *
     * @author liutangqi
     * @date 2025/3/5 10:40
     * @Param [placeholderFieldParseTable]
     **/
    public static PlaceholderExpressionVisitor newInstanceCurLayer(PlaceholderFieldParseTable placeholderFieldParseTable) {
        return new PlaceholderExpressionVisitor(placeholderFieldParseTable, placeholderFieldParseTable.getPlaceholderColumnTableMap(), null);
    }

    /**
     * 获取当前层解析对象
     *
     * @author liutangqi
     * @date 2025/3/5 10:40
     * @Param [placeholderFieldParseTable]
     **/
    public static PlaceholderExpressionVisitor newInstanceCurLayer(BaseFieldParseTable baseFieldParseTable, Map<String, ColumnTableDto> placeholderColumnTableMap) {
        return new PlaceholderExpressionVisitor(baseFieldParseTable, placeholderColumnTableMap, null);
    }

    private PlaceholderExpressionVisitor(BaseFieldParseTable baseFieldParseTable, Map<String, ColumnTableDto> placeholderColumnTableMap, Expression upstreamExpression) {
        super(baseFieldParseTable, placeholderColumnTableMap);
        this.upstreamExpression = upstreamExpression;
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
                FieldParseParseTableSelectVisitor fieldParseParseTableSelectVisitor = FieldParseParseTableSelectVisitor.newInstanceFirstLayer();
                selectBody.accept(fieldParseParseTableSelectVisitor);
                //1.2.3 利用这个单独的sql的解析结果，对这个sql的where的#{}占位符进行分析
                PlaceholderSelectVisitor placeholderSelectVisitor = PlaceholderSelectVisitor.newInstanceCurLayer(fieldParseParseTableSelectVisitor, this.getPlaceholderColumnTableMap());
                selectBody.accept(placeholderSelectVisitor);
            }
        } else {
            //2.1 左边不是Column，但是右边是子查询时，需要对子查询的where进行处理  栗子： wher  concat(a.phone,b.name) in (select xxx from 表 where  xxx= ?占位符)
            if (inExpression.getRightExpression() instanceof SubSelect) {
                //备注：右边的子查询是一个完全独立的sql，所以不共用一个解析结果，需要单独解析当前sql中涉及的字段
                FieldParseParseTableSelectVisitor fieldParseParseTableSelectVisitor = FieldParseParseTableSelectVisitor.newInstanceFirstLayer();
                ((SubSelect) inExpression.getRightExpression()).getSelectBody().accept(fieldParseParseTableSelectVisitor);
                //基于新解析的表结构信息 和当前存储？占位符的Map 解析其中的where条件
                PlaceholderExpressionVisitor placeholderWhereExpressionVisitor = PlaceholderExpressionVisitor.newInstanceCurLayer(fieldParseParseTableSelectVisitor, this.getPlaceholderColumnTableMap());
                inExpression.getRightExpression().accept(placeholderWhereExpressionVisitor);
            } else {
                //2.2 当左边的不是Column时（比如左边是列运算，就是Function，不是单纯的列） 栗子： where concat(a.phone,b.name) in ( ?,?)
                // 这种情况不做处理，这种情况#{}占位符所属的字段信息是一个聚合结果，同时来源多张表，不支持此种写法，两个单独的字段聚合后，单独加密和整体加密密文肯定不同
                // 写出这种sql的时候请反省一下自己，表结构是不是有问题，硬要用这种写法的，请使用数据库函数加密的db模式
            }
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
        //注意：exist这种情况，层数不需要加1，这里使用的字段和上级是同一层的
        subSelect.getSelectBody().accept(PlaceholderSelectVisitor.newInstanceCurLayer(this));
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
     * @date 2024/7/31 10:58
     * @Param [caseExpression]
     **/
    @Override
    public void visit(CaseExpression caseExpression) {
        //记录当前的case 后面的所属字段，如果when 语句后面是表达式的话，需要知道case后面的所属字段，才知道是否需要加密
        Expression upstreamExpression = caseExpression.getSwitchExpression();

        //处理when条件
        if (CollectionUtils.isNotEmpty(caseExpression.getWhenClauses())) {
            for (WhenClause whenClause : caseExpression.getWhenClauses()) {
                //这里处理的逻辑在下面的public void visit(WhenClause whenClause)会处理
                PlaceholderExpressionVisitor placeholderWhereExpressionVisitor = PlaceholderExpressionVisitor.newInstanceCurLayer(this, upstreamExpression);
                whenClause.accept(placeholderWhereExpressionVisitor);
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
        if (this.upstreamExpression instanceof Column && whenClause.getWhenExpression().toString().contains(DecryptConstant.PLACEHOLDER)) {
            ColumnTableDto columnTableDto = JsqlparserUtil.parseColumn((Column) this.upstreamExpression, this.getLayer(), this.getLayerFieldTableMap());
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
        //exist 这里会走 SubSelect 子查询的逻辑
        existsExpression.getRightExpression().accept(this);
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
