package com.sangsang.domain.annos;

import com.sangsang.domain.enums.PoJoAlgorithmEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * pojo模式下 sql的入参响应的实体类上标注此注解
 * 备注:当pojo模式下，某些sql语法不支持根据实体类上面的@FieldEncryptor 进行判断是否需要加解密处理时（例如：列运算函数处理），可以在响应的对应字段上面标注此注解
 *
 * @author liutangqi
 * @date 2024/10/8 18:00
 **/
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PoJoResultEncryptor {

    /**
     * POJO 模式下的加解密算法
     *
     * @author liutangqi
     * @date 2024/9/18 14:07
     * @Param []
     **/
    PoJoAlgorithmEnum pojoAlgorithm() default PoJoAlgorithmEnum.ALGORITHM_DEFAULT;
}
