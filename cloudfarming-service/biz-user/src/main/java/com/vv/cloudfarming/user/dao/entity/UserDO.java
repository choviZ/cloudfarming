package com.vv.cloudfarming.user.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.cloudfarming.common.database.BaseDO;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户实体
 */
@TableName("t_users")
@Data
public class UserDO extends BaseDO implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 用户类型： 0-普通用户 1-农户 2-系统管理员
     */
    private Integer userType;

    /**
     * 账号状态：0-正常 1-禁用
     */
    private Integer status;
}