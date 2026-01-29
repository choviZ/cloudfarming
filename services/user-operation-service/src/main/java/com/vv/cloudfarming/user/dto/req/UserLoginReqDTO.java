package com.vv.cloudfarming.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户登录请求参数对象
 */
@Data
public class UserLoginReqDTO {

    @NotBlank
    @Pattern(
            regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{6,18}$",
            message = "用户名需为6-18位，仅允许中文、英文、数字、下划线"
    )
    private String username;

    @NotBlank
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"|\\\\,.<>?]{6,20}$",
            message = "密码必须为6-20位，且包含字母和数字")
    private String password;
}
