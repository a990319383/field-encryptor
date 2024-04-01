package com.sangsang.visitor.encrtptor.where;

import com.sangsang.cache.TableCache;
import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.util.JsqlparserUtil;
import com.sangsang.util.StringUtils;
import com.sangsang.visitor.encrtptor.select.DecryptSelectVisitor;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;

import java.util.*;

/**
 * 备注：这里使用的jsqlparser的sql解析的访问者
 * 作用：将where 条件后面需要加解密的字段进行解密处理
 * 目前只实现了常见的场景
 *
 * @author liutangqi
 * @date 2024/2/20 14:47
 */
public class DencryptWhereFieldParseVisitor extends BaseFieldParseTable implements ExpressionVisitor {

    public DencryptWhereFieldParseVisitor(int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
    }

    @Override
    public void visit(BitwiseRightShift aThis) {

    }

    @Override
    public void visit(BitwiseLeftShift aThis) {

    }

    @Override
    public void visit(NullValue nullValue) {
        System.out.println(nullValue);
    }

    /**
     * 列运算
     *
     * @author liutangqi
     * @date 2024/3/15 10:07
     * @Param [function]
     **/
    @Override
    public void visit(Function function) {
        List<Expression> expressions = Optional.ofNullable(function.getParameters())
                .map(ExpressionList::getExpressions)
                .orElse(new ArrayList<>());
        for (Expression expression : expressions) {
            expression.accept(this);
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

    /**
     * 括号括起来的一堆条件
     *
     * @author liutangqi
     * @date 2024/2/28 18:39
     * @Param [parenthesis]
     **/
    @Override
    public void visit(Parenthesis parenthesis) {
        //解析括号括起来的表达式
        parenthesis.getExpression().accept(this);
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
        System.out.println(subtraction);
    }

    @Override
    public void visit(AndExpression andExpression) {
        andExpression.getLeftExpression().accept(this);
        andExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(OrExpression orExpression) {
        //解析左右表达式
        orExpression.getLeftExpression().accept(this);
        orExpression.getRightExpression().accept(this);
    }


    @Override
    public void visit(Between between) {
        System.out.println(between);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        //如果左右侧都是 Column 类型的话，不用处理加密，都是数据库的字段，都是加密的
        if ((equalsTo.getLeftExpression() instanceof Column) && equalsTo.getRightExpression() instanceof Column) {
            return;
        }

        //解析左右两边的表达式
        equalsTo.getLeftExpression().accept(this);
        equalsTo.getRightExpression().accept(this);
    }

    /**
     * >
     *
     * @author liutangqi
     * @date 2024/3/6 15:53
     * @Param [greaterThan]
     **/
    @Override
    public void visit(GreaterThan greaterThan) {
        //解析左右表达式
        greaterThan.getLeftExpression().accept(this);
        greaterThan.getRightExpression().accept(this);
    }

    /**
     * >=
     *
     * @author liutangqi
     * @date 2024/3/6 15:54
     * @Param [greaterThanEquals]
     **/
    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        //解析左右表达式
        greaterThanEquals.getLeftExpression().accept(this);
        greaterThanEquals.getRightExpression().accept(this);
    }

    @Override
    public void visit(InExpression inExpression) {
        //解析表达式
        inExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {

    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        isNullExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        //解析表达式
        isBooleanExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        //解析左右表达式
        likeExpression.getLeftExpression().accept(this);
        likeExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(MinorThan minorThan) {
        //解析左右表达式
        minorThan.getLeftExpression().accept(this);
        minorThan.getRightExpression().accept(this);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        //解析左右表达式
        minorThanEquals.getLeftExpression().accept(this);
        minorThanEquals.getRightExpression().accept(this);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        //解析左右表达式
        notEqualsTo.getLeftExpression().accept(this);
        notEqualsTo.getRightExpression().accept(this);
    }

    /**
     * 其它的表达式，最终都会通过这个类型来进行解析
     * 注意：这里没办法将Column 转化为 Function 对象，没办法所以直接将新的解密表达式替换了原字段
     *
     * @author liutangqi
     * @date 2024/2/28 17:00
     * @Param [tableColumn]
     **/
    @Override
    public void visit(Column column) {
        //1.先在当前的表拥有的全部字段中解析当前字段
        ColumnTableDto columnTableDto = JsqlparserUtil.parseColumn(column, this.getLayer(), this.getLayerFieldTableMap());

        //2.如果当前表拥有的全部字段中找不到匹配字段，则说明可能where 后面接的条件是使用的select的别名
        if (StringUtils.isBlank(columnTableDto.getSourceColumn()) || StringUtils.isBlank(columnTableDto.getSourceTableName())) {
            columnTableDto = JsqlparserUtil.parseColumn(column, this.getLayer(), this.getLayerSelectTableFieldMap());
        }
        String tableTrueName = columnTableDto.getSourceTableName();
        String columTrueName = columnTableDto.getSourceColumn();

        //3. 当前字段不需要解密(实体类上面没有标注@FieldEncrypt注解 或者字段不是来源自真实表)
        if (!columnTableDto.isFromSourceTable() || Optional.ofNullable(TableCache.getTableFieldEncryptInfo())
                .map(m -> m.get(tableTrueName))
                .map(m -> m.get(columTrueName))
                .orElse(null) == null) {
            return;
        }

        //4. 将字段进行解密(注意：这里没有办法将Column 对象转换为 Function 对象，所以这里只能进行字符串组装 ，必须将字段所属表设置为null，不然sql会在函数外面再包一层 表名.)
        String EncryptColumn = SymbolConstant.DECODE + columnTableDto.getTableAliasName() + SymbolConstant.FULL_STOP + column.getColumnName() + "), 'encryptionKey秘钥')";
        column.setTable(null);
        column.setColumnName(EncryptColumn);
    }

    /**
     * 子查询
     * 当exist时会走子查询的逻辑
     *
     * @author liutangqi
     * @date 2024/3/6 15:55
     * @Param [subSelect]
     **/
    @Override
    public void visit(SubSelect subSelect) {
        DecryptSelectVisitor sDecryptSelectVisitor = new DecryptSelectVisitor(this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());

        SelectBody selectBody = subSelect.getSelectBody();
        selectBody.accept(sDecryptSelectVisitor);

        //设置解密后的语句
        subSelect.setSelectBody(selectBody);
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        System.out.println(caseExpression);
    }

    @Override
    public void visit(WhenClause whenClause) {
        System.out.println(whenClause);
    }

    /**
     * exist
     *
     * @author liutangqi
     * @date 2024/3/8 11:32
     * @Param [existsExpression]
     **/
    @Override
    public void visit(ExistsExpression existsExpression) {
        //解析表达式
        existsExpression.getRightExpression().accept(this);

    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {

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
        System.out.println(aThis);
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
}
