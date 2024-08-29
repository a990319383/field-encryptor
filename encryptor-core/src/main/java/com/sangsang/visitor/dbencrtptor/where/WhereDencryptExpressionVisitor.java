package com.sangsang.visitor.dbencrtptor.where;

import com.sangsang.cache.FieldEncryptorPatternCache;
import com.sangsang.cache.TableCache;
import com.sangsang.domain.constants.NumberConstant;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.util.JsqlparserUtil;
import com.sangsang.util.StringUtils;
import com.sangsang.visitor.fieldparse.FieldParseParseTableSelectVisitor;
import com.sangsang.visitor.dbencrtptor.select.SDecryptSelectVisitor;
import com.sangsang.visitor.dbencrtptor.selectonlywhere.SOWDecryptSelectVisitor;
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
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 作用：将where 条件后面需要加解密的字段进行解密处理
 * 备注：where 条件加解密的入口！！！
 *
 * @author liutangqi
 * @date 2024/2/20 14:47
 */
public class WhereDencryptExpressionVisitor extends BaseFieldParseTable implements ExpressionVisitor {

    /**
     * 加解密处理好后的表达式
     * 如果不需要处理，则这个值就是构造函数传入的旧表达式
     */
    private Expression expression;

    public WhereDencryptExpressionVisitor(Expression expression, int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
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
            WhereDencryptExpressionVisitor whereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(exp, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
            exp.accept(whereDencryptExpressionVisitor);
            expressions.add(whereDencryptExpressionVisitor.getExpression());
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
        WhereDencryptExpressionVisitor whereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(exp, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        exp.accept(whereDencryptExpressionVisitor);
        parenthesis.setExpression(whereDencryptExpressionVisitor.getExpression());
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
        WhereDencryptExpressionVisitor leftWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftWhereDencryptExpressionVisitor);
        andExpression.setLeftExpression(leftWhereDencryptExpressionVisitor.getExpression());

        Expression rightExpression = andExpression.getRightExpression();
        WhereDencryptExpressionVisitor rightWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightWhereDencryptExpressionVisitor);
        andExpression.setRightExpression(rightWhereDencryptExpressionVisitor.getExpression());
    }

    @Override
    public void visit(OrExpression orExpression) {
        //解析左右表达式
        Expression leftExpression = orExpression.getLeftExpression();
        WhereDencryptExpressionVisitor leftWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftWhereDencryptExpressionVisitor);
        orExpression.setLeftExpression(leftWhereDencryptExpressionVisitor.getExpression());

        Expression rightExpression = orExpression.getRightExpression();
        WhereDencryptExpressionVisitor rightWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightWhereDencryptExpressionVisitor);
        orExpression.setRightExpression(rightWhereDencryptExpressionVisitor.getExpression());
    }

    @Override
    public void visit(XorExpression xorExpression) {

    }


    @Override
    public void visit(Between between) {
        System.out.println(between);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        //1.如果左右侧都是 Column 类型的话，两边都需要加密或者两边都不需要加密时，不需要处理
        if ((equalsTo.getLeftExpression() instanceof Column) && (equalsTo.getRightExpression() instanceof Column)) {
            boolean leftNeedEncrypt = JsqlparserUtil.needEncrypt((Column) equalsTo.getLeftExpression(), this.getLayer(), this.getLayerFieldTableMap());
            boolean rightNeedEncrypt = JsqlparserUtil.needEncrypt((Column) equalsTo.getRightExpression(), this.getLayer(), this.getLayerFieldTableMap());
            if ((leftNeedEncrypt && rightNeedEncrypt) || (!leftNeedEncrypt && !rightNeedEncrypt)) {
                return;
            }
        }

        //2.左边是 Column 右边不是 Column ，避免索引失效，将非Column进行加密处理即可
        if ((equalsTo.getLeftExpression() instanceof Column) && !(equalsTo.getRightExpression() instanceof Column)) {
            //Column 是需要加密的字段则将非Column进行加密
            if (JsqlparserUtil.needEncrypt((Column) equalsTo.getLeftExpression(), this.getLayer(), this.getLayerFieldTableMap())) {
                Expression newRightExpression = FieldEncryptorPatternCache.getInstance().encryption(equalsTo.getRightExpression());
                equalsTo.setRightExpression(newRightExpression);
            }
            return;
        }

        //3. 左边不是Column 右边是 Column  ，避免索引失效，将非Column进行加密处理即可
        if ((equalsTo.getRightExpression() instanceof Column) && !(equalsTo.getLeftExpression() instanceof Column)) {
            //Column 是需要加密的字段则将非Column进行加密
            if (JsqlparserUtil.needEncrypt((Column) equalsTo.getRightExpression(), this.getLayer(), this.getLayerFieldTableMap())) {
                Expression newLeftExpression = FieldEncryptorPatternCache.getInstance().encryption(equalsTo.getLeftExpression());
                equalsTo.setLeftExpression(newLeftExpression);
            }
            return;
        }

        //4.其它情况（两边都不是Column） 解析左右两边的表达式
        Expression leftExpression = equalsTo.getLeftExpression();
        WhereDencryptExpressionVisitor leftWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftWhereDencryptExpressionVisitor);
        equalsTo.setLeftExpression(leftWhereDencryptExpressionVisitor.getExpression());

        Expression rightExpression = equalsTo.getRightExpression();
        WhereDencryptExpressionVisitor rightWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightWhereDencryptExpressionVisitor);
        equalsTo.setRightExpression(rightWhereDencryptExpressionVisitor.getExpression());
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
        WhereDencryptExpressionVisitor leftWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftWhereDencryptExpressionVisitor);
        greaterThan.setLeftExpression(leftWhereDencryptExpressionVisitor.getExpression());

        Expression rightExpression = greaterThan.getRightExpression();
        WhereDencryptExpressionVisitor rightWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightWhereDencryptExpressionVisitor);
        greaterThan.setRightExpression(rightWhereDencryptExpressionVisitor.getExpression());
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
        WhereDencryptExpressionVisitor leftWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftWhereDencryptExpressionVisitor);
        greaterThanEquals.setLeftExpression(leftWhereDencryptExpressionVisitor.getExpression());

        Expression rightExpression = greaterThanEquals.getRightExpression();
        WhereDencryptExpressionVisitor rightWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightWhereDencryptExpressionVisitor);
        greaterThanEquals.setRightExpression(rightWhereDencryptExpressionVisitor.getExpression());
    }

    /**
     * 当左边是 Column 时，针对一些场景进行优化，避免无意义的多次加解密
     *
     * @author liutangqi
     * @date 2024/8/29 10:28
     * @Param [inExpression]
     **/
    @Override
    public void visit(InExpression inExpression) {
        //1.当前左边表达式是Column时，针对下面两种情况做出优化，避免多次无意义的加解密
        if (inExpression.getLeftExpression() instanceof Column) {
            //判断左边 Column 是否需要加解密
            boolean columnNeedEncrypt = JsqlparserUtil.needEncrypt((Column) inExpression.getLeftExpression(), this.getLayer(), this.getLayerFieldTableMap());
            //1.1 右边是 (?,?,?)  Column in (?,?,?) 这种 : 只对右边进行加密处理
            if (columnNeedEncrypt && inExpression.getRightItemsList() != null) {
                inExpression.getRightItemsList().accept(new WhereDencryptItemsListVisitor(this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap()));
                return;
            }
            //1.2 右边是子查询  Column in (select xxx from xxx) 并且子查询select 全部需要加解密 && 左边的 Column也需要加解密
            //这种情况只用处理子查询的where条件
            Expression rightExpression = inExpression.getRightExpression();
            if (rightExpression != null && (rightExpression instanceof SubSelect)) {
                //1.2.1 拿出右边子查询的sql
                SelectBody selectBody = ((SubSelect) inExpression.getRightExpression()).getSelectBody();
                //1.2.2 因为这个sql是一个完全独立的sql，所以单独解析这个sql拥有的字段信息
                FieldParseParseTableSelectVisitor fieldParseParseTableSelectVisitor = new FieldParseParseTableSelectVisitor(NumberConstant.ONE, null, null);
                selectBody.accept(fieldParseParseTableSelectVisitor);
                //1.2.3 select 需要加解密 && 左边的 Column也需要加解密 这种情况，左边的Column不需要处理，子查询的select也不需要处理，只需要处理where
                if (columnNeedEncrypt && JsqlparserUtil.needEncryptAll(fieldParseParseTableSelectVisitor.getLayer(), fieldParseParseTableSelectVisitor.getLayerSelectTableFieldMap())) {
                    SOWDecryptSelectVisitor sowDecryptSelectVisitor = new SOWDecryptSelectVisitor(fieldParseParseTableSelectVisitor.getLayer(), fieldParseParseTableSelectVisitor.getLayerSelectTableFieldMap(), fieldParseParseTableSelectVisitor.getLayerFieldTableMap());
                    selectBody.accept(sowDecryptSelectVisitor);
                    return;
                }
            }
        }


        //2.其它情况，左边的表达式和右边的子查询都需要单独处理，但是右边的 Column in (?,?,?) 这种不需要处理
        //2.1解析左边表达式
        Expression leftExpression = inExpression.getLeftExpression();
        WhereDencryptExpressionVisitor leftWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftWhereDencryptExpressionVisitor);
        inExpression.setLeftExpression(leftWhereDencryptExpressionVisitor.getExpression());
        //2.2解析右边表达式(右边是子查询)
        Expression rightExpression = inExpression.getRightExpression();
        if (rightExpression != null && (rightExpression instanceof SubSelect)) {
            //备注：右边的子查询是一个完全独立的sql，所以不共用一个解析结果，需要单独解析当前sql中涉及的字段
            FieldParseParseTableSelectVisitor fieldParseParseTableSelectVisitor = new FieldParseParseTableSelectVisitor(NumberConstant.ONE, null, null);
            ((SubSelect) rightExpression).getSelectBody().accept(fieldParseParseTableSelectVisitor);
            //根据上面解析出来sql拥有的字段信息，将子查询进行加解密
            WhereDencryptExpressionVisitor rightWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(rightExpression, fieldParseParseTableSelectVisitor.getLayer(), fieldParseParseTableSelectVisitor.getLayerSelectTableFieldMap(), fieldParseParseTableSelectVisitor.getLayerFieldTableMap());
            rightExpression.accept(rightWhereDencryptExpressionVisitor);
            inExpression.setRightExpression(rightWhereDencryptExpressionVisitor.getExpression());
        }

    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {

    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        //is null / is not null 不需要加解密
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        //解析表达式
        Expression leftExpression = isBooleanExpression.getLeftExpression();
        WhereDencryptExpressionVisitor leftWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftWhereDencryptExpressionVisitor);
        isBooleanExpression.setLeftExpression(leftWhereDencryptExpressionVisitor.getExpression());
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        //解析左右表达式
        Expression leftExpression = likeExpression.getLeftExpression();
        WhereDencryptExpressionVisitor leftWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftWhereDencryptExpressionVisitor);
        likeExpression.setLeftExpression(leftWhereDencryptExpressionVisitor.getExpression());

        Expression rightExpression = likeExpression.getRightExpression();
        WhereDencryptExpressionVisitor rightWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightWhereDencryptExpressionVisitor);
        likeExpression.setRightExpression(rightWhereDencryptExpressionVisitor.getExpression());
    }

    @Override
    public void visit(MinorThan minorThan) {
        //解析左右表达式
        Expression leftExpression = minorThan.getLeftExpression();
        WhereDencryptExpressionVisitor leftWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftWhereDencryptExpressionVisitor);
        minorThan.setLeftExpression(leftWhereDencryptExpressionVisitor.getExpression());

        Expression rightExpression = minorThan.getRightExpression();
        WhereDencryptExpressionVisitor rightWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightWhereDencryptExpressionVisitor);
        minorThan.setRightExpression(rightWhereDencryptExpressionVisitor.getExpression());
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        //解析左右表达式
        Expression leftExpression = minorThanEquals.getLeftExpression();
        WhereDencryptExpressionVisitor leftWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftWhereDencryptExpressionVisitor);
        minorThanEquals.setLeftExpression(leftWhereDencryptExpressionVisitor.getExpression());

        Expression rightExpression = minorThanEquals.getRightExpression();
        WhereDencryptExpressionVisitor rightWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightWhereDencryptExpressionVisitor);
        minorThanEquals.setRightExpression(rightWhereDencryptExpressionVisitor.getExpression());
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        //1.如果左右侧都是 Column 类型的话，两边都需要加密或者两边都不需要加密时，不需要处理
        if ((notEqualsTo.getLeftExpression() instanceof Column) && notEqualsTo.getRightExpression() instanceof Column) {
            boolean leftNeedEncrypt = JsqlparserUtil.needEncrypt((Column) notEqualsTo.getLeftExpression(), this.getLayer(), this.getLayerFieldTableMap());
            boolean rightNeedEncrypt = JsqlparserUtil.needEncrypt((Column) notEqualsTo.getRightExpression(), this.getLayer(), this.getLayerFieldTableMap());
            if ((leftNeedEncrypt && rightNeedEncrypt) || (!leftNeedEncrypt && !rightNeedEncrypt)) {
                return;
            }
        }

        //2.左边是 Column 右边不是 Column ，避免索引失效，将非Column进行加密处理即可
        if ((notEqualsTo.getLeftExpression() instanceof Column) && !(notEqualsTo.getRightExpression() instanceof Column)) {
            //Column 是需要加密的字段则将非Column进行加密
            if (JsqlparserUtil.needEncrypt((Column) notEqualsTo.getLeftExpression(), this.getLayer(), this.getLayerFieldTableMap())) {
                Expression newRightExpression = FieldEncryptorPatternCache.getInstance().encryption(notEqualsTo.getRightExpression());
                notEqualsTo.setRightExpression(newRightExpression);
            }
            return;
        }

        //3. 左边不是Column 右边是 Column  ，避免索引失效，将非Column进行加密处理即可
        if ((notEqualsTo.getRightExpression() instanceof Column) && !(notEqualsTo.getLeftExpression() instanceof Column)) {
            //Column 是需要加密的字段则将非Column进行加密
            if (JsqlparserUtil.needEncrypt((Column) notEqualsTo.getRightExpression(), this.getLayer(), this.getLayerFieldTableMap())) {
                Expression newLeftExpression = FieldEncryptorPatternCache.getInstance().encryption(notEqualsTo.getLeftExpression());
                notEqualsTo.setLeftExpression(newLeftExpression);
            }
            return;
        }

        //4.其它情况（两边都不是Column） 解析左右两边的表达式
        Expression leftExpression = notEqualsTo.getLeftExpression();
        WhereDencryptExpressionVisitor leftWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftWhereDencryptExpressionVisitor);
        notEqualsTo.setLeftExpression(leftWhereDencryptExpressionVisitor.getExpression());

        Expression rightExpression = notEqualsTo.getRightExpression();
        WhereDencryptExpressionVisitor rightWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightWhereDencryptExpressionVisitor);
        notEqualsTo.setRightExpression(rightWhereDencryptExpressionVisitor.getExpression());
    }

    /**
     * 其它的表达式，除了 = ，！= , in 这种等值条件以为 ，最终都会通过这个类型来进行解析
     * 备注：等值条件为了避免列运算影响原有的索引使用情况，不对Column进行运算
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
                .map(m -> JsqlparserUtil.getValueIgnoreFloat(m, columTrueName))
                .orElse(null) == null) {
            return;
        }

        //4. 将字段进行解密
        Expression decryptFunction = FieldEncryptorPatternCache.getInstance().decryption(column);
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
        //注意：exist这种情况，层数不需要加1，这里使用的字段和上级是同一层的
        SDecryptSelectVisitor sDecryptSelectVisitor = new SDecryptSelectVisitor(this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());

        SelectBody selectBody = subSelect.getSelectBody();
        selectBody.accept(sDecryptSelectVisitor);

        //设置解密后的语句
        subSelect.setSelectBody(selectBody);
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        //处理case的条件
        Expression switchExpression = caseExpression.getSwitchExpression();
        if (switchExpression != null) {
            WhereDencryptExpressionVisitor expressionVisitor = new WhereDencryptExpressionVisitor(switchExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
            switchExpression.accept(expressionVisitor);
            caseExpression.setSwitchExpression(expressionVisitor.getExpression());
        }

        //处理when的条件
        if (!CollectionUtils.isEmpty(caseExpression.getWhenClauses())) {
            List<WhenClause> whenClauses = caseExpression.getWhenClauses().stream()
                    .map(m -> {
                        WhereDencryptExpressionVisitor expressionVisitor = new WhereDencryptExpressionVisitor(m, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
                        m.accept(expressionVisitor);
                        // 这里返回的类型肯定是通过构造函数传输过去的，所以可以直接强转（这里过去是WhenClause WhenClause下一层才是Column才会转换类型）
                        return (WhenClause) expressionVisitor.getExpression();
                    }).collect(Collectors.toList());
            caseExpression.setWhenClauses(whenClauses);
        }

        //处理else
        Expression elseExpression = caseExpression.getElseExpression();
        if (elseExpression != null) {
            WhereDencryptExpressionVisitor expressionVisitor = new WhereDencryptExpressionVisitor(elseExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
            elseExpression.accept(expressionVisitor);
            caseExpression.setElseExpression(expressionVisitor.getExpression());
        }
    }

    @Override
    public void visit(WhenClause whenClause) {
        Expression thenExpression = whenClause.getThenExpression();
        if (thenExpression != null) {
            WhereDencryptExpressionVisitor expressionVisitor = new WhereDencryptExpressionVisitor(thenExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
            thenExpression.accept(expressionVisitor);
            whenClause.setThenExpression(expressionVisitor.getExpression());
        }

        Expression whenExpression = whenClause.getWhenExpression();
        if (whenExpression != null) {
            WhereDencryptExpressionVisitor expressionVisitor = new WhereDencryptExpressionVisitor(whenExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
            whenExpression.accept(expressionVisitor);
            whenClause.setWhenExpression(expressionVisitor.getExpression());
            whenExpression.accept(expressionVisitor);
        }
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
        WhereDencryptExpressionVisitor rightWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightWhereDencryptExpressionVisitor);
        existsExpression.setRightExpression(rightWhereDencryptExpressionVisitor.getExpression());
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
        //解析左右表达式
        Expression leftExpression = regExpMySQLOperator.getLeftExpression();
        WhereDencryptExpressionVisitor leftWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(leftExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        leftExpression.accept(leftWhereDencryptExpressionVisitor);
        regExpMySQLOperator.setLeftExpression(leftWhereDencryptExpressionVisitor.getExpression());

        Expression rightExpression = regExpMySQLOperator.getRightExpression();
        WhereDencryptExpressionVisitor rightWhereDencryptExpressionVisitor = new WhereDencryptExpressionVisitor(rightExpression, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        rightExpression.accept(rightWhereDencryptExpressionVisitor);
        regExpMySQLOperator.setRightExpression(rightWhereDencryptExpressionVisitor.getExpression());
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

    /**
     * 多字段 in 的时候，左边的多字段会走这里
     * where (xxx,yyy) in ((?,?),(?,?))
     *
     * @author liutangqi
     * @date 2024/8/28 13:50
     * @Param [rowConstructor]
     **/
    @Override
    public void visit(RowConstructor rowConstructor) {
        ExpressionList exprList = rowConstructor.getExprList();
        List<Expression> resExp = new ArrayList<>();

        //依次处理每个表达式
        List<Expression> expressions = exprList.getExpressions();
        for (Expression exp : expressions) {
            WhereDencryptExpressionVisitor wDEVisitor = new WhereDencryptExpressionVisitor(exp, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
            exp.accept(wDEVisitor);
            resExp.add(wDEVisitor.getExpression());
        }

        //处理后的表达式赋值
        exprList.setExpressions(resExp);
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
