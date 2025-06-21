package com.sangsang.cache;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sangsang.config.properties.FieldProperties;
import com.sangsang.config.properties.IsolationProperties;
import com.sangsang.domain.annos.DataIsolation;
import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.domain.dto.DataIsolationDto;
import com.sangsang.domain.enums.IsolationRelationEnum;
import com.sangsang.domain.exception.IsolationException;
import com.sangsang.domain.interfaces.DataIsolationInterface;
import com.sangsang.util.ClassScanerUtil;
import com.sangsang.util.CollectionUtils;
import com.sangsang.util.StringUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据隔离相关缓存
 *
 * @author liutangqi
 * @date 2025/6/13 10:29
 */
public class IsolationCache implements BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(IsolationCache.class);

    private FieldProperties fieldProperties;

    public IsolationCache(FieldProperties fieldProperties) {
        this.fieldProperties = fieldProperties;
    }

    /**
     * 缓存当前数据隔离配置
     *
     * @author liutangqi
     * @date 2025/6/13 11:02
     * @Param
     **/
    private static IsolationProperties ISOLATION_PROPERTIES_CACHE;

    /**
     * 缓存当前数据隔离实现类实例
     * key: 实现类的className
     * value:实例
     *
     * @author liutangqi
     * @date 2025/6/13 10:31
     * @Param
     **/
    private static final Map<String, DataIsolationInterface> INSTANCE_MAP = new HashMap<>();

    /**
     * 缓存当前表的数据隔离信息
     * key: 表名小写
     * valaue: 此表对应的权限隔离信息
     *
     * @author liutangqi
     * @date 2025/6/13 10:32
     * @Param
     **/
    private static final Map<String, DataIsolationDto> TABLE_ISOLATION_MAP = new HashMap<>();


    /**
     * 初始化
     *
     * @author liutangqi
     * @date 2025/6/13 10:33
     * @Param []
     **/
    public void init() {
        long startTime = System.currentTimeMillis();
        //1.配置校验
        if (CollectionUtils.isEmpty(fieldProperties.getScanEntityPackage())) {
            log.warn("【isolation】未配置扫描实体类路径，请检查配置");
            return;
        }

        //2.缓存表和@DataIsolation 的对应关系
        for (String scanEntityPackage : fieldProperties.getScanEntityPackage()) {
            doScanEntityPackage(scanEntityPackage);
        }

        //3.缓存当前的数据隔离配置
        ISOLATION_PROPERTIES_CACHE = fieldProperties.getIsolation();

        log.info("【isolation】初始化完毕，耗时:{}ms", (System.currentTimeMillis() - startTime));
    }

    /**
     * 扫描指定路径下标注了@TableName 的@DataIsolation 的类信息，维护表名和@DataIsolation 的对应关系
     *
     * @author liutangqi
     * @date 2025/6/13 10:40
     * @Param [scanEntityPackage]
     **/
    private void doScanEntityPackage(String scanEntityPackage) {
        //1.扫描指定路径下的的标注类
        Set<Class> entityClasses = ClassScanerUtil.scan(scanEntityPackage, TableName.class, DataIsolation.class)
                .stream()
                .filter(f -> f.getAnnotation(TableName.class) != null && f.getAnnotation(DataIsolation.class) != null)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(entityClasses)) {
            log.warn("【isolation】未找到标注了@TableName 的@DataIsolation 的类信息，请检查配置");
            return;
        }

        //2.处理表和对应表的数据隔离信息
        for (Class entityClass : entityClasses) {
            //2.1获取类上面的两个注解
            TableName tableName = (TableName) entityClass.getAnnotation(TableName.class);
            DataIsolation dataIsolation = (DataIsolation) entityClass.getAnnotation(DataIsolation.class);
            String lowerTableName = tableName.value().toLowerCase();
            //2.2按照优先级获取需要的信息（注解的值>全局配置）
            String isolationField = StringUtils.isNotBlank(dataIsolation.field()) ? dataIsolation.field() : this.fieldProperties.getIsolation().getField();
            IsolationRelationEnum isolationRelationEnum = !IsolationRelationEnum.EMPTY.equals(dataIsolation.relation()) ? dataIsolation.relation() : IsolationRelationEnum.getByCode(this.fieldProperties.getIsolation().getRelation());
            String isolationClass = !DataIsolationInterface.class.equals(dataIsolation.isolationClass()) ? dataIsolation.isolationClass().getName() : this.fieldProperties.getIsolation().getIsolationClass();
            //2.3校验用于数据隔离的表字段是否存在
            CollectionUtils.getValueIgnoreFloat(TableCache.getTableFieldMap(), lowerTableName).stream()
                    .filter(f -> StringUtils.equalIgnoreFieldSymbol(f, isolationField))
                    .findAny()
                    .orElseThrow(() -> new IsolationException(String.format("【isolation】数据隔离字段不存在 表:%s 字段:%s", lowerTableName, isolationField)));
            //2.4拼凑缓存结果
            TABLE_ISOLATION_MAP.put(lowerTableName, new DataIsolationDto(isolationField, isolationRelationEnum, isolationClass));
        }

        //3.实例化数据隔离方式的实例
        entityClasses.stream().map(m -> (DataIsolation) m.getAnnotation(DataIsolation.class))
                .distinct()
                .filter(f -> !DataIsolationInterface.class.equals(f.isolationClass()))
                .forEach(f -> {
                    try {
                        INSTANCE_MAP.put(f.isolationClass().getName(), f.isolationClass().newInstance());
                    } catch (InstantiationException | IllegalAccessException e) {
                        log.error("【isolation】实例化数据隔离方式失败，请确保DataIsolationInterface 实现类有无参构造", e);
                    }
                });

        //4.实例化全局配置的默认获取数据隔离值的实例
        try {
            Class<? extends DataIsolationInterface> defaultInterface = (Class<? extends DataIsolationInterface>) Class.forName(this.fieldProperties.getIsolation().getIsolationClass());
            INSTANCE_MAP.put(this.fieldProperties.getIsolation().getIsolationClass(), defaultInterface.newInstance());
        } catch (Exception e) {
            log.error("【isolation】实例化默认的获取隔离值失败，请检查配置");
            throw new IsolationException(String.format("默认的获取隔离值失败，当前配置：%s", this.fieldProperties.getIsolation().getIsolationClass()));
        }
    }


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
     * 根据表名获取当前表的数据隔离信息
     *
     * @author liutangqi
     * @date 2025/6/13 10:58
     * @Param [tableName]
     **/
    public static DataIsolationDto getIsolationData(String tableName) {
        //看这个表是否需要数据隔离
        return CollectionUtils.getValueIgnoreFloat(TABLE_ISOLATION_MAP, tableName.toLowerCase());
    }

    /**
     * 根据类全限定名，获取当前数据隔离实例
     *
     * @author liutangqi
     * @date 2025/6/13 16:03
     * @Param [isolationClass]
     **/
    public static DataIsolationInterface getIsolationInterface(String isolationClass) {
        return INSTANCE_MAP.get(isolationClass);
    }

    /**
     * 构建权限过滤的表达式
     *
     * @author liutangqi
     * @date 2025/6/13 13:10
     * @Param [isolationField：字段名, tableAlias: 字段所属表别名, isolationRelationEnum ：逻辑关系, isolationValue：数据隔离值]
     **/
    public static Expression buildIsolationExpression(String isolationField,
                                                      String tableAlias,
                                                      IsolationRelationEnum isolationRelationEnum,
                                                      String isolationValue) {
        Column column = new Column(isolationField);
        if (StringUtils.isNotBlank(tableAlias)) {
            column.setTable(new Table(tableAlias));
        }

        // field = value
        if (IsolationRelationEnum.EQUALS.equals(isolationRelationEnum)) {
            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(column);
            equalsTo.setRightExpression(new StringValue(isolationValue));
            return equalsTo;
        }

        //field lile 'value%'
        if (IsolationRelationEnum.LIKE_PREFIX.equals(isolationRelationEnum)) {
            LikeExpression likeExpression = new LikeExpression();
            likeExpression.setLeftExpression(column);
            likeExpression.setRightExpression(new StringValue(isolationValue + SymbolConstant.PER_CENT));
            return likeExpression;
        }

        throw new IsolationException(String.format("错误的数据隔离关系 %s", isolationRelationEnum));
    }
}
