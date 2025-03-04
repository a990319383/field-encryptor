package com.sangsang.visitor.dbencrtptor;

import com.sangsang.domain.enums.EncryptorFunctionEnum;
import com.sangsang.domain.function.EncryptorFunctionScene;
import com.sangsang.util.CollectionUtils;
import com.sangsang.cache.FieldEncryptorPatternCache;
import com.sangsang.cache.TableCache;
import com.sangsang.domain.annos.FieldEncryptor;
import com.sangsang.domain.constants.NumberConstant;
import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.util.JsqlparserUtil;
import com.sangsang.visitor.dbencrtptor.select.SDecryptExpressionVisitor;
import com.sangsang.visitor.fieldparse.FieldParseParseTableFromItemVisitor;
import com.sangsang.visitor.fieldparse.FieldParseParseTableSelectVisitor;
import com.sangsang.visitor.dbencrtptor.insert.IDecryptItemsListVisitor;
import com.sangsang.visitor.dbencrtptor.select.SDecryptSelectVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 解析sql的入口 （crud所有类型的sql ）
 *
 * @author liutangqi
 * @date 2024/2/29 17:55
 */
public class DBDencryptStatementVisitor implements StatementVisitor {
    private static final Logger log = LoggerFactory.getLogger(DBDencryptStatementVisitor.class);

    /**
     * 加密完成后的sql
     */
    private String resultSql;

    public String getResultSql() {
        return resultSql;
    }

    @Override
    public void visit(SavepointStatement savepointStatement) {

    }

    @Override
    public void visit(RollbackStatement rollbackStatement) {

    }

    @Override
    public void visit(Comment comment) {

    }

    @Override
    public void visit(Commit commit) {

    }

    @Override
    public void visit(Delete delete) {
        //1.where 条件 不存在，则不进行加密处理（delete语句主要对delete的条件进行加密）
        Expression where = delete.getWhere();
        if (where == null) {
            return;
        }

        //2.解析涉及到的表拥有的全部字段信息
        FieldParseParseTableFromItemVisitor fieldParseTableFromItemVisitor = new FieldParseParseTableFromItemVisitor(NumberConstant.ONE, null, null);
        // from 后的表
        Table table = delete.getTable();
        table.accept(fieldParseTableFromItemVisitor);

        //join 的表
        List<Join> joins = Optional.ofNullable(delete.getJoins()).orElse(new ArrayList<>());
        for (Join join : joins) {
            FromItem rightItem = join.getRightItem();
            rightItem.accept(fieldParseTableFromItemVisitor);
        }

        //3.当前sql涉及到的表不需要加密的不做处理
        if (!JsqlparserUtil.needEncrypt(fieldParseTableFromItemVisitor.getLayerSelectTableFieldMap(), fieldParseTableFromItemVisitor.getLayerFieldTableMap())) {
            this.resultSql = delete.toString();
            return;
        }

        //4.将where 条件进行解密
        SDecryptExpressionVisitor sDecryptExpressionVisitor = SDecryptExpressionVisitor.newInstanceCurLayer(fieldParseTableFromItemVisitor, EncryptorFunctionEnum.DEFAULT_DECRYPTION);
        where.accept(sDecryptExpressionVisitor);

        //5.结果赋值
        delete.setWhere(Optional.ofNullable(sDecryptExpressionVisitor.getExpression()).orElse(where));
        this.resultSql = delete.toString();
    }

    @Override
    public void visit(Update update) {
        //1.解析涉及到的表拥有的全部字段信息
        FieldParseParseTableFromItemVisitor fieldParseTableFromItemVisitor = new FieldParseParseTableFromItemVisitor(NumberConstant.ONE, null, null);

        //update的表
        Table table = update.getTable();
        table.accept(fieldParseTableFromItemVisitor);

        //join的表
        List<Join> joins = Optional.ofNullable(update.getStartJoins()).orElse(new ArrayList<>());
        for (Join join : joins) {
            join.getRightItem().accept(fieldParseTableFromItemVisitor);
        }

        //2.当前sql涉及到的表不需要加密的不做处理
        if (!JsqlparserUtil.needEncrypt(fieldParseTableFromItemVisitor.getLayerSelectTableFieldMap(), fieldParseTableFromItemVisitor.getLayerFieldTableMap())) {
            this.resultSql = update.toString();
            return;
        }

        //3.解密where 条件的数据
        Expression where = update.getWhere();
        if (where != null) {
            SDecryptExpressionVisitor expressionVisitor = SDecryptExpressionVisitor.newInstanceCurLayer(fieldParseTableFromItemVisitor, EncryptorFunctionEnum.DEFAULT_DECRYPTION);
            where.accept(expressionVisitor);
            //修改后的where赋值
            update.setWhere(Optional.ofNullable(expressionVisitor.getExpression()).orElse(where));
        }

        //4.加密处理set的数据
        List<UpdateSet> updateSets = update.getUpdateSets();
        for (UpdateSet updateSet : updateSets) {
            List<Column> columns = updateSet.getColumns();
            List<Expression> expressions = updateSet.getExpressions();
            //处理每对需要加密的字段，只处理一边是数据库字段，一边是常量的，两边都是数据库字段的，根据情况处理
            for (int i = 0; i < columns.size(); i++) {
                Column column = columns.get(i);
                Expression expression = expressions.get(i);
                //左边是否需要加解密
                boolean leftNeedEncrypt = JsqlparserUtil.needEncrypt(column, fieldParseTableFromItemVisitor.getLayer(), fieldParseTableFromItemVisitor.getLayerFieldTableMap());

                //4.1 两边都是来自数据库
                if (expression instanceof Column) {
                    //右边是否需要加解密
                    boolean rightNeedEncrypt = JsqlparserUtil.needEncrypt((Column) expression, fieldParseTableFromItemVisitor.getLayer(), fieldParseTableFromItemVisitor.getLayerFieldTableMap());
                    //两边都需要加解密，不做处理
                    if (leftNeedEncrypt && rightNeedEncrypt) {
                        continue;
                    }

                    //左边需要加密，右边不需要，则将右边进行加密
                    if (leftNeedEncrypt && !rightNeedEncrypt) {
                        Expression encryptionExpression = FieldEncryptorPatternCache.getInstance().encryption(expression);
                        expressions.set(i, encryptionExpression);
                    }

                    //左边不需要加密，右边需要，则将右边进行解密
                    if (!leftNeedEncrypt && rightNeedEncrypt) {
                        Expression decryptionExpression = FieldEncryptorPatternCache.getInstance().decryption(expression);
                        expressions.set(i, decryptionExpression);
                    }
                }

                //4.2 左边是数据库字段，右边不是
                //左边的Column 字段不需要加密不做处理
                if (!JsqlparserUtil.needEncrypt(column, fieldParseTableFromItemVisitor.getLayer(), fieldParseTableFromItemVisitor.getLayerFieldTableMap())) {
                    continue;
                }
                //数据进行加密处理
                Expression encryptionExpression = FieldEncryptorPatternCache.getInstance().encryption(expression);
                //加密后赋值
                expressions.set(i, encryptionExpression);
            }
        }

        //5.处理结果赋值
        this.resultSql = update.toString();

    }

    @Override
    public void visit(Insert insert) {
        //insert 的表
        Table table = insert.getTable();
        //1.当前表不需要加密，直接返回，不处理
        if (!TableCache.getFieldEncryptTable().contains(table.getName().toLowerCase())) {
            this.resultSql = insert.toString();
            return;
        }

        Map<String, FieldEncryptor> fieldEncryptMap = TableCache.getTableFieldEncryptInfo().get(table.getName().toLowerCase());

        //2.获取当前第几个字段是需要加密的
        // 需要加密的字段的索引
        List<Integer> needEncryptIndex = new ArrayList<>();
        //insert 的字段名
        List<Column> columns = insert.getColumns();
        if (CollectionUtils.isEmpty(columns)) {
            log.warn("【field-encryptor】insert 语句未指定表字段顺序，不支持自动加解密，请规范语法 原sql:{}", insert.toString());
            return;
        }

        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            if (JsqlparserUtil.getValueIgnoreFloat(fieldEncryptMap, column.getColumnName().toLowerCase()) != null) {
                needEncryptIndex.add(i);
            }
        }

        //3.插入的每一列，进行加密处理
        //情况1：这里是 insert into table(xxx,xxx) values(),()  这种语法
        ItemsList itemsList = insert.getItemsList();
        if (itemsList != null) {
            IDecryptItemsListVisitor iDecryptItemsListVisitor = new IDecryptItemsListVisitor(needEncryptIndex);
            itemsList.accept(iDecryptItemsListVisitor);
        }

        //情况2：insert select 语句
        Select select = insert.getSelect();
        if (select != null) {
            //解析当前查询语句的每层表的全部字段
            FieldParseParseTableSelectVisitor fieldParseTableSelectVisitor = new FieldParseParseTableSelectVisitor(NumberConstant.ONE, null, null);
            select.getSelectBody().accept(fieldParseTableSelectVisitor);

            //将这个查询语句where 条件后面的进行加解密处理
            SDecryptSelectVisitor sDecryptSelectVisitor = SDecryptSelectVisitor.newInstanceCurLayer(fieldParseTableSelectVisitor, needEncryptIndex);
            select.getSelectBody().accept(sDecryptSelectVisitor);
        }

        //4.ON DUPLICATE KEY UPDATE 语法 此语法不用单独处理，即可兼容
//        List<Column> duplicateUpdateColumns = insert.getDuplicateUpdateColumns();
//        List<Expression> duplicateUpdateExpressionList = insert.getDuplicateUpdateExpressionList();

        //5.处理好的sql赋值
        this.resultSql = insert.toString();
    }

    @Override
    public void visit(Replace replace) {

    }

    @Override
    public void visit(Drop drop) {

    }

    @Override
    public void visit(Truncate truncate) {

    }

    @Override
    public void visit(CreateIndex createIndex) {

    }

    @Override
    public void visit(CreateSchema createSchema) {

    }

    @Override
    public void visit(CreateTable createTable) {

    }

    @Override
    public void visit(CreateView createView) {

    }

    @Override
    public void visit(AlterView alterView) {

    }

    @Override
    public void visit(Alter alter) {

    }

    @Override
    public void visit(Statements stmts) {

    }

    @Override
    public void visit(Execute execute) {

    }

    @Override
    public void visit(SetStatement set) {

    }

    @Override
    public void visit(ResetStatement resetStatement) {

    }


    @Override
    public void visit(ShowColumnsStatement set) {

    }

    @Override
    public void visit(ShowTablesStatement showTablesStatement) {

    }


    @Override
    public void visit(Merge merge) {

    }

    /**
     * 给select语句进行加密
     *
     * @author liutangqi
     * @date 2024/2/29 17:56
     * @Param [select]
     **/
    @Override
    public void visit(Select select) {
        //1.解析当前sql拥有的全部字段信息
        FieldParseParseTableSelectVisitor fieldParseTableSelectVisitor = new FieldParseParseTableSelectVisitor(NumberConstant.ONE, null, null);
        select.getSelectBody().accept(fieldParseTableSelectVisitor);

        //2.如果不是union语句  并且 该sql涉及的表都不需要加解密，则不处理后续逻辑 （union语句没有整个解析到这个结果集中，union语句是分成多次解析的）
        if (!select.toString().toLowerCase().contains(SymbolConstant.UNION) && !JsqlparserUtil.needEncrypt(fieldParseTableSelectVisitor.getLayerSelectTableFieldMap(), fieldParseTableSelectVisitor.getLayerFieldTableMap())) {
            this.resultSql = select.toString();
            return;
        }

        //3.将需要加密的字段进行加密处理
        SDecryptSelectVisitor sDecryptSelectVisitor = SDecryptSelectVisitor.newInstanceCurLayer(fieldParseTableSelectVisitor);
        select.getSelectBody().accept(sDecryptSelectVisitor);

        //4.处理后的结果赋值
        this.resultSql = sDecryptSelectVisitor.getResultSql();
    }

    @Override
    public void visit(Upsert upsert) {

    }

    @Override
    public void visit(UseStatement use) {

    }

    @Override
    public void visit(Block block) {

    }

    @Override
    public void visit(ValuesStatement values) {

    }

    @Override
    public void visit(DescribeStatement describe) {

    }

    @Override
    public void visit(ExplainStatement aThis) {

    }

    @Override
    public void visit(ShowStatement aThis) {

    }

    @Override
    public void visit(DeclareStatement aThis) {

    }

    @Override
    public void visit(Grant grant) {

    }

    @Override
    public void visit(CreateSequence createSequence) {

    }

    @Override
    public void visit(AlterSequence alterSequence) {

    }

    @Override
    public void visit(CreateFunctionalStatement createFunctionalStatement) {

    }

    @Override
    public void visit(CreateSynonym createSynonym) {

    }

    @Override
    public void visit(AlterSession alterSession) {

    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {

    }

    @Override
    public void visit(RenameTableStatement renameTableStatement) {

    }

    @Override
    public void visit(PurgeStatement purgeStatement) {

    }

    @Override
    public void visit(AlterSystemStatement alterSystemStatement) {

    }

}
