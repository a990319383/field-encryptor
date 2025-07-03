package com.sangsang.util;

import com.sangsang.domain.annos.FieldInterceptorOrder;
import com.sangsang.domain.constants.InterceptorOrderConstant;
import com.sangsang.domain.constants.SymbolConstant;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.InterceptorChain;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 拦截器相关的工具类
 *
 * @author liutangqi
 * @date 2025/5/26 16:41
 */
public class InterceptorUtil {

    /**
     * 将当前注册的拦截器进行排序
     *
     * @author liutangqi
     * @date 2025/5/26 17:01
     * @Param [configuration]
     **/
    public static void sort(Configuration configuration) {
        //1.先通过反射获取当前的所有拦截器
        InterceptorChain interceptorChain = (InterceptorChain) ReflectUtils.getFieldValue(configuration, "interceptorChain");
        List<Interceptor> interceptors = (List<Interceptor>) ReflectUtils.getFieldValue(interceptorChain, "interceptors");

        //2.将当前标注了@FieldInterceptorOrder 的拦截器和没有标注的分开
        List<Interceptor> selftInterceptor = interceptors.stream().filter(f -> f.getClass().isAnnotationPresent(FieldInterceptorOrder.class)).collect(Collectors.toList());
        List<Interceptor> otherInterceptor = interceptors.stream().filter(f -> !f.getClass().isAnnotationPresent(FieldInterceptorOrder.class)).collect(Collectors.toList());

        //3.将自己注册的拦截器根据@FieldInterceptorOrder 进行排序
        selftInterceptor.sort((o1, o2) ->
                Optional.ofNullable(o1.getClass().getAnnotation(FieldInterceptorOrder.class)).map(FieldInterceptorOrder::value).orElse(InterceptorOrderConstant.NORMAL)
                        - Optional.ofNullable(o2.getClass().getAnnotation(FieldInterceptorOrder.class)).map(FieldInterceptorOrder::value).orElse(InterceptorOrderConstant.NORMAL)
        );

        //4.将排序后的拦截器重新赋值给拦截器链
        interceptors.clear();
        interceptors.addAll(selftInterceptor);
        interceptors.addAll(otherInterceptor);
    }

    /**
     * 判断当前sql的mapper上面是否标注了注解T
     *
     * @author liutangqi
     * @date 2025/6/13 18:29
     * @Param [statementHandler, t]
     **/
    public static <T extends Annotation> T getMapperAnnotation(StatementHandler statementHandler, Class<? extends T> t) throws Exception {
        //类全限定名.方法名
        String nameSpace = getNameSpace(statementHandler);

        //以最后一个.为界，获取到类全限定名和方法名
        int lastDotIndex = nameSpace.lastIndexOf(SymbolConstant.FULL_STOP);
        String classFullyName = nameSpace.substring(0, lastDotIndex);
        String methodName = nameSpace.substring(lastDotIndex + 1);

        //反射判断是否存在此注解
        return Stream.of(Class.forName(classFullyName).getDeclaredMethods())
                .filter(f -> f.getName().equals(methodName))
                .findFirst()
                .map(m -> m.getAnnotation(t))
                .orElse(null);
    }


    /**
     * 获取当前sql的namespace
     *
     * @author liutangqi
     * @date 2025/5/21 10:41
     * @Param [statementHandler]
     **/
    public static String getNameSpace(StatementHandler statementHandler) throws Exception {
        //处理代理的情况
        StatementHandler sh = unwrapProxy(statementHandler);

        //开始获取namespace
        MetaObject metaObject = MetaObject.forObject(sh, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        String id = mappedStatement.getId();
        return id;
    }


    /**
     * 动态代理溯源（JDK 动态代理或 CGLIB）
     */
    private static StatementHandler unwrapProxy(StatementHandler proxy) throws Exception {
        Object current = proxy;
        // 循环溯源，直到找到非代理对象
        while (current instanceof Proxy) {
            // JDK 动态代理：通过 InvocationHandler 获取目标对象
            InvocationHandler handler = Proxy.getInvocationHandler(current);
            // MyBatis 的 Plugin 代理
            if (handler instanceof Plugin) {
                Field targetField = handler.getClass().getDeclaredField("target");
                targetField.setAccessible(true);
                current = targetField.get(handler);
            }
            // 非 MyBatis 标准代理，无法溯源
            else {
                break;
            }
        }
        return (StatementHandler) current;
    }
}
