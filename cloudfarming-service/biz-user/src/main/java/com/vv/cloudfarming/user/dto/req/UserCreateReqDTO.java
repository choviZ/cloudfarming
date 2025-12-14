package com.vv.cloudfarming.user.dto.req;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建用户参数对象
 */
@Data
public class UserCreateReqDTO implements Serializable {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Pattern(
            regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{6,18}$",
            message = "用户名需为6-18位，仅允许中文、英文、数字、下划线"
    )
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"|\\\\,.<>?]{6,20}$", 
             message = "密码必须为6-20位，且包含字母和数字")
    private String password;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 用户类型： 0-普通用户 1-农户 2-系统管理员
     */
    @NotNull(message = "用户类型不能为空")
    @Min(value = 0, message = "用户类型错误")
    @Max(value = 2, message = "用户类型错误")
    private Integer userType;
}