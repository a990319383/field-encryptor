package com.sangsang.util;

import com.sangsang.domain.interfaces.DesensitizeInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liutangqi
 * @date 2025/4/12 23:14
 */
public class DesensitizeUtil {

    //缓存脱敏对象，避免重复创建大量对象
    private final static Map<Class, DesensitizeInterface> instanceMap = new HashMap<>();

    /**
     * 使用指定脱敏方法对字符串进行脱敏
     *
     * @author liutangqi
     * @date 2025/4/12 23:20
     * @Param [dClass, cleartext, t]
     **/
    public static final <T> String desensitize(Class<? extends DesensitizeInterface> dClass,
                                               String cleartext,
                                               T t) throws IllegalAccessException, InstantiationException {
        //1.避免重复反射创建对象，这里缓存脱敏实现方法对象
        DesensitizeInterface desensitizeInstance = instanceMap.get(dClass);
        if (desensitizeInstance == null) {
            desensitizeInstance = dClass.newInstance();
            instanceMap.put(dClass, desensitizeInstance);
        }

        //2.正式开始脱敏
        return desensitizeInstance.desensitize(cleartext, t);
    }
}
