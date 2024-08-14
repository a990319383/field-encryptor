package com.sangsang.interceptor;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.sangsang.cache.FieldEncryptorPatternCache;
import com.sangsang.domain.annos.FieldEncryptor;
import com.sangsang.domain.constants.DecryptConstant;
import com.sangsang.domain.constants.PatternTypeConstant;
import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.domain.dto.ColumnTableDto;
import com.sangsang.domain.dto.FieldEncryptorInfoDto;
import com.sangsang.util.JsqlparserUtil;
import com.sangsang.util.ReflectUtils;
import com.sangsang.util.StringUtils;
import com.sangsang.visitor.beanencrtptor.BeanEncrtptorStatementVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采用java 函数对bean处理的加解密模式
 *
 * @author liutangqi
 * @date 2024/7/9 14:06
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
@ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = PatternTypeConstant.BEAN)
public class BeanEncrtptorInterceptor implements Interceptor {

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
        //1.获取核心类(@Signature 后面的args顺序和下面获取的一致)
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        String originalSql = boundSql.getSql();

        //2.解析sql,获取入参和响应对应的表字段关系
        Pair<Map<String, ColumnTableDto>, List<FieldEncryptorInfoDto>> pair = parseSql(originalSql);

        //3.处理入参
        disposeParam(boundSql, configuration, pair);

        //4.执行sql
        Object result = invocation.proceed();

        //5.处理响应
        result = disposeResult(result, pair);

        return result;
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
        BeanEncrtptorStatementVisitor beanEncrtptorStatementVisitor = new BeanEncrtptorStatementVisitor();
        statement.accept(beanEncrtptorStatementVisitor);

        //3.获取解析结果
        Map<String, ColumnTableDto> placeholderColumnTableMap = beanEncrtptorStatementVisitor.getPlaceholderColumnTableMap();
        List<FieldEncryptorInfoDto> fieldEncryptorInfos = beanEncrtptorStatementVisitor.getFieldEncryptorInfos();
        return Pair.of(placeholderColumnTableMap, fieldEncryptorInfos);
    }

    /**
     * 将入参中需要加密的进行加密处理
     *
     * @author liutangqi
     * @date 2024/7/18 15:18
     * @Param [parameterObject, pair]
     **/
    private void disposeParam(BoundSql boundSql, Configuration configuration, Pair<Map<String, ColumnTableDto>, List<FieldEncryptorInfoDto>> pair) {
        //1.解析入参和我们自定义占位符的对应关系
        Map<String, ParameterMapping> objectObjectHashMap = new HashMap<>();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        for (int i = 0; i < parameterMappings.size(); i++) {
            objectObjectHashMap.put(DecryptConstant.PLACEHOLDER + i, parameterMappings.get(i));
        }

        //2.将其中需要加密的字段进行加密
        for (ParameterMapping parameterMapping : parameterMappings) {
            //获取当前映射字段的入参值
            Object propertyValue = parseObj(configuration, boundSql, parameterMapping);

            //如果需要加密的话，将加密后的值，替换原有入参
            if (propertyValue instanceof String && encrytor(parameterMapping, objectObjectHashMap, pair.getKey())) {
                String ciphertext = FieldEncryptorPatternCache.getBeanInstance().encryption((String) propertyValue);
                boundSql.setAdditionalParameter(parameterMapping.getProperty(), ciphertext);

            } else {
                //不需要加密的话，则入参还是使用旧值
                boundSql.setAdditionalParameter(parameterMapping.getProperty(), propertyValue);
            }

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
     * @Param [configuration, obj, parameter]
     **/
    private Object parseObj(Configuration configuration, BoundSql boundSql, ParameterMapping parameter) {
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
            pre = configuration.newMetaObject(pre).getValue(prop);
        }
        return pre;
    }


    /**
     * 将响应结果中需要解密的进行解密处理
     *
     * @author liutangqi
     * @date 2024/7/26 15:52
     * @Param [result, pair]
     **/
    private Object disposeResult(Object result, Pair<Map<String, ColumnTableDto>, List<FieldEncryptorInfoDto>> pair) throws IllegalAccessException {
        //1.sql执行结果为空，直接返回
        List<Object> resList = (List<Object>) result;
        if (CollectionUtils.isEmpty(resList)) {
            return resList;
        }

        //2.当前执行的sql 查询的所有字段信息
        List<FieldEncryptorInfoDto> fieldInfos = pair.getValue();

        //3.依次对结果的每一个字段进行处理
        List decryptorRes = new ArrayList();
        for (Object res : resList) {
            decryptorRes.add(decryptor(res, fieldInfos));
        }

        return decryptorRes;
    }


    /**
     * 对需要加密的对象属性进行加密
     *
     * @author liutangqi
     * @date 2024/7/26 16:24
     * @Param [res, fieldInfos]
     **/
    private Object decryptor(Object res, List<FieldEncryptorInfoDto> fieldInfos) throws IllegalAccessException {
        //1.基础数据类型对应的包装类或字符串或时间类型
        if (DecryptConstant.FUNDAMENTAL.contains(res.getClass())) {
            //1.1 响应类型是字符串，并且该sql 查询结果只有一个字段
            if (res instanceof String && fieldInfos.size() == 1) {
                if (fieldInfos.get(0).getFieldEncryptor() != null) {
                    res = FieldEncryptorPatternCache.getBeanInstance().decryption((String) res);
                    return res;
                }
            }

            //2.响应类型是Map
        } else if (res instanceof Map) {
            Map resMap = (Map) res;
            for (Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) resMap.entrySet()) {
                if (getFieldEncryptorByFieldName(entry.getKey(), fieldInfos) != null) {
                    entry.setValue(FieldEncryptorPatternCache.getBeanInstance().decryption((String) entry.getValue()));
                }
            }

            //3.响应类型是其它实体类
        } else {
            List<Field> allFields = ReflectUtils.getNotStaticFinalFields(res.getClass());
            for (Field field : allFields) {
                if (getFieldEncryptorByFieldName(field.getName(), fieldInfos) != null) {
                    field.setAccessible(true);
                    field.set(res, FieldEncryptorPatternCache.getBeanInstance().decryption((String) field.get(res)));
                }
            }
        }
        return res;
    }

    /**
     * 根据字段名字从sql解析结果中，找到该实体类上面标准的注解信息
     * 注意：fieldName是驼峰的，而fieldInfos 中的信息是下划线的，这里会做自动转换再做匹配
     *
     * @author liutangqi
     * @date 2024/7/26 16:07
     * @Param [fieldName, fieldInfos]
     **/
    private FieldEncryptor getFieldEncryptorByFieldName(String fieldName, List<FieldEncryptorInfoDto> fieldInfos) {
        //驼峰转下划线,并转小写
        String underlineCaseFieldName = StrUtil.toUnderlineCase(fieldName).toLowerCase();
        //从所有字段中查到这个字段的信息（理论上只会存在一个）
        return fieldInfos.stream()
                .filter(f -> Objects.equals(f.getSourceColumn(), underlineCaseFieldName))
                .findAny()
                .map(FieldEncryptorInfoDto::getFieldEncryptor)
                .orElse(null);
    }


}
