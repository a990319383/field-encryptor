package com.sangsang.domain.constants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author liutangqi
 * @date 2024/3/15 11:27
 */
public interface DecryptConstant {
    /**
     * 解析sql的字段时，如果function处理了字段，处理后这个字段别名放到这个名字的虚拟别名表的结果中
     */
    String FUNCTION_TMP = "function_tmp";

    /**
     * 对于？ 占位符的替换，后面拼接自增序号，从0开始
     */
    String PLACEHOLDER = "encryptor_placeholder_";

    /**
     * 对于某些情况下，需要重新修改入参parameterMappings的变量名时的统一前缀
     **/
    String NEW_PARAM_PLACEHOLDER = "new_param_placeholder_";

    /**
     * 基本数据类型对应的包装类 + 字符串类型 + 时间类型 的集合
     */
    List<Class> FUNDAMENTAL = Arrays.asList(Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            String.class,
            LocalDateTime.class,
            LocalDate.class,
            Date.class
    );

}
