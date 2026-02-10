package com.vv.cloudfarming.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.user.dto.req.UserCreateReqDTO;
import com.vv.cloudfarming.user.dto.req.UserPageQueryReqDTO;
import com.vv.cloudfarming.user.dto.req.UserUpdateReqDTO;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import com.vv.cloudfarming.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户信息控制层")
@RestController
@RequiredArgsConstructor
public class UserInfoController {

    private final UserService userService;

    @Operation(summary = "根据id获取用户信息")
    @SaCheckLogin
    @GetMapping("/api/user/get")
    public Result<UserRespDTO> getUserById(@RequestParam("id") @NotNull Long id) {
        return Results.success(userService.getUserById(id));
    }

    @Operation(summary = "创建用户")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/user/create")
    public Result<Boolean> createUser(@RequestBody @Validated UserCreateReqDTO requestParam) {
        userService.createUser(requestParam);
        return Results.success(true);
    }

    @Operation(summary = "修改用户信息")
    @SaCheckLogin
    @PostMapping("/api/user/update")
    public Result<Boolean> updateUser(@RequestBody @Validated UserUpdateReqDTO requestParam) {
        userService.updateUser(requestParam);
        return Results.success(true);
    }

    @Operation(summary = "根据id删除用户")
    @SaCheckLogin
    @PostMapping("/api/user/delete")
    public Result<Boolean> deleteUser(@RequestBody @NotNull Long id) {
        return Results.success(userService.deleteUserById(id));
    }

    @Operation(summary = "获取用户分页列表")
    @SaCheckLogin
    @PostMapping("/api/user/page")
    public Result<IPage<UserRespDTO>> getUserPage(@RequestBody @Validated UserPageQueryReqDTO requestParam) {
        return Results.success(userService.pageUser(requestParam));
    }
}
