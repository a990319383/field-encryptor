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
import org.springframework.util.CollectionUtils;

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

    /**
     * 加解密处理好后的表达式
     * 如果不需要处理，则这个值就是构造函数传入的旧表达式
     */
    private Expression expression;

    public DencryptWhereFieldParseVisitor(Expression expression, int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
        this.expression = expression;
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
     * 列运算
     *
     * @author liutangqi
     * @date 2024/3/15 10:07
     * @Param [function]
     **/
    @Override
    public void visit(Function function) {
        ExpressionList parameters = function.getParameters();
        //没有表达式不处理
        if (parameters == null || CollectionUtils.isEmpty(parameters.getExpressions())) {
            return;
        }

        //表达式
        List<Expression> expressions = new ArrayList<>();
        for (Expression exp : parameters.getExpressions()) {
            DencryptWhereFieldParseVisitor dencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(exp, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
            exp.accept(dencryptWhereFieldParseVisitor);
            expressions.add(dencryptWhereFieldParseVisitor.getExpression());
        }

        //替换原有表达式
        parameters.setExpressions(expressions);
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
        Expression exp = parenthesis.getExpression();
        DencryptWhereFieldParseVisitor dencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(exp, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        exp.accept(dencryptWhereFieldParseVisitor);
        parenthesis.setExpression(dencryptWhereFieldParseVisitor.getExpression());
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
        Expression leftExpression = andExpression.getLeftExpression();
        DencryptWhereFieldParseVisitor leftDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftDencryptWhereFieldParseVisitor);
        andExpression.setLeftExpression(leftDencryptWhereFieldParseVisitor.getExpression());

        Expression rightExpression = andExpression.getRightExpression();
        DencryptWhereFieldParseVisitor rightDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightDencryptWhereFieldParseVisitor);
        andExpression.setRightExpression(rightDencryptWhereFieldParseVisitor.getExpression());
    }

    @Override
    public void visit(OrExpression orExpression) {
        //解析左右表达式
        Expression leftExpression = orExpression.getLeftExpression();
        DencryptWhereFieldParseVisitor leftDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftDencryptWhereFieldParseVisitor);
        orExpression.setLeftExpression(leftDencryptWhereFieldParseVisitor.getExpression());

        Expression rightExpression = orExpression.getRightExpression();
        DencryptWhereFieldParseVisitor rightDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightDencryptWhereFieldParseVisitor);
        orExpression.setRightExpression(rightDencryptWhereFieldParseVisitor.getExpression());
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
        Expression leftExpression = equalsTo.getLeftExpression();
        DencryptWhereFieldParseVisitor leftDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftDencryptWhereFieldParseVisitor);
        equalsTo.setLeftExpression(leftDencryptWhereFieldParseVisitor.getExpression());

        Expression rightExpression = equalsTo.getRightExpression();
        DencryptWhereFieldParseVisitor rightDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightDencryptWhereFieldParseVisitor);
        equalsTo.setRightExpression(rightDencryptWhereFieldParseVisitor.getExpression());
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
        Expression leftExpression = greaterThan.getLeftExpression();
        DencryptWhereFieldParseVisitor leftDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftDencryptWhereFieldParseVisitor);
        greaterThan.setLeftExpression(leftDencryptWhereFieldParseVisitor.getExpression());

        Expression rightExpression = greaterThan.getRightExpression();
        DencryptWhereFieldParseVisitor rightDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightDencryptWhereFieldParseVisitor);
        greaterThan.setRightExpression(rightDencryptWhereFieldParseVisitor.getExpression());
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
        Expression leftExpression = greaterThanEquals.getLeftExpression();
        DencryptWhereFieldParseVisitor leftDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftDencryptWhereFieldParseVisitor);
        greaterThanEquals.setLeftExpression(leftDencryptWhereFieldParseVisitor.getExpression());

        Expression rightExpression = greaterThanEquals.getRightExpression();
        DencryptWhereFieldParseVisitor rightDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightDencryptWhereFieldParseVisitor);
        greaterThanEquals.setRightExpression(rightDencryptWhereFieldParseVisitor.getExpression());
    }

    @Override
    public void visit(InExpression inExpression) {
        //解析表达式
        Expression leftExpression = inExpression.getLeftExpression();
        DencryptWhereFieldParseVisitor leftDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftDencryptWhereFieldParseVisitor);
        inExpression.setLeftExpression(leftDencryptWhereFieldParseVisitor.getExpression());
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {

    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        Expression leftExpression = isNullExpression.getLeftExpression();
        DencryptWhereFieldParseVisitor leftDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftDencryptWhereFieldParseVisitor);
        isNullExpression.setLeftExpression(leftDencryptWhereFieldParseVisitor.getExpression());
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        //解析表达式
        Expression leftExpression = isBooleanExpression.getLeftExpression();
        DencryptWhereFieldParseVisitor leftDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftDencryptWhereFieldParseVisitor);
        isBooleanExpression.setLeftExpression(leftDencryptWhereFieldParseVisitor.getExpression());
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        //解析左右表达式
        Expression leftExpression = likeExpression.getLeftExpression();
        DencryptWhereFieldParseVisitor leftDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftDencryptWhereFieldParseVisitor);
        likeExpression.setLeftExpression(leftDencryptWhereFieldParseVisitor.getExpression());

        Expression rightExpression = likeExpression.getRightExpression();
        DencryptWhereFieldParseVisitor rightDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightDencryptWhereFieldParseVisitor);
        likeExpression.setRightExpression(rightDencryptWhereFieldParseVisitor.getExpression());
    }

    @Override
    public void visit(MinorThan minorThan) {
        //解析左右表达式
        Expression leftExpression = minorThan.getLeftExpression();
        DencryptWhereFieldParseVisitor leftDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftDencryptWhereFieldParseVisitor);
        minorThan.setLeftExpression(leftDencryptWhereFieldParseVisitor.getExpression());

        Expression rightExpression = minorThan.getRightExpression();
        DencryptWhereFieldParseVisitor rightDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightDencryptWhereFieldParseVisitor);
        minorThan.setRightExpression(rightDencryptWhereFieldParseVisitor.getExpression());
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        //解析左右表达式
        Expression leftExpression = minorThanEquals.getLeftExpression();
        DencryptWhereFieldParseVisitor leftDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftDencryptWhereFieldParseVisitor);
        minorThanEquals.setLeftExpression(leftDencryptWhereFieldParseVisitor.getExpression());

        Expression rightExpression = minorThanEquals.getRightExpression();
        DencryptWhereFieldParseVisitor rightDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightDencryptWhereFieldParseVisitor);
        minorThanEquals.setRightExpression(rightDencryptWhereFieldParseVisitor.getExpression());
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        //如果左右侧都是 Column 类型的话，不用处理加密，都是数据库的字段，都是加密的
        if ((notEqualsTo.getLeftExpression() instanceof Column) && notEqualsTo.getRightExpression() instanceof Column) {
            return;
        }

        //解析左右表达式
        Expression leftExpression = notEqualsTo.getLeftExpression();
        DencryptWhereFieldParseVisitor leftDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftDencryptWhereFieldParseVisitor);
        notEqualsTo.setLeftExpression(leftDencryptWhereFieldParseVisitor.getExpression());

        Expression rightExpression = notEqualsTo.getRightExpression();
        DencryptWhereFieldParseVisitor rightDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightDencryptWhereFieldParseVisitor);
        notEqualsTo.setRightExpression(rightDencryptWhereFieldParseVisitor.getExpression());
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

        //3. 当前字段不需要解密(实体类上面没有标注@FieldEncryptor注解 或者字段不是来源自真实表)
        if (!columnTableDto.isFromSourceTable() || Optional.ofNullable(TableCache.getTableFieldEncryptInfo())
                .map(m -> m.get(tableTrueName))
                .map(m -> m.get(columTrueName))
                .orElse(null) == null) {
            return;
        }

        //4. 将字段进行解密(注意：这里没有办法将Column 对象转换为 Function 对象，所以这里只能进行字符串组装 ，必须将字段所属表设置为null，不然sql会在函数外面再包一层 表名.)
        Function base64Function = new Function();
        base64Function.setName(SymbolConstant.FROM_BASE64);
        base64Function.setParameters(new ExpressionList(column));
        Function decryptFunction = new Function();
        decryptFunction.setName(SymbolConstant.AES_DECRYPT);
        decryptFunction.setParameters(new ExpressionList(base64Function, new StringValue("encryptionKey秘钥")));
        this.expression = decryptFunction;

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
        Expression rightExpression = existsExpression.getRightExpression();
        DencryptWhereFieldParseVisitor rightDencryptWhereFieldParseVisitor = new DencryptWhereFieldParseVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightDencryptWhereFieldParseVisitor);
        existsExpression.setRightExpression(rightDencryptWhereFieldParseVisitor.getExpression());
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
