package com.sangsang.visitor.encrtptor;

import com.sangsang.cache.TableCache;
import com.sangsang.domain.annos.FieldEncryptor;
import com.sangsang.domain.constants.NumberConstant;
import com.sangsang.util.JsqlparserUtil;
import com.sangsang.visitor.encrtptor.fieldparse.FieldParseParseTableFromItemVisitor;
import com.sangsang.visitor.encrtptor.fieldparse.FieldParseParseTableSelectVisitor;
import com.sangsang.visitor.encrtptor.insert.IDecryptItemsListVisitor;
import com.sangsang.visitor.encrtptor.insert.IDecryptSelectVisitor;
import com.sangsang.visitor.encrtptor.select.DecryptSelectVisitor;
import com.sangsang.visitor.encrtptor.update.UDecryptExpressionVisitor;
import com.sangsang.visitor.encrtptor.where.DencryptWhereFieldParseVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.*;

/**
 * 解析sql的入口 （crud所有类型的sql ）
 *
 * @author liutangqi
 * @date 2024/2/29 17:55
 */
public class DencryptStatementVisitor implements StatementVisitor {
    /**
     * 加密完成后的sql
     */
    private String resultSql;

    public String getResultSql() {
        return resultSql;
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

        //4.将where 条件进行加密
        DencryptWhereFieldParseVisitor dencryptWhereFieldVisitor = new DencryptWhereFieldParseVisitor(where, fieldParseTableFromItemVisitor.getLayer(), fieldParseTableFromItemVisitor.getLayerSelectTableFieldMap(), fieldParseTableFromItemVisitor.getLayerFieldTableMap());
        where.accept(dencryptWhereFieldVisitor);

        //5.结果赋值
        delete.setWhere(dencryptWhereFieldVisitor.getExpression());
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

        //3.加密where 条件的数据
        Expression where = update.getWhere();
        if (where != null) {
            DencryptWhereFieldParseVisitor dencryptWhereFieldVisitor = new DencryptWhereFieldParseVisitor(where, fieldParseTableFromItemVisitor.getLayer(), fieldParseTableFromItemVisitor.getLayerSelectTableFieldMap(), fieldParseTableFromItemVisitor.getLayerFieldTableMap());
            where.accept(dencryptWhereFieldVisitor);
            //修改后的where赋值
            update.setWhere(dencryptWhereFieldVisitor.getExpression());
        }

        //4.加密处理set的数据 （只加密 set 后面的表达式不是来自于其它表的，来自从其它表里面取的，默认是从密文到密文，不需要加密）
        //set 后面的值
        List<Expression> expressions = update.getExpressions();
        //set 前面所属的字段（这个字段不用处理，只用加密expressions即可）
        List<Column> columns = update.getColumns();
        List<Expression> encryptExpression = new ArrayList<>();
        for (int i = 0; i < expressions.size(); i++) {
            //和当前set值配对的字段
            Column column = columns.get(i);
            Expression expression = expressions.get(i);
            UDecryptExpressionVisitor uDecryptExpressionVisitor = new UDecryptExpressionVisitor(column, expression, fieldParseTableFromItemVisitor.getLayer(), fieldParseTableFromItemVisitor.getLayerSelectTableFieldMap(), fieldParseTableFromItemVisitor.getLayerFieldTableMap());
            expression.accept(uDecryptExpressionVisitor);
            //获取修改后的表达式
            encryptExpression.add(uDecryptExpressionVisitor.getExpression());
        }

        //5.处理结果赋值
        update.setExpressions(encryptExpression);
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
        List<String> needEncryptIndex = new ArrayList<>();
        //insert 的字段名
        List<Column> columns = insert.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            if (fieldEncryptMap.get(column.getColumnName().toLowerCase()) != null) {
                needEncryptIndex.add(String.valueOf(i));
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
            IDecryptSelectVisitor iDecryptSelectVisitor = new IDecryptSelectVisitor(fieldParseTableSelectVisitor.getLayer(), fieldParseTableSelectVisitor.getLayerSelectTableFieldMap(), fieldParseTableSelectVisitor.getLayerFieldTableMap());
            select.getSelectBody().accept(iDecryptSelectVisitor);
        }

        //4.处理好的sql赋值
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
    public void visit(ShowColumnsStatement set) {

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

        //2.如果该sql涉及的表都不需要加解密，则不处理后续逻辑
        if (!JsqlparserUtil.needEncrypt(fieldParseTableSelectVisitor.getLayerSelectTableFieldMap(), fieldParseTableSelectVisitor.getLayerFieldTableMap())) {
            this.resultSql = select.toString();
            return;
        }

        //3.将需要加密的字段进行加密处理
        DecryptSelectVisitor SDecryptSelectVisitor = new DecryptSelectVisitor(NumberConstant.ONE, fieldParseTableSelectVisitor.getLayerSelectTableFieldMap(), fieldParseTableSelectVisitor.getLayerFieldTableMap());
        select.getSelectBody().accept(SDecryptSelectVisitor);

        //4.处理后的结果赋值
        this.resultSql = SDecryptSelectVisitor.getResultSql();
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

}
