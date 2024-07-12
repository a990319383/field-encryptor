package com.sangsang.visitor.beanencrtptor;

import com.sangsang.cache.TableCache;
import com.sangsang.domain.constants.NumberConstant;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.FieldEncryptorInfoDto;
import com.sangsang.domain.dto.FieldInfoDto;
import com.sangsang.visitor.beanencrtptor.select.PlaceholderSelectVisitor;
import com.sangsang.visitor.encrtptor.fieldparse.FieldParseParseTableSelectVisitor;
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
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 通过对javabean进行加解密的sql解析入口
 * 通过这里的解析，主要目的
 * 1.获取到select的每一项对应数据库的表和字段
 * 2.将每个#{}占位符合数据库的表，字段对应上
 *
 * @author liutangqi
 * @date 2024/7/6 13:22
 */
public class BeanEncrtptorStatementVisitor implements StatementVisitor {

    /**
     * 当前sql涉及到的字段以及字段的所属表结构信息
     **/
    private List<FieldEncryptorInfoDto> fieldEncryptorInfos = new ArrayList<>();

    /**
     * 当前占位符对应的数据库表，字段信息
     * key: 占位符DecryptConstant.PLACEHOLDER + 0开始的自增序号  （这个在解析前，我们会将？的占位符统一替换成这个格式的占位符）
     * value: 这个字段所属的表字段
     */
    private Map<String, ColumnTableDto> placeholderColumnTableMap;

    public List<FieldEncryptorInfoDto> getFieldEncryptorInfos() {
        return fieldEncryptorInfos;
    }

    public Map<String, ColumnTableDto> getPlaceholderColumnTableMap() {
        return placeholderColumnTableMap;
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

    }

    @Override
    public void visit(Update update) {

    }

    @Override
    public void visit(Insert insert) {

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
    public void visit(Statements statements) {

    }

    @Override
    public void visit(Execute execute) {

    }

    @Override
    public void visit(SetStatement setStatement) {

    }

    @Override
    public void visit(ResetStatement resetStatement) {

    }

    @Override
    public void visit(ShowColumnsStatement showColumnsStatement) {

    }

    @Override
    public void visit(ShowTablesStatement showTablesStatement) {

    }

    @Override
    public void visit(Merge merge) {

    }

    //todo-ltq  uninon 语句 只解析第一个sql即可 （正确的union语句，一个字段，不同的union段肯定要么都加密，要么都不加密）
    @Override
    public void visit(Select select) {
        //1.解析select拥有的字段对应的表结构信息
        //1.1解析当前sql拥有的全部字段信息
        FieldParseParseTableSelectVisitor fieldParseTableSelectVisitor = new FieldParseParseTableSelectVisitor(NumberConstant.ONE, null, null);
        select.getSelectBody().accept(fieldParseTableSelectVisitor);

        //1.2.获取sql 查询的所有字段
        Map<String, Map<String, Set<FieldInfoDto>>> layerSelectTableFieldMap = fieldParseTableSelectVisitor.getLayerSelectTableFieldMap();
        List<FieldInfoDto> selectFiles = layerSelectTableFieldMap.get(String.valueOf(NumberConstant.ONE))
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        //1.3.将每个字段从实体类上找到标注的@FieldEncryptor 注解
        List<FieldEncryptorInfoDto> fieldInfos = selectFiles.stream()
                .map(m -> FieldEncryptorInfoDto.builder()
                        .columnName(m.getColumnName())
                        .sourceColumn(m.getSourceColumn())
                        .sourceTableName(m.getSourceTableName())
                        .fieldEncryptor(TableCache.getTableFieldEncryptInfo().getOrDefault(m.getSourceTableName(), new HashMap<>()).get(m.getSourceColumn()))
                        .build()
                ).collect(Collectors.toList());

        //1.4.结果集赋值
        this.fieldEncryptorInfos.addAll(fieldInfos);

        //2.将#{}占位符和数据库表结构字段对应起来
        //2.1开始解析
        PlaceholderSelectVisitor placeholderSelectVisitor = new PlaceholderSelectVisitor(fieldParseTableSelectVisitor, null);
        select.getSelectBody().accept(placeholderSelectVisitor);
        //2.2结果赋值
        this.placeholderColumnTableMap = placeholderSelectVisitor.getPlaceholderColumnTableMap();

    }

    @Override
    public void visit(Upsert upsert) {

    }

    @Override
    public void visit(UseStatement useStatement) {

    }

    @Override
    public void visit(Block block) {

    }

    @Override
    public void visit(ValuesStatement valuesStatement) {

    }

    @Override
    public void visit(DescribeStatement describeStatement) {

    }

    @Override
    public void visit(ExplainStatement explainStatement) {

    }

    @Override
    public void visit(ShowStatement showStatement) {

    }

    @Override
    public void visit(DeclareStatement declareStatement) {

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
