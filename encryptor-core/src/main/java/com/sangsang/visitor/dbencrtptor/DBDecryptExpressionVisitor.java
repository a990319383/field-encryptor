package com.sangsang.visitor.dbencrtptor;

import com.sangsang.cache.FieldEncryptorPatternCache;
import com.sangsang.domain.constants.NumberConstant;
import com.sangsang.domain.dto.BaseDEcryptParseTable;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.domain.enums.EncryptorEnum;
import com.sangsang.domain.enums.EncryptorFunctionEnum;
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
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.Select;

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
        List<Expression> expressions = Optional.ofNullable((ExpressionList<Expression>) function.getParameters())
                .orElse(new ExpressionList<>())
                .stream()
                .map(m -> {
                    DBDecryptExpressionVisitor sDecryptExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
                    m.accept(sDecryptExpressionVisitor);
                    return Optional.ofNullable(sDecryptExpressionVisitor.getExpression()).orElse(m);
                }).collect(Collectors.toList());
        if (function.getParameters() != null) {
            function.setParameters(new ExpressionList(expressions));
        }

    }

    @Override
    public void visit(SignedExpression signedExpression) {

    }

    /**
     * update tb set xxx = ? 这种语法的?
     *
     * @author liutangqi
     * @date 2025/3/13 11:08
     * @Param [jdbcParameter]
     **/
    @Override
    public void visit(JdbcParameter jdbcParameter) {
        //注意：这种是常量，肯定不是密文，所以下面写死的false
        Expression newExpression = this.getEncryptorFunctionEnum()
                .getFun()
                .dispose(false)
                .getdEncryptorFunction()
                .dEcryp(jdbcParameter);
        //处理结果赋值
        this.expression = newExpression;
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

    @Override
    public void visit(OverlapsCondition overlapsCondition) {

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
     * in的处理比较复杂，大部分情况下，根据左边不同的类型处理右边的列，可以有效解决索引失效的问题
     * 语法1： xxx in (?,?)
     * 语法2： xxx in (select xxx from )
     * 语法3： ? in (select xxx from)
     * 语法4： (xxx,yyy) in ((?,?),(?,?))
     * 语法5： (xxx,yyy) in (select xxx,yyy from )
     * 语法6： concat("aaa",tu.phone) in (? , ?)
     * 语法7： (?,?) in (select xxx,yyy from )
     *
     * @author liutangqi
     * @date 2025/3/1 13:54
     * @Param [inExpression]
     **/
    @Override
    public void visit(InExpression inExpression) {
        //1.处理左边表达式（一般只需要记录左边需要密文存储的索引(1.1,1.2)，只有特殊情况下才需要对左边进行加解密处理(1.3)）
        Expression leftExpression = inExpression.getLeftExpression();
        //记录左边字段需要密文存储的列下标
        List<Integer> needEncryptIndex = new ArrayList<>();
        //1.1 左边是单列的常量或者是字段列时（对应语法1，语法2，语法3）
        if ((leftExpression instanceof Column) || (inExpression.getLeftExpression() instanceof JdbcParameter)) {
            //获取左边表达式是否是 Column 并且需要进行密文存储
            boolean columnNeedEncrypt = (inExpression.getLeftExpression() instanceof Column)
                    && JsqlparserUtil.needEncrypt((Column) inExpression.getLeftExpression(), this.getLayer(), this.getLayerFieldTableMap());
            //这种情况左边只有一列，如果需要密文存储的话，下标肯定只有一个，是0
            if (columnNeedEncrypt) {
                needEncryptIndex.add(NumberConstant.ZERO);
            }
        }
        //1.2 左边是多值字段时（对应语法4，语法5，语法7）
        else if (leftExpression instanceof ParenthesedExpressionList) {
            ParenthesedExpressionList leftExpressionList = (ParenthesedExpressionList) inExpression.getLeftExpression();
            for (int i = 0; i < leftExpressionList.size(); i++) {
                //当前列是 Column类型，并且需要密文存储，则记录当前索引
                if ((leftExpressionList.get(i) instanceof Column)
                        && (JsqlparserUtil.needEncrypt((Column) leftExpressionList.get(i), this.getLayer(), this.getLayerFieldTableMap()))) {
                    needEncryptIndex.add(i);
                }
            }
        }
        //1.3 左边是其它情况时（对应语法6）
        else {
            //这种情况下，不维护左边的密文存储的下标，左右单独处理
            DBDecryptExpressionVisitor leftExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
            leftExpression.accept(leftExpressionVisitor);
            inExpression.setLeftExpression(Optional.ofNullable(leftExpressionVisitor.getExpression()).orElse(leftExpression));
        }

        //2.处理右边表达式
        Expression rightExpression = inExpression.getRightExpression();
        //2.1 当右边是多列，并且右边也是多列的集合时（对应语法4）
        if ((rightExpression instanceof ParenthesedExpressionList) && (((ParenthesedExpressionList) rightExpression).get(0)) instanceof ExpressionList) {
            ParenthesedExpressionList<ExpressionList> rightExpressionList = (ParenthesedExpressionList<ExpressionList>) rightExpression;
            for (ExpressionList expList : rightExpressionList) {
                for (int i = 0; i < expList.size(); i++) {
                    //根据左边是否明密文的情况处理右边
                    EncryptorFunctionEnum encryptorFunctionEnum = needEncryptIndex.contains(i) ? EncryptorFunctionEnum.UPSTREAM_SECRET : EncryptorFunctionEnum.UPSTREAM_PLAINTEXT;
                    DBDecryptExpressionVisitor sDecryptExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this, encryptorFunctionEnum);
                    ((Expression) expList.get(i)).accept(sDecryptExpressionVisitor);
                    //处理结果赋值
                    expList.set(i, Optional.ofNullable(sDecryptExpressionVisitor.getExpression()).orElse((Expression) expList.get(i)));
                }
            }
        }
        //2.2 当右边是多列的其它情况（对应语法1,语法6）注意：此时左边肯定只有1列，所以左边如果存在密文存储的字段，则右边全部都需要处理
        else if ((rightExpression instanceof ParenthesedExpressionList)) {
            ParenthesedExpressionList<Expression> rightExpressionList = (ParenthesedExpressionList<Expression>) rightExpression;
            for (int i = 0; i < rightExpressionList.size(); i++) {
                //根据左边是否明密文的情况处理右边
                EncryptorFunctionEnum encryptorFunctionEnum = CollectionUtils.isNotEmpty(needEncryptIndex) ? EncryptorFunctionEnum.UPSTREAM_SECRET : EncryptorFunctionEnum.UPSTREAM_PLAINTEXT;
                DBDecryptExpressionVisitor sDecryptExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this, encryptorFunctionEnum);
                rightExpressionList.get(i).accept(sDecryptExpressionVisitor);
                //处理结果赋值
                rightExpressionList.set(i, Optional.ofNullable(sDecryptExpressionVisitor.getExpression()).orElse(rightExpressionList.get(i)));
            }
        }
        //2.3 当右边是子查询时 （对应语法2，语法3，语法5，语法7）
        else if (rightExpression instanceof ParenthesedSelect) {
            ParenthesedSelect rightSelect = (ParenthesedSelect) rightExpression;
            //这种情况右边是一个完全独立的sql，单独解析
            FieldParseParseTableSelectVisitor fPTableSelectVisitor = FieldParseParseTableSelectVisitor.newInstanceFirstLayer();
            rightSelect.accept(fPTableSelectVisitor);
            //对右边的sql进行加解密处理
            DBDecryptSelectVisitor dbDecryptSelectVisitor = DBDecryptSelectVisitor.newInstanceCurLayer(fPTableSelectVisitor, needEncryptIndex);
            rightSelect.accept(dbDecryptSelectVisitor);
        }
        //2.4 其它情况没单独解析右边
        else {
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

    @Override
    public void visit(DoubleAnd doubleAnd) {

    }

    @Override
    public void visit(Contains contains) {

    }

    @Override
    public void visit(ContainedBy containedBy) {

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
     * 场景1：
     * select
     * (select 字段 from xx )
     * from
     * 这种语法
     * 场景2：
     * xxx in (select xxx from tb)
     *
     * @author liutangqi
     * @date 2024/3/6 17:19
     * @Param [subSelect]
     **/
    @Override
    public void visit(Select subSelect) {
        //这种语法的里面都是单独的语句，所以这里将里层的语句单独解析一次
        //1.采用独立存储空间单独解析当前子查询的语法
        FieldParseParseTableSelectVisitor sFieldSelectItemVisitor = FieldParseParseTableSelectVisitor.newInstanceIndividualMap(this);
        subSelect.accept(sFieldSelectItemVisitor);

        //2.上游如果是密文存储，则将上游的密文存储的下标传递给下游
        List<Integer> upstreamNeedEncryptIndex = new ArrayList<>();
        if (EncryptorFunctionEnum.UPSTREAM_SECRET.equals(this.getEncryptorFunctionEnum())) {
            upstreamNeedEncryptIndex.add(NumberConstant.ZERO);
        }

        //3.利用解析后的表结构Map进行子查询解密处理
        DBDecryptSelectVisitor sDecryptSelectVisitor = DBDecryptSelectVisitor.newInstanceCurLayer(sFieldSelectItemVisitor, upstreamNeedEncryptIndex);
        subSelect.accept(sDecryptSelectVisitor);
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
    public void visit(MemberOfExpression memberOfExpression) {

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
        ExpressionList<?> expressionList = groupConcat.getExpressionList();
        List<Expression> newExpressions = new ArrayList<>();
        for (Expression exp : expressionList) {
            DBDecryptExpressionVisitor sDecryptExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
            exp.accept(sDecryptExpressionVisitor);
            newExpressions.add(Optional.ofNullable(sDecryptExpressionVisitor.getExpression()).orElse(exp));
        }

        //替换解密后的表达式
        groupConcat.setExpressionList(new ExpressionList(newExpressions));
    }

    @Override
    public void visit(ExpressionList expressionList) {
        for (int i = 0; i < expressionList.size(); i++) {
            Expression expression = (Expression) expressionList.get(i);
            DBDecryptExpressionVisitor dbDecryptExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
            expression.accept(dbDecryptExpressionVisitor);
            expressionList.set(i, Optional.ofNullable(dbDecryptExpressionVisitor.getExpression()).orElse(expression));
        }
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
        List<Expression> resExp = new ArrayList<>();

        //依次处理每个表达式
        List<Expression> expressions = (List<Expression>) rowConstructor;
        for (Expression exp : expressions) {
            DBDecryptExpressionVisitor expressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
            exp.accept(expressionVisitor);
            resExp.add(Optional.ofNullable(expressionVisitor.getExpression()).orElse(exp));
        }

        //处理后的表达式赋值
        rowConstructor.setExpressions(resExp);
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

    @Override
    public void visit(ParenthesedSelect parenthesedSelect) {
        System.out.println("可疑语法出现，请注意" + parenthesedSelect.toString());
    }

    /**
     * convert函数
     *
     * @author liutangqi
     * @date 2025/3/17 16:51
     * @Param [transcodingFunction]
     **/
    @Override
    public void visit(TranscodingFunction transcodingFunction) {
        //处理表达式
        Expression expression = transcodingFunction.getExpression();
        DBDecryptExpressionVisitor dbDecryptExpressionVisitor = DBDecryptExpressionVisitor.newInstanceCurLayer(this);
        expression.accept(dbDecryptExpressionVisitor);

        //处理后的表达式赋值
        transcodingFunction.setExpression(Optional.ofNullable(dbDecryptExpressionVisitor.getExpression()).orElse(expression));
    }

    @Override
    public void visit(TrimFunction trimFunction) {

    }

    @Override
    public void visit(RangeExpression rangeExpression) {

    }

    @Override
    public void visit(TSQLLeftJoin tsqlLeftJoin) {

    }

    @Override
    public void visit(TSQLRightJoin tsqlRightJoin) {

    }
}
