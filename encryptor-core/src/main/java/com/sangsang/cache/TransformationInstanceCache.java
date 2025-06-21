package com.sangsang.cache;

import com.sangsang.config.properties.FieldProperties;
import com.sangsang.domain.constants.SymbolConstant;
import com.sangsang.domain.context.TransformationHolder;
import com.sangsang.domain.exception.TransformationException;
import com.sangsang.transformation.TransformationInterface;
import com.sangsang.util.ClassScanerUtil;
import com.sangsang.util.GenericityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 各类转换器实例缓存
 *
 * @author liutangqi
 * @date 2025/5/21 16:24
 */

public class TransformationInstanceCache implements BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(TransformationInstanceCache.class);
    /**
     * 存储当前转换器的实例
     * key:转换器的父类class (限定是属于Column 还是Function)
     * value:对应转换器实例
     */
    private static Map<Class, List<TransformationInterface>> transformationMap = null;
    /**
     * 实现了TransformationInterface接口的基类转换器的Class和泛型的对应关系
     * key:泛型Class
     * value:类的Class
     */
    private static final Map<Class, Class> superTransformationMap = new HashMap<>();


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
        return bean;
    }

    //---------------------------对外提供的方法分割线---------------------------

    /**
     * 初始化各种实例
     *
     * @author liutangqi
     * @date 2025/5/21 16:45
     * @Param [functionTransformationList, columnTransformationList, expressionTransformationList]
     **/
    public void init(FieldProperties fieldProperties) {
        //1.将TransformationInterface的包路径 + 当前转换类型作为包路径，扫描下面的所有的类
        String basePackage = TransformationInterface.class.getPackage().getName() + SymbolConstant.FULL_STOP + fieldProperties.getTransformation().getPatternType();

        //2.扫描当前配置模式包路径下所有类
        Set<Class> classes = ClassScanerUtil.scan(basePackage);
        if (CollectionUtils.isEmpty(classes)) {
            log.warn("【db-transformation】未扫描到转换器，请确保transformation.pattern配置的转换类型和存放转换器的包路径名一致");
        }

        //3.将对应的转换器子类实例化后存储到对应的缓存中
        transformationMap = classes.stream().collect(Collectors.groupingBy(Class::getSuperclass, Collectors.mapping(c -> (TransformationInterface) ReflectUtils.newInstance(c), Collectors.toList())));

        //4.找到各个转换器父类泛型和对应转换器父类class的对应关系
        classes.stream().map(Class::getSuperclass).distinct().forEach(f -> superTransformationMap.put(GenericityUtil.getInterfaceT(f, 0), f));
    }

    /**
     * 进行数据转换
     *
     * @author liutangqi
     * @date 2025/5/23 17:08
     * @Param [t： 必须和转换器的泛型类型一致]
     **/
    public static <T> T transformation(T t) {
        //1.先找转换器类型基类中泛型是这个的
        Class superClass = superTransformationMap.get(t.getClass());
        Optional.ofNullable(superClass).orElseThrow(() -> new TransformationException(String.format("找不到对应泛型<%s>的基类转换器", t.getClass().getSimpleName())));

        //2.找到这个基类转换器的所有实现类实例
        List<TransformationInterface> transformationList = transformationMap.get(superClass);

        //3.从所有实例中找符合处理条件的
        for (TransformationInterface transformationInterface : transformationList) {
            if (transformationInterface.needTransformation(t)) {
                //3.1 记录当前进行过语法转换
                TransformationHolder.transformationRecord();
                //3.2 返回转换后的结果
                return (T) transformationInterface.doTransformation(t);
            }
        }
        return null;
    }

    /**
     * 进行数据转换
     *
     * @author liutangqi
     * @date 2025/5/23 17:08
     * @Param [t： 不必转换器的泛型类型一致,typeClass:指定基类转换器泛型类型]
     **/
    public static <T> T transformation(T t, Class typeClass) {
        //1.先找转换器类型基类中泛型是这个的
        Class superClass = superTransformationMap.get(typeClass);
        Optional.ofNullable(superClass).orElseThrow(() -> new TransformationException(String.format("找不到对应泛型<%s>的基类转换器", typeClass.getSimpleName())));

        //2.找到这个基类转换器的所有实现类实例
        List<TransformationInterface> transformationList = transformationMap.get(superClass);

        //3.从所有实例中找符合处理条件的
        for (TransformationInterface transformationInterface : transformationList) {
            if (transformationInterface.needTransformation(t)) {
                //3.1 记录当前进行过语法转换
                TransformationHolder.transformationRecord();
                //3.2 返回转换后的结果
                return (T) transformationInterface.doTransformation(t);
            }
        }
        return null;
    }
}
