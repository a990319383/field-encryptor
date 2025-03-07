package com.sangsang.util;

import cn.hutool.core.collection.CollUtil;

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
}
