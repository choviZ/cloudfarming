package com.vv.cloudfarming.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.constant.UserRoleConstant;
import com.vv.cloudfarming.user.dto.req.UserCreateReqDTO;
import com.vv.cloudfarming.user.dto.req.UserPageQueryReqDTO;
import com.vv.cloudfarming.user.dto.req.UserUpdateReqDTO;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import com.vv.cloudfarming.user.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService userService;

    /**
     * 根据id获取用户信息
     */
    @SaCheckLogin
    @GetMapping("/admin/v1/user/{id}")
    public Result<UserRespDTO> getUserById(@PathVariable("id") @NotNull Long id) {
        return Results.success(userService.getUserById(id));
    }

    /**
     * 创建用户
     */
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/admin/v1/user")
    public Result<Boolean> createUser(@RequestBody @Validated UserCreateReqDTO requestParam) {
        userService.createUser(requestParam);
        return Results.success(true);
    }

    /**
     * 修改用户信息
     */
    @SaCheckLogin
    @PutMapping("/admin/v1/user")
    public Result<Boolean> updateUser(@RequestBody @Validated UserUpdateReqDTO requestParam) {
        userService.updateUser(requestParam);
        return Results.success(true);
    }

    /**
     * 删除用户
     */
    @SaCheckLogin
    @DeleteMapping("/admin/v1/user/{id}")
    public Result<Boolean> deleteUser(@PathVariable("id") @NotNull Long id) {
        return Results.success(userService.deleteUserById(id));
    }

    /**
     * 分页查询用户列表
     */
    @SaCheckLogin
    @PostMapping("/admin/v1/user/page")
    public Result<IPage<UserRespDTO>> getUserPage(@RequestBody @Validated UserPageQueryReqDTO requestParam) {
        return Results.success(userService.pageUser(requestParam));
    }
}
