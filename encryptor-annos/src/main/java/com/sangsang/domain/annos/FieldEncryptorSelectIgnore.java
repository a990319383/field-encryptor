package com.sangsang.domain.annos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于mapper上面
 * 标注了这个注解的mapper， select的字段不会进行解密处理
 * todo-ltq 后续规划功能，看后续场景是否有此必要，目前暂未实现
 *
 * @author liutangqi
 * @date 2024/2/1 13:40
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldEncryptorSelectIgnore {
}
