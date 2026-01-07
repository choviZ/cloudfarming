package com.vv.cloudfarming.user.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 用户分页查询请求参数对象
 */
@Data
public class UserPageQueryReqDTO extends Page {

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

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