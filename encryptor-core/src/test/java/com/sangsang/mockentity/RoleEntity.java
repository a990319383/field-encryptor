package com.sangsang.mockentity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sangsang.domain.annos.encryptor.FieldEncryptor;
import com.sangsang.encryptor.TestDBFieldEncryptorPattern;

/**
 * @author liutangqi
 * @date 2025/7/2 14:45
 */
@TableName("tb_role")
public class RoleEntity extends BaseEntity {

    @TableField("role_name")
    @FieldEncryptor(TestDBFieldEncryptorPattern.class)
    private String roleName;

    @TableField("role_desc")
    private String roleDesc;
}
