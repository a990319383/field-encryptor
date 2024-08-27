package com.sangsang.test;

import com.sangsang.cache.TableCache;
import com.sangsang.domain.dto.TableInfoDto;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * @author liutangqi
 * @date 2024/4/2 15:58
 */
public class InitTableInfo {

    /**
     * 将TableCache的数据做个mock
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


    @Test
    public void bakSql() {
        String scanEntityPackage = "com.sangsang.mockentity";
        String suffix = "20240827";
//        BakSqlCreater.bakSql(scanEntityPackage, suffix);

    }
}