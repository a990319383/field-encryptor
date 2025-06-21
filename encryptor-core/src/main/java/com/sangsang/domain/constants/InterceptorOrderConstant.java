package com.sangsang.domain.constants;

import com.sangsang.interceptor.*;

import java.util.Arrays;
import java.util.List;

/**
 * 当前项目的拦截器的顺序
 *
 * @author liutangqi
 * @date 2025/5/26 16:54
 */
public interface InterceptorOrderConstant {
    List<Class> SEQUENCE = Arrays.asList(
            //sql语法转换最先执行
            TransformationInterceptor.class,

            //字段加解密中间执行
            DBFieldEncryptorInterceptor.class,
            PoJoParamEncrtptorInterceptor.class,
            PoJoResultEncrtptorInterceptor.class,

            //字段脱敏最后执行
            FieldDesensitizeInterceptor.class
    );


    /**
     * sql语法转换的顺序
     * 最先执行
     */
    int TRANSFORMATION = 0;

    /**
     * 未指定order的顺序
     */
    int NORMAL = 50;

    /**
     * 字段加解密的顺序
     * 中间执行
     **/
    int ENCRYPTOR = 50;
    /**
     * 字段脱敏的顺序
     * 必须最后执行
     */
    int DESENSITIZE = 100;
}
