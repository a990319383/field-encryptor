package com.sangsang.interceptor;

import cn.hutool.core.lang.Pair;
import com.sangsang.cache.FieldEncryptorPatternCache;
import com.sangsang.domain.constants.DecryptConstant;
import com.sangsang.domain.constants.PatternTypeConstant;
import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.FieldEncryptorInfoDto;
import com.sangsang.util.JsqlparserUtil;
import com.sangsang.util.StringUtils;
import com.sangsang.visitor.pojoencrtptor.PoJoEncrtptorStatementVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采用java 函数对pojo处理的加解密模式
 * 处理入参
 *
 * @author liutangqi
 * @date 2024/7/9 14:06
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class PoJoParamEncrtptorInterceptor implements Interceptor {

    private ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    private ObjectFactory objectFactory = new DefaultObjectFactory();
    private ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    /**
     * 将入参的字段和占位符？ 对应起来  （boundSql.getParameterMappings()获取的参数和占位符的顺序是一致的，这个结果集里面也有对应的占位符的key，这样就可以全部关联起来了）
     * 思路： 将boundsql 中的？ 占位符替换为 XXX特殊符号防重_1  XXX特殊符号防重_2  XXX特殊符号防重_3  这种，解析时就能得到占位符合参数的对应关系
     * 得到关系后再对请求参数进行加解密处理，因为这个时候我们已经知道该参数对应的数据库表字段是哪个了
     * 处理完后，将我们替换后的 _XXX特殊符号防重_1  这种重新替换为？  这样就能解决这个问题，并且不会存在破坏预编译sql导致sql注入的问题了
     *
     * @author liutangqi
     * @date 2024/7/18 14:45
     * @Param [invocation]
     **/
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //1.获取基础信息
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String originalSql = boundSql.getSql();

        //2.解析sql,获取入参和响应对应的表字段关系
        Pair<Map<String, ColumnTableDto>, List<FieldEncryptorInfoDto>> pair = parseSql(originalSql);

        //3.处理入参
        disposeParam(boundSql, pair);

        //4.执行返回
        return invocation.proceed();
    }


    /**
     * 解析sql,获取入参和响应对应的表字段关系
     *
     * @author liutangqi
     * @date 2024/7/18 14:55
     * @Param [sql]
     **/
    private Pair<Map<String, ColumnTableDto>, List<FieldEncryptorInfoDto>> parseSql(String sql) throws JSQLParserException {
        //1.将sql中的 ? 占位符替换成我们自定义的特殊符号
        String placeholderSql = StringUtils.question2Placeholder(sql);

        //2.解析sql的响应结果，和占位符对应的表字段关系
        Statement statement = CCJSqlParserUtil.parse(placeholderSql);
        PoJoEncrtptorStatementVisitor poJoEncrtptorStatementVisitor = new PoJoEncrtptorStatementVisitor();
        statement.accept(poJoEncrtptorStatementVisitor);

        //3.获取解析结果
        Map<String, ColumnTableDto> placeholderColumnTableMap = poJoEncrtptorStatementVisitor.getPlaceholderColumnTableMap();
        List<FieldEncryptorInfoDto> fieldEncryptorInfos = poJoEncrtptorStatementVisitor.getFieldEncryptorInfos();
        return Pair.of(placeholderColumnTableMap, fieldEncryptorInfos);
    }

    /**
     * 将入参中需要加密的进行加密处理
     *
     * @author liutangqi
     * @date 2024/7/18 15:18
     * @Param [parameterObject, pair]
     **/
    private void disposeParam(BoundSql boundSql, Pair<Map<String, ColumnTableDto>, List<FieldEncryptorInfoDto>> pair) {
        //1.解析入参和我们自定义占位符的对应关系
        Map<String, ParameterMapping> objectObjectHashMap = new HashMap<>();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        for (int i = 0; i < parameterMappings.size(); i++) {
            objectObjectHashMap.put(DecryptConstant.PLACEHOLDER + i, parameterMappings.get(i));
        }

        //2.将其中需要加密的字段进行加密(注意：这里只返回key value对应关系，不能现在就boundSql.setAdditionalParameter ，否则会导致 parseObj()方法中 hasAdditionalParameter()结果出错 aaa.bbb.ccc 这种方法只判断里面是否有aaa)
        Map<String, Object> propertyMap = new HashMap<>();
        for (ParameterMapping parameterMapping : parameterMappings) {
            //获取当前映射字段的入参值
            Object propertyValue = parseObj(boundSql, parameterMapping);

            //如果需要加密的话，将加密后的值，替换原有入参
            if (propertyValue instanceof String && encrytor(parameterMapping, objectObjectHashMap, pair.getKey())) {
                String ciphertext = FieldEncryptorPatternCache.getBeanInstance().encryption((String) propertyValue);
                propertyMap.put(parameterMapping.getProperty(), ciphertext);
            } else {
                //不需要加密的话，则入参还是使用旧值
                propertyMap.put(parameterMapping.getProperty(), propertyValue);
            }
        }

        //3.将处理好的结果集进行设置值
        for (Map.Entry<String, Object> entry : propertyMap.entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
    }


    /**
     * 根据占位符名字，判断当前字段是否需要加解密
     *
     * @author liutangqi
     * @date 2024/7/24 17:23
     * @Param [propertyMapping:当前占位符映射对象 , objectObjectHashMap 占位符映射对象和自定义占位符对应关系, placeholderColumnTableMap 自定义占位单映射关系]
     **/
    private boolean encrytor(ParameterMapping propertyMapping, Map<String, ParameterMapping> objectObjectHashMap, Map<String, ColumnTableDto> placeholderColumnTableMap) {
        //1.过滤出当前入参对应的所有的自定义占位符（正常情况，一个入参，即使对应多个表字段，这些表字段是否需要加密都是统一的）
        List<String> placeholders = objectObjectHashMap.entrySet()
                .stream()
                .filter(f -> Objects.equals(f.getValue().getProperty(), propertyMapping.getProperty()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        //2.这些入参只要有一个需要加密，则需要进行加密处理
        return placeholders.stream()
                .filter(f -> placeholderColumnTableMap.get(f) != null && JsqlparserUtil.needEncrypt(placeholderColumnTableMap.get(f)))
                .count() > 0;
    }

    /**
     * 解析映射对象的属性值
     *
     * @author liutangqi
     * @date 2024/7/24 14:49
     * @Param [configuration, boundSql, parameter]
     **/
    private Object parseObj(BoundSql boundSql, ParameterMapping parameter) {
        Object obj = boundSql.getParameterObject();
        String property = parameter.getProperty();

        //0.判断boundsql中AdditionalParameter是否存在，存在就取boundsql中的(当入参在实体类中存在List时会走这段逻辑)
        if (boundSql.hasAdditionalParameter(property)) {
            return boundSql.getAdditionalParameter(property);
        }

        //1. 基本数据类型的包装类或者字符串或时间类型，直接返回原值
        if (DecryptConstant.FUNDAMENTAL.contains(obj.getClass())) {
            return obj;
        }

        //2.其它类型的值，通过反射获取，如果入参是  dto.xxx 这种，则分开解析每一段，直至获取最终值
        String[] propertyArr = property.split(SymbolConstant.ESC_FULL_STOP);

        //上一层对象
        Object pre = obj;
        for (String prop : propertyArr) {
            pre = MetaObject.forObject(pre, objectFactory, objectWrapperFactory, reflectorFactory).getValue(prop);
        }
        return pre;
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
