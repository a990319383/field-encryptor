package com.sangsang.domain.strategy;


import com.sangsang.domain.annos.DefaultStrategy;
import com.sangsang.domain.enums.IsolationRelationEnum;
import com.sangsang.domain.exception.IsolationException;
import com.sangsang.domain.strategy.encryptor.DBFieldEncryptorStrategy;
import com.sangsang.domain.strategy.encryptor.FieldEncryptorStrategy;
import com.sangsang.domain.strategy.encryptor.PoJoFieldEncryptorStrategy;
import com.sangsang.domain.strategy.isolation.IsolationDataStrategy;
import net.sf.jsqlparser.expression.Expression;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认策略的基类
 * 1.spring的bean中如果只有一个T的子类，那这个类就是默认实现类
 * 2.如果有多个子类，那么标注了@DefaultStrategy的类就是默认实现类
 *
 * @author liutangqi
 * @date 2025/6/23 18:17
 */
public class DefaultStrategyBase<T> {

    protected T defaultStrategyInstance;

    /**
     * 默认的实现策略，当前spring容器中，有一个就是默认的，有多个的话，标注了@DefaultStrategy的是默认的
     *
     * @author liutangqi
     * @date 2025/6/23 18:19
     * @Param [strategies]
     **/
    public DefaultStrategyBase(List<T> strategies) {
        //1.当前项目未找到实现类，抛出异常
        if (strategies == null || strategies.size() == 0) {
            throw new IsolationException("当前项目未配置策略，请实现对应策略接口并放在spring容器中");
        }
        //2.当前项目只配置了一种策略，则这个策略就是默认策略
        if (strategies.size() == 1) {
            defaultStrategyInstance = strategies.get(0);
            return;
        }
        //3.当前项目配置了多种策略，则标注了@DefaultStrategy是默认策略
        List<T> defaultStrategyList = strategies.stream().filter(item -> item.getClass().isAnnotationPresent(DefaultStrategy.class)).collect(Collectors.toList());
        if (defaultStrategyList.size() == 0) {
            throw new IsolationException("当前项目存在多种策略，请将默认的策略标注@DefaultStrategy");
        }
        if (defaultStrategyList.size() > 1) {
            throw new IsolationException("当前项目存在多种策略，且多个不同类型策略标注了@DefaultStrategy，只能选择其中一个作为默认");
        }
        defaultStrategyInstance = defaultStrategyList.get(0);
    }

    /**
     * 数据隔离默认加载策略
     *
     * @author liutangqi
     * @date 2025/6/24 10:22
     * @Param
     **/
    public static class IsolationBeanStrategy extends DefaultStrategyBase<IsolationDataStrategy> implements IsolationDataStrategy {

        /**
         * 默认的实现策略，当前spring容器中，有一个就是默认的，有多个的话，标注了@DefaultStrategy的是默认的
         *
         * @param strategies
         * @author liutangqi
         * @date 2025/6/23 18:19
         * @Param [strategies]
         */
        public IsolationBeanStrategy(List<IsolationDataStrategy> strategies) {
            super(strategies);
        }

        @Override
        public String getIsolationField() {
            return defaultStrategyInstance.getIsolationField();
        }

        @Override
        public IsolationRelationEnum getIsolationRelation() {
            return defaultStrategyInstance.getIsolationRelation();
        }

        @Override
        public Object getIsolationData() {
            return defaultStrategyInstance.getIsolationData();
        }
    }


    /**
     * 加解密默认加载策略
     * 其中泛型T 是对数据的加解密类型，目前 pojo模式T是String db模式T是Expression
     *
     * @author liutangqi
     * @date 2025/6/30 17:51
     * @Param
     **/
    public static class EncryptorBeanStrategy<T> extends DefaultStrategyBase<FieldEncryptorStrategy<T>> implements FieldEncryptorStrategy<T> {
        public EncryptorBeanStrategy(List<FieldEncryptorStrategy<T>> strategies) {
            super(strategies);
        }

        @Override
        public T encryption(T oldExpression) {
            return defaultStrategyInstance.encryption(oldExpression);
        }

        @Override
        public T decryption(T oldExpression) {
            return defaultStrategyInstance.decryption(oldExpression);
        }
    }
}
