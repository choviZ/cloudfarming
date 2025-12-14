package com.vv.cloudfarming.user.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息对象
 */
@Data
public class UserRespDTO implements Serializable {

    /**
     * 主键
     */
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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新
     */
    private Date updateTime;
}