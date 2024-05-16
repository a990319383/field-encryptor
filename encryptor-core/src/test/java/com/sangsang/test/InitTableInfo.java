package com.sangsang.test;

import cn.hutool.core.map.MapUtil;
import com.sangsang.cache.TableCache;
import com.sangsang.domain.annos.FieldEncryptor;
import com.sangsang.mockentity.UserEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        //mock (key:小写表名  value: key:字段名 value:上面标注的@FieldEncryptor 注解)
        Map<String, Map<String, FieldEncryptor>> mockMap = new HashMap<>();
        FieldEncryptor fieldEncryptor = UserEntity.class.getDeclaredField("phone").getAnnotation(FieldEncryptor.class);
        Map<String, FieldEncryptor> user = MapUtil.<String, FieldEncryptor>builder()
                .put("id", null)
                .put("user_name", null)
                .put("login_name", null)
                .put("login_pwd", null)
                .put("phone", fieldEncryptor)
                .put("role_id", null)
                .put("create_time", null)
                .put("update_time", null)
                .build();
        Map<String, FieldEncryptor> menu = MapUtil.<String, FieldEncryptor>builder()
                .put("id", null)
                .put("menu_name", null)
                .put("parent_id", null)
                .put("path", fieldEncryptor)
                .put("create_time", null)
                .put("update_time", null)
                .build();

        mockMap.put("tb_user", user);
        mockMap.put("tb_menu", menu);

        //将mock的值赋值给TableCache
        Map<String, Map<String, FieldEncryptor>> tableFieldEncryptInfo = TableCache.getTableFieldEncryptInfo();
        tableFieldEncryptInfo.put("tb_user", user);
        tableFieldEncryptInfo.put("tb_menu", menu);

        Set<String> fieldEncryptTable = TableCache.getFieldEncryptTable();
        fieldEncryptTable.add("tb_user");

        Map<String, Set<String>> tableFieldMap = TableCache.getTableFieldMap();
        for (Map.Entry<String, Map<String, FieldEncryptor>> mockEntry : mockMap.entrySet()) {
            Set<String> fields = mockEntry.getValue().keySet();
            tableFieldMap.put(mockEntry.getKey(), fields);
        }

    }
}