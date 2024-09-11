package com.sangsang.mockentity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

/**
 * 系统用户表
 *
 * @author hgwlpt
 */
@TableName("sys_user")
public class SysUserEntity {

    /**
     * 主键
     */
    private Long id;

    /**
     * 登录名
     */
    private String loginName;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户名
     */
    private String name;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    private Long createDate;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    private Long updateDate;

    /**
     * 假删除(默认:1)
     */
    private Boolean enableFlag;

    /**
     * 最后登录时间
     */
    private Long lastLoginDate;

    /**
     * 职位
     */
    private String post;

    private Integer areaCode;

    /**
     * 所属单位id
     */
    private Long unitId;

    /**
     * 执勤状态,0:未执勤,1:执勤中
     */
    private Boolean workStatus;

    /**
     * 有效期
     */
    private Long userValidity;

    /**
     * 国密3密码
     */
    private String sm3Password;

}