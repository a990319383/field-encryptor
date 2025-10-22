package com.sangsang.util;

import cn.hutool.core.collection.CollUtil;
import com.sangsang.domain.constants.SymbolConstant;

import java.util.*;

/**
 * @author liutangqi
 * @date 2024/9/9 15:53
 */
public class CollectionUtils {
    /**
     * 校验集合是否为空
     *
     * @param coll 入参
     * @return boolean
     */
    public static boolean isEmpty(Collection<?> coll) {
        return (coll == null || coll.isEmpty());
    }

    /**
     * 校验集合是否不为空
     *
     * @param coll 入参
     * @return boolean
     */
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    /**
     * 判断两个List是否相等
     *
     * @author liutangqi
     * @date 2025/3/4 18:06
     * @Param [listA, listB]
     **/
    public static boolean equals(List listA, List listB) {
        return listA.size() == listB.size() && CollUtil.containsAll(listA, listB);
    }

    /**
     * 判断两个Map是否相等（只判断一层）
     *
     * @author liutangqi
     * @date 2025/3/4 18:18
     * @Param [mapA, mapB]
     **/
    public static boolean equals(Map mapA, Map mapB) {
        Set keySetA = mapA.keySet();
        Set keySetB = mapB.keySet();
        if (keySetA.size() != keySetB.size()) {
            return false;
        }

        for (Object key : keySetA) {
            if (!Objects.equals(mapA.get(key), mapB.get(key))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 在原有的List中，每个间隔插入分隔符
     *
     * @author liutangqi
     * @date 2025/5/30 16:44
     * @Param [lists, separator]
     **/
    public static <T> List<T> join(List<T> lists, T separator) {
        List<T> res = new ArrayList<>();
        for (int i = 0; i < lists.size(); i++) {
            res.add(lists.get(i));
            if (i != lists.size() - 1) {
                res.add(separator);
            }
        }
        return res;
    }


    /**
     * 忽略 ` 和 " ，从map中获取值
     *
     * @author liutangqi
     * @date 2025/5/30 11:24
     * @Param [map, key]
     **/
    public static <T> T getValueIgnoreFloat(Map<String, T> map, String key) {
        T value = map.get(key);
        //1.找到了直接返回
        if (value != null) {
            return value;
        }

        //2.去除 ` 和 " 进行查询
        if (key.startsWith(SymbolConstant.FLOAT)) {
            return map.get(StringUtils.trim(key, SymbolConstant.FLOAT));
        }
        if (key.startsWith(SymbolConstant.DOUBLE_QUOTES)) {
            return map.get(StringUtils.trim(key, SymbolConstant.DOUBLE_QUOTES));
        }

        //3.按照添加` " 的方式去查询
        if (value == null) {
            value = map.get(SymbolConstant.FLOAT + key.trim() + SymbolConstant.FLOAT);
        }
        if (value == null) {
            value = map.get(SymbolConstant.DOUBLE_QUOTES + key.trim() + SymbolConstant.DOUBLE_QUOTES);
        }
        return value;
    }


    /**
     * 从Map中获取值，获取成功后，再将该值给移除掉
     *
     * @author liutangqi
     * @date 2025/7/18 10:58
     * @Param [map, key]
     **/
    public static <K, V> V getAndRemove(Map<K, V> map, K key) {
        //1.先获取值
        V res = map.get(key);

        //2.判断Map中是否包含此key，包含就移除（注意：这里不能判断上面get的值是否为null来作为移除依据，因为Map中可以存null值作为value）
        if (map.containsKey(key)) {
            map.remove(key);
        }
        return res;
    }


    /**
     * 忽略大小写，忽略` 和 " ，判断集合中中是否存在指定值
     *
     * @author liutangqi
     * @date 2025/8/25 14:28
     * @Param [set, key]
     **/
    public static boolean containsIgnoreFieldSymbol(Collection<String> coll, String key) {
        return coll.stream().filter(f -> StringUtils.equalIgnoreFieldSymbol(f, key))
                .findFirst()
                .orElse(null) != null;
    }
}
