package com.sangsang.interceptor;

import com.sangsang.util.StringUtils;
import com.sangsang.visitor.dbencrtptor.DBDencryptStatementVisitor;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Properties;

/**
 * 采用数据库函数加解密模式
 * 将sql需要加解密的字段进行加解密处理
 *
 * @author liutangqi
 * @date 2023/11/9 19:03
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DBFieldEncryptorInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(DBFieldEncryptorInterceptor.class);


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //1.获取拦截器中提供的一些对象
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

        //2.获取当前执行的sql
        BoundSql boundSql = statementHandler.getBoundSql();
        String oldSql = boundSql.getSql();
        log.debug("【FieldEncryptor】旧sql：{}", oldSql);

        //3.将原sql进行加解密处理
        String newSql = oldSql;
        try {
            Statement statement = CCJSqlParserUtil.parse(oldSql);
            DBDencryptStatementVisitor DBDencryptStatementVisitor = new DBDencryptStatementVisitor();
            statement.accept(DBDencryptStatementVisitor);
            if (StringUtils.isNotBlank(DBDencryptStatementVisitor.getResultSql())) {
                newSql = DBDencryptStatementVisitor.getResultSql();
            }
            log.debug("【FieldEncryptor】新sql：{}", newSql);
        } catch (Exception e) {
            log.error("【FieldEncryptor】加解密sql异常 原sql:{}", oldSql, e);
        }

        //4.反射修改 SQL 语句。
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, newSql);

        //5.执行修改后的 SQL 语句。
        return invocation.proceed();
    }

    /**
     * 低版本mybatis 这个方法不是default 方法，会报错找不到实现方法，所以这里实现默认的方法
     *
     * @author liutangqi
     * @date 2024/9/9 17:38
     * @Param [target]
     **/
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

}
