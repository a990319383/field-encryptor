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
import com.sangsang.visitor.pojoencrtptor.PoJoEncrtptorStatementVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采用java 函数对pojo处理的加解密模式
 * 处理select的 响应语句
 *
 * @author liutangqi
 * @date 2024/7/9 14:06
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
@ConditionalOnProperty(name = "field.encryptor.patternType", havingValue = PatternTypeConstant.POJO)
public class PoJoResultEncrtptorInterceptor implements Interceptor {


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //1.获取核心类(@Signature 后面的args顺序和下面获取的一致)
        BoundSql boundSql = (BoundSql) invocation.getArgs()[5];
        String originalSql = boundSql.getSql();

        //2.解析sql,获取入参和响应对应的表字段关系
        Pair<Map<String, ColumnTableDto>, List<FieldEncryptorInfoDto>> pair = parseSql(originalSql);

        //3.执行sql
        Object result = invocation.proceed();

        //4.处理响应
        return disposeResult(result, pair);
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
     * 将响应结果中需要解密的进行解密处理
     *
     * @author liutangqi
     * @date 2024/7/26 15:52
     * @Param [result, pair]
     **/
    private Object disposeResult(Object result, Pair<Map<String, ColumnTableDto>, List<FieldEncryptorInfoDto>> pair) throws IllegalAccessException {
        //0.sql执行结果不是Collection直接返回(update insert语句执行时，结果不是Collection)
        if (!(result instanceof Collection)) {
            return result;
        }

        //1.sql执行结果为空，直接返回
        Collection<Object> resList = (Collection<Object>) result;
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
