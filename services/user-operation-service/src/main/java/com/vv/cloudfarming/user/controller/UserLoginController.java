package com.vv.cloudfarming.user.controller;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.user.dto.req.UserLoginReqDTO;
import com.vv.cloudfarming.user.dto.req.UserRegisterReqDTO;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import com.vv.cloudfarming.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户登录控制层
 */
@Tag(name = "用户登录控制层")
@RestController
@RequiredArgsConstructor
public class UserLoginController {

    private final UserService userService;

    @Operation(summary = "登录")
    @PostMapping("/api/user/login")
    public Result<UserRespDTO> userLogin(@RequestBody UserLoginReqDTO requestParam) {
        return Results.success(userService.userLogin(requestParam));
    }

    @Operation(summary = "注册")
    @PostMapping("/api/user/register")
    public Result<Boolean> userRegister(@RequestBody UserRegisterReqDTO requestParam) {
        return Results.success(userService.userRegister(requestParam));
    }

    @Operation(summary = "获取登录用户信息")
    @GetMapping("/api/user/get/login")
    public Result<UserRespDTO> getUser() {
        return Results.success(userService.getLoginUser());
    }

    @Operation(summary = "退出登录")
    @PostMapping("/api/user/logout")
    public Result<Boolean> userLogout() {
        userService.userLogout();
        return Results.success(true);
    }
}
