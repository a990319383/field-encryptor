package com.sangsang.interceptor;

import com.sangsang.domain.constants.DecryptConstant;
import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.util.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Objects;

/**
 * 对javabean进行加解密模式的查询语句解密拦截器
 *
 * @author liutangqi
 * @date 2024/7/9 14:06
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class BeanEncrtptorQueryInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //1.获取核心类(@Signature 后面的args顺序和下面获取的一致)
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        BoundSql boundSql = (BoundSql) invocation.getArgs()[5];


        String originalSql = boundSql.getSql();


        //2.将入参的字段和占位符？ 对应起来  （boundSql.getParameterMappings()获取的参数和占位符的顺序是一致的，这个结果集里面也有对应的占位符的key，这样就可以全部关联起来了）
        //思路： 将boundsql 中的？ 占位符替换为 ?_XXX特殊符号防重_1  ?_XXX特殊符号防重_2  ?_XXX特殊符号防重_3  这种，解析时就能得到占位符合参数的对应关系
        //得到关系后再对请求参数进行加解密处理，因为这个时候我们已经知道该参数对应的数据库表字段是哪个了
        //处理完后，将我们替换后的 _XXX特殊符号防重_1  这种重新替换为？  这样就能解决这个问题，并且不会存在破坏预编译sql导致sql注入的问题了

        //2.获取入参
        ParameterMap parameterMap = mappedStatement.getParameterMap();


        return null;
    }


    public static void main(String[] args) {
        String str = "select \n" +
                "? as d ,\n" +
                "a.* \n" +
                "from \n" +
                "(select * from tb_schedule ts \n" +
                "where ts.schedule_no  = ?\n" +
                "union \n" +
                "select * from tb_schedule ts \n" +
                "where ts.schedule_no  = ?)a\n" +
                "left join tb_schedule ts2 \n" +
                "on a.schedule_no = ts2.schedule_no \n" +
                "where ts2.id = ?";

        String sql1 = StringUtils.question2Placeholder(str);
        String sql2 = StringUtils.placeholder2Question(sql1);
        System.out.println(sql1);
        System.out.println(sql2);
    }
}
