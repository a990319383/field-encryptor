package com.sangsang.visitor.dbencrtptor;

import cn.hutool.core.util.ObjectUtil;
import com.sangsang.cache.FieldEncryptorPatternCache;
import com.sangsang.domain.constants.NumberConstant;
import com.sangsang.domain.dto.BaseDEcryptParseTable;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.domain.enums.EncryptorEnum;
import com.sangsang.domain.enums.EncryptorFunctionEnum;
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
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 将每一项字段  如果需要加密的，则进行加密
 * 备注：目前主要对Column 类型进行了处理
 *
 * @author liutangqi
 * @date 2024/2/29 16:50
 */
public class DBDecryptExpressionVisitor extends BaseDEcryptParseTable implements ExpressionVisitor {
    /**
     * 列经过处理后的别名
     * 当字段经过加解密函数处理后，这个值就会被赋值
     */
    private Alias alias;

    /**
     * 加解密处理好后的表达式
     * 当字段经过加解密函数处理后，这个值就会被赋值
     */
    private Expression expression;

    /**
     * 获取当前层的解析对象
     * （一般是上层节点调用）
     *
     * @author liutangqi
     * @date 2025/2/28 23:09
     * @Param [baseFieldParseTable, alias, expression]
     **/
    public static DBDecryptExpressionVisitor newInstanceCurLayer(BaseFieldParseTable baseFieldParseTable,
                                                                 EncryptorFunctionEnum encryptorFunctionEnum) {
        return new DBDecryptExpressionVisitor(baseFieldParseTable.getLayer(),
                encryptorFunctionEnum,
                baseFieldParseTable.getLayerSelectTableFieldMap(),
                baseFieldParseTable.getLayerFieldTableMap());
    }

    /**
     * 获取当前层的解析对象
     * （一般是当前节点调用，当前节点调用请不用直接accept(this),每个节点处理完毕的alias 和expression 需要单独保存，复用会导致错乱）
     *
     * @author liutangqi
     * @date 2025/2/28 23:09
     * @Param [baseFieldParseTable, expression]
     **/
    public static DBDecryptExpressionVisitor newInstanceCurLayer(BaseDEcryptParseTable baseDEcryptParseTable) {
        return new DBDecryptExpressionVisitor(
                baseDEcryptParseTable.getLayer(),
                baseDEcryptParseTable.getEncryptorFunctionEnum(),
                baseDEcryptParseTable.getLayerSelectTableFieldMap(),
                baseDEcryptParseTable.getLayerFieldTableMap());
    }

    private DBDecryptExpressionVisitor(int layer,
                                       EncryptorFunctionEnum encryptorFunctionEnum,
                                       Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap,
                                       Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, encryptorFunctionEnum, layerSelectTableFieldMap, layerFieldTableMap);
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
                    DBDecryptExpressionVisitor sDecryptExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
                    m.accept(sDecryptExpressionVisitor);
                    return Optional.ofNullable(sDecryptExpressionVisitor.getExpression()).orElse(m);
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

    /**
     * 括号括起来的一堆条件
     *
     * @author liutangqi
     * @date 2025/3/1 13:41
     * @Param [parenthesis]
     **/
    @Override
    public void visit(Parenthesis parenthesis) {
        //解析括号括起来的表达式
        Expression exp = parenthesis.getExpression();
        DBDecryptExpressionVisitor sDecryptExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        exp.accept(sDecryptExpressionVisitor);
        parenthesis.setExpression(Optional.ofNullable(sDecryptExpressionVisitor.getExpression()).orElse(exp));
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
        Expression leftExpression = andExpression.getLeftExpression();
        DBDecryptExpressionVisitor leftExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        leftExpression.accept(leftExpressionVisitor);
        andExpression.setLeftExpression(Optional.ofNullable(leftExpressionVisitor.getExpression()).orElse(leftExpression));

        Expression rightExpression = andExpression.getRightExpression();
        DBDecryptExpressionVisitor rightExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        rightExpression.accept(rightExpressionVisitor);
        andExpression.setRightExpression(Optional.ofNullable(rightExpressionVisitor.getExpression()).orElse(rightExpression));
    }

    @Override
    public void visit(OrExpression orExpression) {
        //解析左右表达式
        Expression leftExpression = orExpression.getLeftExpression();
        DBDecryptExpressionVisitor leftExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        leftExpression.accept(leftExpressionVisitor);
        orExpression.setLeftExpression(Optional.ofNullable(leftExpressionVisitor.getExpression()).orElse(leftExpression));

        Expression rightExpression = orExpression.getRightExpression();
        DBDecryptExpressionVisitor rightExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        rightExpression.accept(rightExpressionVisitor);
        orExpression.setRightExpression(Optional.ofNullable(rightExpressionVisitor.getExpression()).orElse(rightExpression));
    }

    @Override
    public void visit(XorExpression xorExpression) {

    }

    @Override
    public void visit(Between between) {

    }

    /**
     * select 语句中存在 case when 字段 = xxx then 这种语法的时候， 其中字段=xxx 会走这里的解析
     * where 语句中的 = 也会走这里解析
     *
     * @author liutangqi
     * @date 2024/7/30 16:49
     * @Param [equalsTo]
     **/
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
        DBDecryptExpressionVisitor leftExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        leftExpression.accept(leftExpressionVisitor);
        equalsTo.setLeftExpression(Optional.ofNullable(leftExpressionVisitor.getExpression()).orElse(leftExpression));

        Expression rightExpression = equalsTo.getRightExpression();
        DBDecryptExpressionVisitor rightExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        rightExpression.accept(rightExpressionVisitor);
        equalsTo.setRightExpression(Optional.ofNullable(rightExpressionVisitor.getExpression()).orElse(rightExpression));
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        //解析左右表达式
        Expression leftExpression = greaterThan.getLeftExpression();
        DBDecryptExpressionVisitor leftExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        leftExpression.accept(leftExpressionVisitor);
        greaterThan.setLeftExpression(Optional.ofNullable(leftExpressionVisitor.getExpression()).orElse(leftExpression));

        Expression rightExpression = greaterThan.getRightExpression();
        DBDecryptExpressionVisitor rightExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        rightExpression.accept(rightExpressionVisitor);
        greaterThan.setRightExpression(Optional.ofNullable(rightExpressionVisitor.getExpression()).orElse(rightExpression));
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        //解析左右表达式
        Expression leftExpression = greaterThanEquals.getLeftExpression();
        DBDecryptExpressionVisitor leftExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        leftExpression.accept(leftExpressionVisitor);
        greaterThanEquals.setLeftExpression(Optional.ofNullable(leftExpressionVisitor.getExpression()).orElse(leftExpression));

        Expression rightExpression = greaterThanEquals.getRightExpression();
        DBDecryptExpressionVisitor rightExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        rightExpression.accept(rightExpressionVisitor);
        greaterThanEquals.setRightExpression(Optional.ofNullable(rightExpressionVisitor.getExpression()).orElse(rightExpression));
    }

    /**
     * 当左边是 Column 时，针对一些场景进行优化，避免无意义的多次加解密
     *
     * @author liutangqi
     * @date 2025/3/1 13:54
     * @Param [inExpression]
     **/
    @Override
    public void visit(InExpression inExpression) {
        //1.当前左边表达式是Column时，针对下面两种情况做出优化，避免多次无意义的加解密
        if (inExpression.getLeftExpression() instanceof Column) {
            //判断左边 Column 是否需要密文存储
            boolean columnNeedEncrypt = JsqlparserUtil.needEncrypt((Column) inExpression.getLeftExpression(), this.getLayer(), this.getLayerFieldTableMap());
            //1.1 左边的Column是否密文存储影响后面子查询的加解密函数调用，如果需要密文存储的话，将自己的下标传给下游，因为只有一个，所以下标是0
            List<Integer> upstreamNeedEncryptIndex = new ArrayList<>();
            if (columnNeedEncrypt) {
                upstreamNeedEncryptIndex.add(NumberConstant.ZERO);
            }

            //1.2 右边是 (?,?,?)  Column in (?,?,?) 这种 : 只对右边进行加密处理
            if (inExpression.getRightItemsList() != null) {
                DBDecryptItemsListVisitor dBDecryptItemsListVisitor = DBDecryptItemsListVisitor.newInstanceCurLayer(columnNeedEncrypt);
                inExpression.getRightItemsList().accept(dBDecryptItemsListVisitor);
                return;
            }

            //1.3 右边是子查询  Column in (select xxx from xxx) 并且子查询select 全部需要加解密 && 左边的 Column也需要加解密
            //这种情况只用处理子查询的where条件
            Expression rightExpression = inExpression.getRightExpression();
            if (rightExpression != null && (rightExpression instanceof SubSelect)) {
                //1.3.1 拿出右边子查询的sql
                SelectBody selectBody = ((SubSelect) inExpression.getRightExpression()).getSelectBody();
                //1.3.2 因为这个sql是一个完全独立的sql，所以单独解析这个sql拥有的字段信息
                FieldParseParseTableSelectVisitor fieldParseParseTableSelectVisitor = FieldParseParseTableSelectVisitor.newInstanceFirstLayer();
                ;
                selectBody.accept(fieldParseParseTableSelectVisitor);

                DBDecryptSelectVisitor sDecryptSelectVisitor = DBDecryptSelectVisitor.newInstanceCurLayer(fieldParseParseTableSelectVisitor, upstreamNeedEncryptIndex);
                selectBody.accept(sDecryptSelectVisitor);
                return;
            }
        }

        //2.其它情况，左边的表达式和右边的子查询都需要单独处理，但是右边的 Column in (?,?,?) 这种不需要处理
        //2.1解析左边表达式
        Expression leftExpression = inExpression.getLeftExpression();
        DBDecryptExpressionVisitor leftExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        leftExpression.accept(leftExpressionVisitor);
        inExpression.setLeftExpression(Optional.ofNullable(leftExpressionVisitor.getExpression()).orElse(leftExpression));
        //2.2解析右边表达式(右边是子查询)
        Expression rightExpression = inExpression.getRightExpression();
        if (rightExpression != null && (rightExpression instanceof SubSelect)) {
            //备注：右边的子查询是一个完全独立的sql，所以不共用一个解析结果，需要单独解析当前sql中涉及的字段
            FieldParseParseTableSelectVisitor fieldParseParseTableSelectVisitor = FieldParseParseTableSelectVisitor.newInstanceFirstLayer();
            ((SubSelect) rightExpression).getSelectBody().accept(fieldParseParseTableSelectVisitor);
            //根据上面解析出来sql拥有的字段信息，将子查询进行加解密
            DBDecryptExpressionVisitor rightExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
            rightExpression.accept(rightExpressionVisitor);
            inExpression.setRightExpression(Optional.ofNullable(rightExpressionVisitor.getExpression()).orElse(rightExpression));
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
        //解析表达式
        Expression leftExpression = isBooleanExpression.getLeftExpression();
        DBDecryptExpressionVisitor leftExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        leftExpression.accept(leftExpressionVisitor);
        isBooleanExpression.setLeftExpression(Optional.ofNullable(leftExpressionVisitor.getExpression()).orElse(leftExpression));
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        //解析左右表达式
        Expression leftExpression = likeExpression.getLeftExpression();
        DBDecryptExpressionVisitor leftExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        leftExpression.accept(leftExpressionVisitor);
        likeExpression.setLeftExpression(Optional.ofNullable(leftExpressionVisitor.getExpression()).orElse(leftExpression));

        Expression rightExpression = likeExpression.getRightExpression();
        DBDecryptExpressionVisitor rightExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        rightExpression.accept(rightExpressionVisitor);
        likeExpression.setRightExpression(Optional.ofNullable(rightExpressionVisitor.getExpression()).orElse(rightExpression));
    }

    @Override
    public void visit(MinorThan minorThan) {
        //解析左右表达式
        Expression leftExpression = minorThan.getLeftExpression();
        DBDecryptExpressionVisitor leftExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        leftExpression.accept(leftExpressionVisitor);
        minorThan.setLeftExpression(Optional.ofNullable(leftExpressionVisitor.getExpression()).orElse(leftExpression));

        Expression rightExpression = minorThan.getRightExpression();
        DBDecryptExpressionVisitor rightExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        rightExpression.accept(rightExpressionVisitor);
        minorThan.setRightExpression(Optional.ofNullable(rightExpressionVisitor.getExpression()).orElse(rightExpression));
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        //解析左右表达式
        Expression leftExpression = minorThanEquals.getLeftExpression();
        DBDecryptExpressionVisitor leftExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        leftExpression.accept(leftExpressionVisitor);
        minorThanEquals.setLeftExpression(Optional.ofNullable(leftExpressionVisitor.getExpression()).orElse(leftExpression));

        Expression rightExpression = minorThanEquals.getRightExpression();
        DBDecryptExpressionVisitor rightExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        rightExpression.accept(rightExpressionVisitor);
        minorThanEquals.setRightExpression(Optional.ofNullable(rightExpressionVisitor.getExpression()).orElse(rightExpression));
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
        DBDecryptExpressionVisitor leftExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        leftExpression.accept(leftExpressionVisitor);
        notEqualsTo.setLeftExpression(Optional.ofNullable(leftExpressionVisitor.getExpression()).orElse(leftExpression));

        Expression rightExpression = notEqualsTo.getRightExpression();
        DBDecryptExpressionVisitor rightExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        rightExpression.accept(rightExpressionVisitor);
        notEqualsTo.setRightExpression(Optional.ofNullable(rightExpressionVisitor.getExpression()).orElse(rightExpression));


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
        boolean curNeedEncrypt = JsqlparserUtil.needEncrypt(column, this.getLayer(), this.getLayerFieldTableMap());

        //2.判断当前需要调用哪个方法
        EncryptorEnum encryptorEnum = this.getEncryptorFunctionEnum().getFun().dispose(curNeedEncrypt);

        //3.将该字段进行加/解密处理
        Expression decryptFunction = encryptorEnum.getdEncryptorFunction().dEcryp(column);
        this.expression = decryptFunction;

        //4.别名处理（字段经过加密函数后，如果之前没有别名的话，需要用之前的字段名作为别名，不然ORM映射的时候会无法匹配）
        if (!EncryptorEnum.WITHOUT.equals(encryptorEnum)) {
            this.alias = Optional.ofNullable(alias).orElse(new Alias(column.getColumnName()));
        }
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

        //1.采用独立存储空间单独解析当前子查询的语法
        FieldParseParseTableSelectVisitor sFieldSelectItemVisitor = FieldParseParseTableSelectVisitor.newInstanceIndividualMap(this);
        subSelect.getSelectBody().accept(sFieldSelectItemVisitor);

        //2.利用解析后的表结构Map进行子查询解密处理
        DBDecryptSelectVisitor sDecryptSelectVisitor = DBDecryptSelectVisitor.newInstanceCurLayer(sFieldSelectItemVisitor);
        subSelect.getSelectBody().accept(sDecryptSelectVisitor);
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
        //处理case的条件
        Expression switchExpression = caseExpression.getSwitchExpression();
        if (switchExpression != null) {
            DBDecryptExpressionVisitor expressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
            switchExpression.accept(expressionVisitor);
            caseExpression.setSwitchExpression(Optional.ofNullable(expressionVisitor.getExpression()).orElse(switchExpression));
        }

        //处理when的条件
        if (!CollectionUtils.isEmpty(caseExpression.getWhenClauses())) {
            List<WhenClause> whenClauses = caseExpression.getWhenClauses().stream()
                    .map(m -> {
                        DBDecryptExpressionVisitor expressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
                        m.accept(expressionVisitor);
                        // 这里返回的类型肯定是通过构造函数传输过去的，所以可以直接强转（这里过去是WhenClause WhenClause下一层才是Column才会转换类型）
                        return (WhenClause) (Optional.ofNullable(expressionVisitor.getExpression()).orElse(m));
                    }).collect(Collectors.toList());
            caseExpression.setWhenClauses(whenClauses);
        }

        //处理else
        Expression elseExpression = caseExpression.getElseExpression();
        if (elseExpression != null) {
            DBDecryptExpressionVisitor expressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
            elseExpression.accept(expressionVisitor);
            caseExpression.setElseExpression(Optional.ofNullable(expressionVisitor.getExpression()).orElse(elseExpression));
        }
    }

    @Override
    public void visit(WhenClause whenClause) {
        Expression thenExpression = whenClause.getThenExpression();
        if (thenExpression != null) {
            DBDecryptExpressionVisitor expressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
            thenExpression.accept(expressionVisitor);
            whenClause.setThenExpression(Optional.ofNullable(expressionVisitor.getExpression()).orElse(thenExpression));
        }

        Expression whenExpression = whenClause.getWhenExpression();
        if (whenExpression != null) {
            DBDecryptExpressionVisitor expressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
            whenExpression.accept(expressionVisitor);
            whenClause.setWhenExpression(Optional.ofNullable(expressionVisitor.getExpression()).orElse(whenExpression));
        }
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        //解析表达式
        Expression rightExpression = existsExpression.getRightExpression();
        DBDecryptExpressionVisitor rightExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        rightExpression.accept(rightExpressionVisitor);
        existsExpression.setRightExpression(Optional.ofNullable(rightExpressionVisitor.getExpression()).orElse(rightExpression));
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
        DBDecryptExpressionVisitor expressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        leftExpression.accept(expressionVisitor);
        cast.setLeftExpression(Optional.ofNullable(expressionVisitor.getExpression()).orElse(leftExpression));
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
        DBDecryptExpressionVisitor leftExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        leftExpression.accept(leftExpressionVisitor);
        regExpMySQLOperator.setLeftExpression(Optional.ofNullable(leftExpressionVisitor.getExpression()).orElse(leftExpression));

        Expression rightExpression = regExpMySQLOperator.getRightExpression();
        DBDecryptExpressionVisitor rightExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        rightExpression.accept(rightExpressionVisitor);
        regExpMySQLOperator.setRightExpression(Optional.ofNullable(rightExpressionVisitor.getExpression()).orElse(rightExpression));
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
            DBDecryptExpressionVisitor sDecryptExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
            exp.accept(sDecryptExpressionVisitor);
            newExpressions.add(Optional.ofNullable(sDecryptExpressionVisitor.getExpression()).orElse(exp));
        }

        //替换解密后的表达式
        expressionList.setExpressions(newExpressions);
    }

    @Override
    public void visit(ValueListExpression valueList) {

    }

    /**
     * 多字段 in 的时候，左边的多字段会走这里
     * where (xxx,yyy) in ((?,?),(?,?))
     *
     * @author liutangqi
     * @date 2025/3/1 14:11
     * @Param [rowConstructor]
     **/
    @Override
    public void visit(RowConstructor rowConstructor) {
        ExpressionList exprList = rowConstructor.getExprList();
        List<Expression> resExp = new ArrayList<>();

        //依次处理每个表达式
        List<Expression> expressions = exprList.getExpressions();
        for (Expression exp : expressions) {
            DBDecryptExpressionVisitor expressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
            exp.accept(expressionVisitor);
            resExp.add(Optional.ofNullable(expressionVisitor.getExpression()).orElse(exp));
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
