package com.sangsang.domain.context;

import com.sangsang.domain.annos.isolation.IsolationForbid;

import java.util.ArrayDeque;

/**
 * @author liutangqi
 * @date 2025/6/16 8:51
 */
public class IsolationHolder {
    /**
     * 存储当前方法是否禁止了数据隔离的标识
     * 这里使用队列，存储标识，就可以支持嵌套
     **/
    private static final InheritableThreadLocal<ArrayDeque<IsolationForbid>> ISOLATION_HOLDER = new InheritableThreadLocal<>();


    /**
     * 设置当前禁止数据隔离的标识
     *
     * @author liutangqi
     * @date 2025/6/16 9:06
     * @Param [forbidIsolation]
     **/
    public static void setForbidIsolation(IsolationForbid isolationForbid) {
        ArrayDeque<IsolationForbid> isolationForbids = ISOLATION_HOLDER.get();
        if (isolationForbids == null) {
            isolationForbids = new ArrayDeque<>();
            ISOLATION_HOLDER.set(isolationForbids);
        }
        //从头部添加
        isolationForbids.addFirst(isolationForbid);
    }

    /**
     * 解除当前层禁止数据隔离标识
     *
     * @author liutangqi
     * @date 2025/6/16 9:06
     * @Param []
     **/
    public static void removeForbidIsolation() {
        ArrayDeque<IsolationForbid> isolationForbids = ISOLATION_HOLDER.get();
        if (isolationForbids != null) {
            //尾部移除
            isolationForbids.removeLast();
            //如果移除完毕了，就整个清除了
            if (isolationForbids.isEmpty()) {
                ISOLATION_HOLDER.remove();
            }
        }
    }

    /**
     * 获取当前方法是否禁止数据隔离
     *
     * @author liutangqi
     * @date 2025/6/16 17:48
     * @Param []
     **/
    public static IsolationForbid getForbidIsolation() {
        ArrayDeque<IsolationForbid> isolationForbids = ISOLATION_HOLDER.get();
        if (isolationForbids == null) {
            return null;
        }
        //头部获取，头部是最新的
        return isolationForbids.getFirst();
    }

}
