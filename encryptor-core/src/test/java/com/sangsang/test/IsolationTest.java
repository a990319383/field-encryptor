package com.sangsang.test;

import com.sangsang.util.JsqlparserUtil;
import com.sangsang.visitor.isolation.IsolationStatementVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Test;


/**
 * @author liutangqi
 * @date 2025/5/27 10:46
 */
public class IsolationTest {
    //普通查询，没有条件
    String s1 = "select * from il_user tu";

    //带条件的查询，且条件里面有or
    String s2 = "select \n" +
            "*\n" +
            "from il_user \n" +
            "WHERE phone like 'xxx'\n" +
            "and user_name = 'xxx'\n" +
            "or phone = 'xxx'";
    //多层嵌套查询
    String s3 = "select a.*,\n" +
            "(select tu.phone from il_user tu) as ppp\n" +
            "from \n" +
            "(\n" +
            "select *\n" +
            "from il_user \n" +
            "WHERE phone = 'xxx')a";
    //联表查询 + EXISTS
    String s4 = "SELECT *\n" +
            "\t\tfrom il_user t1 \n" +
            "\t\tleft join il_user t2\n" +
            "\t\ton t1.id = t2.id \n" +
            "\t\tWHERE EXISTS (SELECT count(1) from il_user )";

    // in select
    String s5 = "\tSELECT *\n" +
            "\tfrom il_user tu \n" +
            "\tWHERE tu.id in (select id from il_user)";

    /**
     * mysql转换为达梦的语法转换器测试
     *
     * @author liutangqi
     * @date 2025/5/22 11:01
     * @Param []
     **/
    @Test
    public void isolationTest() throws JSQLParserException, NoSuchFieldException {
        //需要的sql
        String sql = s5;
        System.out.println("----------------------原始sql-----------------------");
        System.out.println(sql);
        //mock数据
        InitTableInfo.initTable();
        InitTableInfo.initIsolation();

        //开始进行语法转换
        Statement statement = JsqlparserUtil.parse(sql);
        IsolationStatementVisitor ilStatementVisitor = new IsolationStatementVisitor();
        statement.accept(ilStatementVisitor);


        System.out.println("----------------------数据隔离后sql-----------------------");
        System.out.println(ilStatementVisitor.getResultSql());
        System.out.println("---------------------------------------------");
    }

    @Test
    public void otherTest() {
    }


//----------------------------------------校验当前程序是否正确分割线---------------------------------------------------------
/*
    //需要测试的sql
    List<String> sqls = Arrays.asList(
    );


    *//**
     * 校验语法转换处理是否正确
     * 哥们儿，来对答案了
     *
     * @author liutangqi
     * @date 2025/6/6 15:40
     * @Param []
     **//*
    @Test
    public void tfCheck() throws NoSuchFieldException, JSQLParserException, IllegalAccessException {
        //mock数据
        InitTableInfo.initTable();

        //初始化转换器实例缓存
        FieldProperties fieldProperties = new FieldProperties();
        TransformationProperties transformationProperties = new TransformationProperties();
        transformationProperties.setPatternType(TransformationPatternTypeConstant.MYSQL_2_DM);
        fieldProperties.setTransformation(transformationProperties);
        new TransformationInstanceCache().init(fieldProperties);

        for (int i = 0; i < sqls.size(); i++) {
            String sql = sqls.get(i);
            //开始解析sql
            Statement statement = JsqlparserUtil.parse(sql);
            TransformationStatementVisitor transformationStatementVisitor = new TransformationStatementVisitor();
            statement.accept(transformationStatementVisitor);
            String resultSql = transformationStatementVisitor.getResultSql();

            //找答案
            String answer = AnswerUtil.readTfAnswerToFile(this, sql);
            String sqlFieldName = ReflectUtils.getFieldNameByValue(this, sql);
            if (StringUtils.isBlank(answer)) {
                System.out.println("这个sql没答案，自己检查，然后把正确答案给录到com.sangsang.answer.standard下面 :" + sqlFieldName);
                System.out.println("原始sql: " + sql);
                return;
            }
            if (answer.equalsIgnoreCase(resultSql)) {
                System.out.println("成功: " + sqlFieldName);
            } else {
                System.out.println("错误: " + sqlFieldName);
                System.out.println("原始sql: " + sql);
                System.out.println("-------------------------------------------------------");
                System.out.println("正确答案： " + answer);
                System.out.println("-------------------------------------------------------");
                System.out.println("当前答案： " + resultSql);
                return;
            }
        }
    }


//----------------------------------------写入处理好的答案分割线---------------------------------------------------------
//-----------------标准答案存储路径：com.sangsang.answer.standard
//-----------------此处答案输出路径：com.sangsang.answer.current

    *//**
     * 将转换好的结果答案写入到文件中
     *
     * @author liutangqi
     * @date 2025/6/6 15:31
     * @Param []
     **//*
    @Test
    public void transformationAnswerWrite() throws Exception {
        //mock数据
        InitTableInfo.initTable();

        //初始化转换器实例缓存
        FieldProperties fieldProperties = new FieldProperties();
        TransformationProperties transformationProperties = new TransformationProperties();
        transformationProperties.setPatternType(TransformationPatternTypeConstant.MYSQL_2_DM);
        fieldProperties.setTransformation(transformationProperties);
        new TransformationInstanceCache().init(fieldProperties);

        for (String sql : sqls) {
            //开始解析sql
            //开始进行语法转换
            Statement statement = JsqlparserUtil.parse(sql);
            TransformationStatementVisitor transformationStatementVisitor = new TransformationStatementVisitor();
            statement.accept(transformationStatementVisitor);
            String resultSql = transformationStatementVisitor.getResultSql();
            AnswerUtil.writeTfAnswerToFile(this, sql, resultSql);
        }
    }*/
}
