package com.vv.cloudfarming.user.dto.req;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改用户参数对象
 */
@Data
public class UserUpdateReqDTO implements Serializable {

    /**
     * id
     */
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"|\\\\,.<>?]{6,20}$",
             message = "密码必须为6-20位，且包含字母和数字")
    private String password;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 用户类型： 0-普通用户 1-农户 2-系统管理员
     */
    @Min(value = 0, message = "用户类型错误")
    @Max(value = 2, message = "用户类型错误")
    private Integer userType;

    /**
     * 账号状态：0-正常 1-禁用
     */
    @Min(value = 0, message = "状态码错误")
    @Max(value = 1, message = "状态码错误")
    private Integer status;
}