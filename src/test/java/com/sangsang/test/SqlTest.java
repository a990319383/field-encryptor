package com.sangsang.test;

import com.sangsang.cache.FieldEncryptorPatternCache;
import com.sangsang.visitor.encrtptor.DencryptStatementVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Test;

/**
 * @author liutangqi
 * @date 2024/4/2 15:22
 */
//@ContextConfiguration(classes = {TableCache.class})
public class SqlTest {
    //------------select 解密 测试语句--------------

    //嵌套查询 ，带* ，where 条件带() 带or
    String s1 = "select \n" +
            "\t\ttu.*,\n" +
            "\t\ttm.menu_name\n" +
            "from\n" +
            "\t\ttb_user tu\n" +
            "left join tb_menu tm \n" +
            "\t\ton\n" +
            "\t\ttu.id = tm.id\n" +
            "where\n" +
            "\ttu.phone like \"%xxx%\"\n" +
            " and tu.phone = 'yyyyy'" +
            "\tand tm.menu_name != null\n" +
            "\tand tm.parent_id in (1, 2, 3)\n" +
            "\tor tm.path is not null\n" +
            "\tor (tm.parent_id = 1\n" +
            "\t\tand tm.create_time is not null )";

    //多层嵌套 带*
    String s2 = "select \n" +
            "\t\t*\n" +
            "from\n" +
            "\t(\n" +
            "\tselect\n" +
            "\t\t*\n" +
            "\tfrom\n" +
            "\t\ttb_user \n" +
            "\t\t)a\n" +
            "where\n" +
            "\ta.id >0\n" +
            "\tand a.phone = 'xxxx'";


    //多层嵌套，where 条件中用上一层的字段作为筛选
    String s3 = "select\n" +
            "\tb.ph,\n" +
            "\tb.create_time as btime\n" +
            "from\n" +
            "\t(\n" +
            "\tselect\n" +
            "\t\tmenuName,\n" +
            "\t\tlogin_name,\n" +
            "\t\tph,\n" +
            "\t\ta.create_time\n" +
            "\tfrom\n" +
            "\t\t(\n" +
            "\t\tselect\n" +
            "\t\t\tphone as ph,\n" +
            "\t\t\ttu.login_name,\n" +
            "\t\t\ttm.create_time,\n" +
            "\t\t\ttm.menu_name as menuName\n" +
            "\t\tfrom\n" +
            "\t\t\ttb_user tu\n" +
            "\t\tleft join tb_menu tm \n" +
            "\t\t\ton\n" +
            "\t\t\ttu.id = tm.id) a\n" +
            "\twhere\n" +
            "\t\tph >1\n" +
            "\t\t\t) b";

    // select (select xxx from ) from
    String s4 = "select\n" +
            "\ttu.phone as ph ,\n" +
            "\tmenu_name as mName,\n" +
            "\t(select tm2.menu_name from tb_menu tm2 where tm2.id = tm.id ) as m2Name\n" +
            "from\n" +
            "\ttb_user tu\n" +
            "left join tb_menu tm \n" +
            "on tu.id = tm.id";

    //select (select xxx from ) + 嵌套
    String s5 = "select\n" +
            "\ta.*\n" +
            "from \n" +
            "\t\t(\n" +
            "\tselect\n" +
            "\t\t tu.phone  as ph,\n" +
            "\t\tmenu_name as mName,\n" +
            "\t\t(\n" +
            "\t\tselect\n" +
            "\t\t\ttm2.menu_name\n" +
            "\t\tfrom\n" +
            "\t\t\ttb_menu tm2\n" +
            "\t\twhere\n" +
            "\t\t\ttm2.id = tm.id) as m2Name,\n" +
            "\t\t(\n" +
            "\t\tselect\n" +
            "\t\t\ttu.phone\n" +
            "\t\tfrom\n" +
            "\t\t\ttb_menu tm3\n" +
            "\t\twhere\n" +
            "\t\t\ttm3.id = tu.id\n" +
            "\t\t\tand tu.phone = 'yyyy'\n" +
            "\t\t) as m3Ph\n" +
            "\tfrom\n" +
            "\t\ttb_user tu\n" +
            "\tleft join tb_menu tm on\n" +
            "\t\ttu.id = tm.id) a";


    // union
    String s6 = "\t\t\n" +
            "\t\tselect * from tb_user tu \n" +
            "\t\tunion all \n" +
            "\t\tselect * from tb_user tu2 ";

    String s7 = "select\n" +
            "\tb.ph,\n" +
            "\tb.create_time as btime\n" +
            "from\n" +
            "\t(\n" +
            "\tselect\n" +
            "\t\tmenuName,\n" +
            "\t\tlogin_name,\n" +
            "\t\tph,\n" +
            "\t\ta.create_time\n" +
            "\tfrom\n" +
            "\t\t(\n" +
            "\t\tselect\n" +
            "\t\t\tphone as ph,\n" +
            "\t\t\ttu.login_name,\n" +
            "\t\t\ttm.create_time,\n" +
            "\t\t\ttm.menu_name as menuName\n" +
            "\t\tfrom\n" +
            "\t\t\ttb_user tu\n" +
            "\t\tleft join tb_menu tm \n" +
            "\t\t\ton\n" +
            "\t\t\ttu.id = tm.id) a\n" +
            "\twhere\n" +
            "\t\tph >1\n" +
            "\t\t\t) b";

    // 同一张表一个字段出现多次，但别名不同  （tb_menu 表的 mmenu_name）
    String s8 = "select\n" +
            "\tb.ph,\n" +
            "\tb.create_time as btime\n" +
            "from\n" +
            "\t(\n" +
            "\tselect\n" +
            "\t\tmenuName,\n" +
            "\t\tlogin_name,\n" +
            "\t\tph,\n" +
            "\t\ta.create_time\n" +
            "\tfrom\n" +
            "\t\t(\n" +
            "\t\tselect\n" +
            "\t\t\tphone as ph,\n" +
            "\t\t\ttu.login_name,\n" +
            "\t\t\ttm.menu_name as menuName,\n" +
            "\t\t\ttm.*\n" +
            "\t\tfrom\n" +
            "\t\t\ttb_user tu\n" +
            "\t\tleft join tb_menu tm \n" +
            "\t\t\ton\n" +
            "\t\t\ttu.id = tm.id) a\n" +
            "\twhere\n" +
            "\t\tph >1\n" +
            "\t\t\t) b";

    //case when
    String s9 = "select\n" +
            "\tphone as ph,\n" +
            "\tcase tm.menu_name \n" +
            "\twhen '1' then \n" +
            "\tphone  \n" +
            "\telse \n" +
            "\ttu.user_name \n" +
            "\tend as xxx,\n" +
            "\tcase tu.phone \n" +
            "\twhen '111' then \n" +
            "\ttu.phone \n" +
            "\telse \n" +
            "\ttm.menu_name \n" +
            "\tend as yyy,\n" +
            "\tcase  \n" +
            "\twhen tu.phone = '333333' then \n" +
            "\ttu.phone \n" +
            "\telse \n" +
            "\ttm.create_time \n" +
            "\tend as zzz,\n" +
            "\ttu.login_name,\n" +
            "\ttm.menu_name as menuName,\n" +
            "\ttm.*\n" +
            "from\n" +
            "\ttb_user tu\n" +
            "left join tb_menu tm \n" +
            "\ton\n" +
            "\ttu.id = tm.id";


    // exists
    String s10 = "\tselect * from \n" +
            "\ttb_user tu \n" +
            "\twhere \n" +
            "\texists ( select count(1) from tb_menu tm where tm.id = tu.id \n" +
            "\tand tm.menu_name  = tu.phone and tu.phone like '%xxx%' \n" +
            "\t)";

    // select 查询字段 和where 带function  没别名
    String s11 = "select \n" +
            "concat('xxx:',tu.phone)\n" +
            "from tb_user tu \n" +
            "where concat('yyy:',tu.phone) like '1840'";

    //select 查询字段 和where 带function  有别名
    String s12 = "select \n" +
            "concat('xxx:',tu.phone) as ph\n" +
            "from tb_user tu \n" +
            "where concat('yyy:',tu.phone) like '1840'";

    // select function 里面存在多个列
    String s13 = "select\n" +
            "\tconcat('xxx:', tu.phone , tm.id) as ph\n" +
            "from\n" +
            "\ttb_user tu\n" +
            "left join tb_menu tm \n" +
            "on\n" +
            "\ttu.id = tm.id\n" +
            "where\n" +
            "\tconcat('yyy:', tu.phone, tm.id) like '1840'";

    //select 嵌套查询，where 条件里面是function 处理后的别名
    String s14 = "select \n" +
            "a.*\n" +
            "from \n" +
            "(select\n" +
            "\tconcat('xxx:', tu.phone , tm.id) as ph,\n" +
            "\ttu.*\n" +
            "from\n" +
            "\ttb_user tu\n" +
            "left join tb_menu tm \n" +
            "on\n" +
            "\ttu.id = tm.id\n" +
            "where\n" +
            "\tconcat('yyy:', tu.phone, tm.id) like '1840')a\n" +
            "where a.ph = 'tttt'";

    //select a.* from (select function )
    String s15 = "\tselect \n" +
            "\t\ta.* \n" +
            "\t\tfrom (\n" +
            "\t\tselect \n" +
            "\t\tconcat(tu.phone,'-',tu.create_time) as fff\n" +
            "\t    from tb_user tu )a";

    //where 中带case
    String s16 = "\tselect\n" +
            "\t*\n" +
            "from tb_user tu\n" +
            "left join tb_menu tm \n" +
            "on tu.id = tm.id\n" +
            "where tu.phone like \"%aaa%\"\n" +
            "and\n" +
            "case tu.phone\n" +
            "when 'zzz' then tu.phone like 'ggg'\n" +
            "when 'xxx' then tm.id > 10\n" +
            "end";

    // =  != 时，避免列运算，将Column 另外一边的进行加解密
    String s17 = "SELECT * from tb_user tu \n" +
            "WHERE  tu.phone = 'yyy'\n" +
            "and 'xxx' = tu.phone \n" +
            "and tu.phone = concat('xxx','yyy')\n" +
            "and tu.phone != 'zzzz'\n" +
            "and tu.user_name = '%xxx%'";

    // in 时，避免列运算，将Column 另外一边的进行加解密
    String s18 = "SELECT *\n" +
            "from tb_user tu \n" +
            "WHERE tu.phone not in ('1842','13578')";

    // in (select xxx from) 子查询语法 todo-ltq
    String s19 = "select \n" +
            "*\n" +
            "from tb_user tu \n" +
            "where  tu.phone in (\n" +
            "select t.phone from tb_user t \n" +
            "where t.phone = 'yyyy'\n" +
            ")";

    // in 前面不是 字段
    String s20 = "select * from tb_user tu \n" +
            "where  concat(\"aaa\",tu.phone) in ('111','222')";

    // 测试convert函数如何拼接的 (JsqlParse不支持 convert函数！！！)
    String s21 = "SELECT \n" +
            "convert(tu.phone using utf8mb4)\n" +
            "from tb_user tu";

    //使用 cast 函数 某些场景下平替 convert 函数 （说的场景就是 AES_DECRYPT 中文解密乱码，点名批评一下）
    String s22 = "select cast(tu.phone as char) from tb_user tu";

    // -----------------insert 测试语句---------------------
    String i1 = "insert into tb_user(id, user_name ,phone) \n" +
            "values(1,'西瓜','18243512315'),(2,'南瓜','18243121315')";

    // insert select 语句
    String i2 = "insert into \n" +
            "tb_user(user_name,phone)\n" +
            "(\n" +
            "select  user_name,phone from  tb_user  tu\n" +
            "where tu.phone is not null \n" +
            ")";


    // --------------delete 测试语句 ---------------

    // delet join
    String d1 = "delete tu,tm \n" +
            "from tb_user tu \n" +
            "join tb_menu tm \n" +
            "on tu.id = tm.id \n" +
            "where tu.phone = 'xxx'";

    // delte 一张表
    String d2 = "\t delete from tb_user \n" +
            "\twhere phone like '%xxx%' ";

    // --------------update 测试语句 ---------------

    //update 联表  set的时候存在 其它表的值，也存在常量值
    String u1 = "update tb_user tu \n" +
            "join tb_menu tm \n" +
            "on tu.id = tm.id \n" +
            "set tm.menu_name = tm.`path` ,\n" +
            "tu.phone = 'yyy'\n" +
            "where tu.phone like '%xxx%'";

    //update 联多张表  set的时候存在 其它表的值，也存在常量值
    String u2 = "update tb_user tu \n" +
            "join tb_menu tm \n" +
            "on tu.id = tm.id \n" +
            "join tb_user tu2 \n" +
            "on tu.id = tu2.id \n" +
            "set tu.phone = tm.`path` ,\n" +
            "tm.menu_name = 'yyy'\n" +
            "where tu.phone like '%xxx%'";

    //update 一张表
    String u3 = "\tupdate tb_user \n" +
            "\tset create_time = now(),\n" +
            "\tphone = 'xxx'\n" +
            "\twhere phone = 'yyy'";

    @Test
    public void testSql() throws JSQLParserException, NoSuchFieldException {
        //初始化加解密函数
        FieldEncryptorPatternCache.initDeafultInstance();
        //mock数据
        InitTableInfo.initTable();

        //需要测试的sql
        String sql = s22;
        System.out.println("----------------------------------------------------------------------------");
        System.out.println(sql);
        System.out.println("----------------------------------------------------------------------------");

        //开始解析sql
        Statement statement = CCJSqlParserUtil.parse(sql);

        DencryptStatementVisitor dencryptStatementVisitor = new DencryptStatementVisitor();
        statement.accept(dencryptStatementVisitor);
        System.out.println("----------------------------------------------------------------------------");
        System.out.println((dencryptStatementVisitor.getResultSql()));
        System.out.println("----------------------------------------------------------------------------");

    }

}
