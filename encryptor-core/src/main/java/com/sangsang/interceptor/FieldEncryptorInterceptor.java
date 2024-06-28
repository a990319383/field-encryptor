package com.sangsang.interceptor;

import com.sangsang.cache.TableCache;
import com.sangsang.util.StringUtils;
import com.sangsang.visitor.encrtptor.DencryptStatementVisitor;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 将sql需要加解密的字段进行加解密处理
 *
 * @author liutangqi
 * @date 2023/11/9 19:03
 */
@Component
@ConditionalOnClass(name = "org.apache.ibatis.plugin.Interceptor")
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class FieldEncryptorInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(FieldEncryptorInterceptor.class);

    private static final Random random = new Random();


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //1.获取拦截器中提供的一些对象
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

        //2.获取当前执行的sql
        BoundSql boundSql = statementHandler.getBoundSql();
        String oldSql = boundSql.getSql();
        log.debug("【FieldEncryptor】旧sql：{}", oldSql);

        //3.判断表结构缓存是否加载完毕，没有加载完毕的话，自旋等待，直到加载完毕后再执行
        spinAwait();

        //4.将原sql进行加解密处理
        String newSql = oldSql;
        try {
            Statement statement = CCJSqlParserUtil.parse(oldSql);
            DencryptStatementVisitor dencryptStatementVisitor = new DencryptStatementVisitor();
            statement.accept(dencryptStatementVisitor);
            if (StringUtils.isNotBlank(dencryptStatementVisitor.getResultSql())) {
                newSql = dencryptStatementVisitor.getResultSql();
            }
            log.debug("【FieldEncryptor】新sql：{}", newSql);
        } catch (Exception e) {
            log.error("【FieldEncryptor】加解密sql异常 原sql:{}", oldSql, e);
        }

        //5.反射修改 SQL 语句。
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, newSql);

        //6.执行修改后的 SQL 语句。
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    /**
     * 自旋等待，直至表结构缓存加载完毕
     *
     * @author liutangqi
     * @date 2024/6/27 18:21
     * @Param []
     **/
    private static void spinAwait() {
        int count = 0;
        //最大自旋次数，超过这个次数，此处sql抛异常
        int maxspinCount = 30;
        while (!TableCache.initFinish() && count < maxspinCount) {
            log.info("【FieldEncryptor】当前表结构还未加载完，sql执行开始自旋等待");
            try {
                //随机休眠，避免启动时请求都休眠同样的时间，导致同一时间访问数据库，增加数据库压力
                int sleepTime = random.nextInt(201 - 100) + 100;
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                count++;
            } catch (Exception e) {
                log.error("【FieldEncryptor】自旋异常", e);
            }
        }

        if (count >= maxspinCount) {
            throw new RuntimeException("【FieldEncryptor】自旋等待超时");
        }

    }


}
