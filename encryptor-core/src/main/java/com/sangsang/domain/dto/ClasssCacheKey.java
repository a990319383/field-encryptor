package com.sangsang.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.ClassUtils;


/**
 * Class作为缓存key时，由于一些代理对象和类加载不同，可能导致存取值错误，所以，需要使用Class作为缓存key时，使用这个包一层
 *
 * @author liutangqi
 * @date 2025/8/12 9:33
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClasssCacheKey {
    /**
     * 非代理类的类名
     */
    private String className;
    /**
     * 类加载器
     * 暂时不管类加载器，只要类全限定名一致，就判定为同一个类，部分本地热部署等会导致类加载器不同
     */
//    private ClassLoader classLoader;


    /**
     * 构建缓存key
     *
     * @author liutangqi
     * @date 2025/8/12 9:35
     * @Param [clazz]
     **/
    public static ClasssCacheKey buildKey(Class clazz) {
        //1.如果是代理类的话，获取真实key
        Class<?> originalClass = ClassUtils.getUserClass(clazz);

        //2.构建返回对象
        return ClasssCacheKey.builder()
                .className(originalClass.getName())
//                .classLoader(clazz.getClassLoader())
                .build();
    }

}
