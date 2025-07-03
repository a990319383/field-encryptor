package com.sangsang.cache.isolation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sangsang.config.properties.FieldProperties;
import com.sangsang.domain.annos.isolation.IsolationData;
import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.domain.enums.IsolationRelationEnum;
import com.sangsang.domain.exception.IsolationException;
import com.sangsang.domain.strategy.DefaultStrategyBase;
import com.sangsang.domain.strategy.isolation.IsolationDataStrategy;
import com.sangsang.util.ClassScanerUtil;
import com.sangsang.util.CollectionUtils;
import com.sangsang.util.ExpressionsUtil;
import com.sangsang.util.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据隔离相关缓存
 * 优先加载这个bean，避免有些@PostConstruct 处理逻辑中需要用到这个缓存，但是这个缓存还未初始化完成
 *
 * @author liutangqi
 * @date 2025/6/13 10:29
 */
@Slf4j
public class IsolationInstanceCache implements BeanPostProcessor {
    /**
     * 缓存当前数据隔离实现类实例
     * key: 实现类的className
     * value:实例
     *
     * @author liutangqi
     * @date 2025/6/13 10:31
     * @Param
     **/
    private static final Map<Class<? extends IsolationDataStrategy>, IsolationDataStrategy> INSTANCE_MAP = new HashMap<>();

    /**
     * 缓存当前表的数据隔离信息
     * key: 表名小写
     * value: 此表对应的隔离策略实例对象
     *
     * @author liutangqi
     * @date 2025/6/13 10:32
     * @Param
     **/
    private static final Map<String, IsolationDataStrategy> TABLE_ISOLATION_MAP = new HashMap<>();

    /**
     * 存储当前需要进行数据隔离的小写的表名
     *
     * @author liutangqi
     * @date 2025/7/3 10:36
     * @Param
     **/
    @Getter
    private static final Set<String> ISOLATION_TABLE = new HashSet<>();

    /**
     * 初始化
     *
     * @author liutangqi
     * @date 2025/6/13 10:33
     * @Param []
     **/
    public void init(FieldProperties fieldProperties,
                     List<IsolationDataStrategy> isolationDataStrategyList) throws Exception {
        long startTime = System.currentTimeMillis();
        //1.配置校验
        if (CollectionUtils.isEmpty(fieldProperties.getScanEntityPackage())) {
            throw new IsolationException("未配置扫描路径，请检查配置 field.scanEntityPackage");
        }

        //2.实例化默认的策略
        DefaultStrategyBase.IsolationBeanStrategy isolationBeanStrategy = new DefaultStrategyBase.IsolationBeanStrategy(isolationDataStrategyList);
        INSTANCE_MAP.put(DefaultStrategyBase.IsolationBeanStrategy.class, isolationBeanStrategy);

        //3.初始化当前spring容器内的实现策略
        for (IsolationDataStrategy isolationDataStrategy : isolationDataStrategyList) {
            INSTANCE_MAP.put(isolationDataStrategy.getClass(), isolationDataStrategy);
        }

        //4.缓存表和@DataIsolation 的对应关系 ，顺便记录哪些表涉及到数据隔离
        for (String scanEntityPackage : fieldProperties.getScanEntityPackage()) {
            doScanEntityPackage(scanEntityPackage);
        }

        log.info("【isolation】初始化完毕，耗时:{}ms", (System.currentTimeMillis() - startTime));
    }

    /**
     * 扫描指定路径下标注了@TableName 的@DataIsolation 的类信息，维护表名和@DataIsolation 的对应关系
     * 并将spring容器中不存在的bean但是存在@DataIsolation有使用到的，进行实例化，并缓存
     *
     * @author liutangqi
     * @date 2025/6/13 10:40
     * @Param [scanEntityPackage]
     **/
    private void doScanEntityPackage(String scanEntityPackage) {
        //1.扫描指定路径下的的标注类
        Set<Class> entityClasses = ClassScanerUtil.scan(scanEntityPackage, TableName.class, IsolationData.class).stream().filter(f -> f.getAnnotation(TableName.class) != null && f.getAnnotation(IsolationData.class) != null).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(entityClasses)) {
            log.warn("【isolation】未找到标注了@TableName 的@DataIsolation 的类信息，请检查配置");
            return;
        }

        //2.处理表和对应表的数据隔离信息
        for (Class entityClass : entityClasses) {
            //2.1获取类上面的两个注解
            TableName tableName = (TableName) entityClass.getAnnotation(TableName.class);
            IsolationData dataIsolation = (IsolationData) entityClass.getAnnotation(IsolationData.class);
            String lowerTableName = tableName.value().toLowerCase();
            //2.2校验配置是否正确
            IsolationDataStrategy isolationDataStrategy = INSTANCE_MAP.get(dataIsolation.value());
            if (isolationDataStrategy == null) {
                throw new IsolationException(String.format("当前表%s 配置的隔离策略 %s spring容器中找不到", tableName.value(), dataIsolation.value()));
            }
            //2.3缓存表和数据隔离实例关系
            TABLE_ISOLATION_MAP.put(lowerTableName, isolationDataStrategy);
            //2.4记录当前需要涉及到数据隔离的所有表
            ISOLATION_TABLE.add(lowerTableName);
        }
    }


    //------------------------------------------------------分割线----------------------------------------------------------------

    /**
     * 实现父类default方法，避免低版本不兼容，找不到实现类
     *
     * @author liutangqi
     * @date 2025/6/13 10:36
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
     * @date 2025/6/13 10:36
     * @Param [bean, beanName]
     **/
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    //--------------------------------------------下面是对外提供的方法---------------------------------------------------------------


    /**
     * 根据表名得到获取当前登录的数据隔离信息
     *
     * @author liutangqi
     * @date 2025/6/13 10:58
     * @Param [tableName]
     **/
    public static IsolationDataStrategy getInstance(String tableName) {
        //看这个表是否需要数据隔离
        return CollectionUtils.getValueIgnoreFloat(TABLE_ISOLATION_MAP, tableName.toLowerCase());
    }


    /**
     * 构建权限过滤的表达式
     *
     * @author liutangqi
     * @date 2025/6/13 13:10
     * @Param [isolationField：字段名, tableAlias: 字段所属表别名, isolationRelationEnum ：逻辑关系, isolationValue：数据隔离值]
     **/
    public static Expression buildIsolationExpression(String isolationField, String tableAlias, IsolationRelationEnum isolationRelationEnum, Object isolationValue) {
        //1.类型校验
        if (!IsolationDataStrategy.ALLOW_TYPES.stream().filter(f -> f.isAssignableFrom(isolationValue.getClass())).findAny().isPresent()) {
            throw new IsolationException(String.format("数据隔离值类型不支持 %s", isolationValue.getClass().getName()));
        }

        //2.拼凑字段
        Column column = new Column(isolationField);
        if (StringUtils.isNotBlank(tableAlias)) {
            column.setTable(new Table(tableAlias));
        }

        //3.拼凑值
        Expression valueExpression = null;
        if (isolationValue instanceof List) {
            List expressionList = ExpressionsUtil.buildExpressionList((List) isolationValue);
            ParenthesedExpressionList parenthesedExpressionList = new ParenthesedExpressionList();
            parenthesedExpressionList.addAll(expressionList);
            valueExpression = parenthesedExpressionList;
        } else {
            valueExpression = ExpressionsUtil.buildConstant(isolationValue);
        }

        //4.拼凑表达式
        // field = value
        if (IsolationRelationEnum.EQUALS.equals(isolationRelationEnum)) {
            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(column);
            equalsTo.setRightExpression(valueExpression);
            return equalsTo;
        }

        //field lile 'value%'
        if (IsolationRelationEnum.LIKE_PREFIX.equals(isolationRelationEnum)) {
            LikeExpression likeExpression = new LikeExpression();
            likeExpression.setLeftExpression(column);
            likeExpression.setRightExpression(new StringValue(isolationValue + SymbolConstant.PER_CENT));
            return likeExpression;
        }

        // field in (xxx,xxx)
        if (IsolationRelationEnum.IN.equals(isolationRelationEnum)) {
            InExpression inExpression = new InExpression();
            inExpression.setLeftExpression(column);
            inExpression.setRightExpression(valueExpression);
            return inExpression;
        }

        throw new IsolationException(String.format("错误的数据隔离关系 %s", isolationRelationEnum));
    }
}
