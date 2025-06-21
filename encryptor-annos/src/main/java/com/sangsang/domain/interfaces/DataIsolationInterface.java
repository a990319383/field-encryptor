package com.sangsang.domain.interfaces;

/**
 * 注意：实现类必须提供无参构造方法
 *
 * @author liutangqi
 * @date 2025/6/12 16:09
 */
public interface DataIsolationInterface {

    /**
     * 获取当前数据隔离的具体值
     * 栗子：按照当前登录用户的所属组织id来进行隔离的话，这里返回的就是当前登录用户的所属组织id
     *
     * @author liutangqi
     * @date 2025/6/13 10:12
     * @Param []
     **/
    String getIsolationData();

}
