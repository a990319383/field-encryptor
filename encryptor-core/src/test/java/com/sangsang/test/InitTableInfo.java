package com.sangsang.test;

import com.sangsang.cache.TableCache;
import com.sangsang.domain.dto.TableInfoDto;

import java.util.*;

/**
 * @author liutangqi
 * @date 2024/4/2 15:58
 */
public class InitTableInfo {

    /**
     * 将TableCache的数据做个mock
     * 实体类是 com.sangsang.mockentity.MenuEntity  com.sangsang.mockentity.UserEntity
     * 其中，只有tb_user表的phone字段需要进行加密
     *
     * @author liutangqi
     * @date 2024/4/2 15:58
     * @Param []
     **/
    public static void initTable() throws NoSuchFieldException {
        TableCache tableCache = new TableCache();
        //扫描这个路径下的实体类
        List<TableInfoDto> tableInfoDtos = tableCache.parseTableInfoByScanEntityPackage("com.sangsang.mockentity");
        //将这些实体类信息填充到本地缓存中
        tableCache.fillCacheMap(tableInfoDtos);
    }
}