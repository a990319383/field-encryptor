package com.sangsang.interceptor;

import com.sangsang.cache.TransformationSqlCache;
import com.sangsang.domain.annos.FieldInterceptorOrder;
import com.sangsang.domain.constants.InterceptorOrderConstant;
import com.sangsang.domain.context.TransformationHolder;
import com.sangsang.util.InterceptorUtil;
import com.sangsang.util.StringUtils;
import com.sangsang.visitor.transformation.TransformationStatementVisitor;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Properties;

/**
 * 进行sql语法转换的拦截器
 *
 * @author liutangqi
 * @date 2025/5/21 10:28
 */
@FieldInterceptorOrder(InterceptorOrderConstant.TRANSFORMATION)
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class TransformationInterceptor implements Interceptor, BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(TransformationInterceptor.class);


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //1.获取拦截器中提供的一些对象
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

        //2.获取当前执行的sql
        BoundSql boundSql = statementHandler.getBoundSql();
        String oldSql = boundSql.getSql();

        //3.如果当前sql肯定不需要语法转换，则直接执行
        if (TransformationSqlCache.isNeedlessTransformationSql(oldSql)) {
            return invocation.proceed();
        }

        //4.将原sql进行语法转换
        String newSql = oldSql;
        try {
            log.debug("【db-transformation】旧sql：{}", oldSql);
            Statement statement = CCJSqlParserUtil.parse(StringUtils.replaceLineBreak(oldSql));
            TransformationStatementVisitor transformationStatementVisitor = new TransformationStatementVisitor();
            statement.accept(transformationStatementVisitor);
            if (StringUtils.isNotBlank(transformationStatementVisitor.getResultSql())) {
                newSql = transformationStatementVisitor.getResultSql();
            }
            log.debug("【db-transformation】新sql：{}", newSql);

            //5.如果当前sql语句未发生了语法转换，则将当前sql放入缓存中，避免下次重复处理
            if (!TransformationHolder.isTransformation()) {
                TransformationSqlCache.addNeedlessTransformationSql(oldSql);
            }
        } catch (Exception e) {
            log.error("【db-transformation】语法转换 sql异常 原sql:{}", oldSql, e);
        } finally {
            //手动清除，避免内存泄漏
            TransformationHolder.clear();
        }

        //6.反射修改 SQL 语句。
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, newSql);

        //8.执行修改后的 SQL 语句。
        return invocation.proceed();
    }

    /**
     * 低版本mybatis 这个方法不是default 方法，会报错找不到实现方法，所以这里实现默认的方法
     *
     * @author liutangqi
     * @date 2025/5/21 10:28
     * @Param [target]
     **/
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }


    /**
     * 实现父类default方法，避免低版本不兼容，找不到实现类
     *
     * @author liutangqi
     * @date 2025/5/21 10:28
     * @Param [bean, beanName]
     **/
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 实现父类default方法，避免低版本不兼容，找不到实现类
     *
     * @author liutangqi
     * @date 2025/5/21 10:28
     * @Param [bean, beanName]
     **/
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //当前没有注册此拦截器，则手动注册，避免有些项目自定义了SqlSessionFactory 导致拦截器漏注册
        //使用@Bean的方式注册，可能会导致某些项目的@PostContruct先于拦截器执行，导致拦截器业务代码失效
        if (SqlSessionFactory.class.isAssignableFrom(bean.getClass())) {
            SqlSessionFactory sessionFactory = (SqlSessionFactory) bean;
            if (sessionFactory.getConfiguration().getInterceptors().stream().filter(f -> TransformationInterceptor.class.isAssignableFrom(f.getClass())).findAny().orElse(null) == null) {
                sessionFactory.getConfiguration().addInterceptor(new TransformationInterceptor());
                log.info("【db-transformation】手动注册拦截器 TransformationInterceptor");
            }

            //修改拦截器顺序
            InterceptorUtil.sort(sessionFactory.getConfiguration());
        }
        return bean;
    }
}