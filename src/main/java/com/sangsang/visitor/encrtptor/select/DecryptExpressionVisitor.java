package com.sangsang.visitor.encrtptor.select;

import com.sangsang.cache.TableCache;
import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.domain.dto.BaseFieldParseTable;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.util.JsqlparserUtil;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.*;

/**
 * 将select的每一项字段  如果需要加密的，则进行加密
 * 备注：目前主要对Column 类型进行了处理
 *
 * @author liutangqi
 * @date 2024/2/29 16:50
 */
public class DecryptExpressionVisitor extends BaseFieldParseTable implements ExpressionVisitor {
    /**
     * 当前查询项原来的别名
     */
    private Alias alias;

    /**
     * 当前字段是否加密后需要增加别名处理,默认是true
     * 一般每个字段处理后，要求结果集别名和之前一致，所以需要as
     * 但是case这种处理的时候不需要加
     */
    private boolean aliasAs = true;

    public DecryptExpressionVisitor(Alias alias, int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
        this.alias = alias;
    }

    public DecryptExpressionVisitor(boolean aliasAs, Alias alias, int layer, Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap, Map<String, Map<String, Set<FieldInfoDto>>> layerFieldTableMap) {
        super(layer, layerSelectTableFieldMap, layerFieldTableMap);
        this.alias = alias;
        this.aliasAs = aliasAs;
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
        //解析function ，function 里面的不需要处理别名
        DecryptExpressionVisitor decryptExpressionVisitor = new DecryptExpressionVisitor(false, null, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());

        List<Expression> expressions = Optional.ofNullable(function.getParameters())
                .map(ExpressionList::getExpressions)
                .orElse(new ArrayList<>());
        for (Expression expression : expressions) {
            expression.accept(decryptExpressionVisitor);
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

    /**
     * select的每一个查询项
     *
     * @author liutangqi
     * @date 2024/2/29 17:39
     * @Param [column]
     **/
    @Override
    public void visit(Column column) {
        //1.解析当前字段所属的表信息
        ColumnTableDto columnTableDto = JsqlparserUtil.parseColumn(column, this.getLayer(), this.getLayerFieldTableMap());


        //2.当前字段不需要解密直接返回 (实体类上面没有标注@FieldEncrypt注解 或者字段不是来源自真实表)
        // 注意：select只有最直接和真实表获取字段的那层才需要加解密
        if (!columnTableDto.isFromSourceTable() || Optional.ofNullable(TableCache.getTableFieldEncryptInfo())
                .map(m -> m.get(columnTableDto.getSourceTableName()))
                .map(m -> m.get(columnTableDto.getSourceColumn()))
                .orElse(null) == null) {
            return;
        }

        //3.将字段进行解密(注意：这里需要特殊处理别名，不然sql最后会变成函数处理后的字段，导致结果集映射失败)
        //别名:当旧别名不存在，并且需要设置别名时设置别名，旧别名存在时不需要单独设置别名，toString()的时候会设置别名的
        String columnAlias = alias == null && aliasAs ? SymbolConstant.AS + column.getColumnName() : "";
        String EncryptColumn = SymbolConstant.DECODE + columnTableDto.getTableAliasName() + SymbolConstant.FULL_STOP + column.getColumnName() + "), 'encryptionKey秘钥') " + columnAlias;
        column.setTable(null);
        column.setColumnName(EncryptColumn);

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
        //子查询进行解密处理
        DecryptSelectVisitor decryptSelectVisitor = new DecryptSelectVisitor(this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        subSelect.getSelectBody().accept(decryptSelectVisitor);
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        //case处理的时候，不加别名处理
        DecryptExpressionVisitor expressionVisitor = new DecryptExpressionVisitor(false, null, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());

        //处理case的条件
        Expression switchExpression = caseExpression.getSwitchExpression();
        if (switchExpression != null) {
            switchExpression.accept(expressionVisitor);
        }

        //处理when的条件
        List<WhenClause> whenClauses = Optional.ofNullable(caseExpression.getWhenClauses()).orElse(new ArrayList<>());
        for (WhenClause whenClause : whenClauses) {
            whenClause.accept(expressionVisitor);
        }

        //处理else
        Expression elseExpression = caseExpression.getElseExpression();
        if (elseExpression != null) {
            elseExpression.accept(expressionVisitor);
        }
    }

    @Override
    public void visit(WhenClause whenClause) {
        //case处理的时候，不加别名处理
        DecryptExpressionVisitor expressionVisitor = new DecryptExpressionVisitor(false, null, this.getLayer(), this.getLayerSelectTableFieldMap(), this.getLayerFieldTableMap());
        Expression thenExpression = whenClause.getThenExpression();
        if (thenExpression != null) {
            thenExpression.accept(expressionVisitor);
        }

        Expression whenExpression = whenClause.getWhenExpression();
        if (whenExpression != null) {
            whenExpression.accept(expressionVisitor);
        }
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        //select查询中exist 解密暂未考虑到场景，不做处理，目前只兼容了where条件后面的exist
        System.out.println(existsExpression);
//        Expression rightExpression = existsExpression.getRightExpression();

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
