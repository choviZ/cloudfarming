package com.vv.cloudfarming.controller.user;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.user.dto.req.UserLoginReqDTO;
import com.vv.cloudfarming.user.dto.req.UserRegisterReqDTO;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import com.vv.cloudfarming.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制层
 */
@Tag(name = "用户操作")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 登录
     */
    @Operation(summary = "登录")
    @PostMapping("/v1/user/login")
    public Result<UserRespDTO> userLogin(@RequestBody UserLoginReqDTO requestParam) {
        return Results.success(userService.userLogin(requestParam));
    }

    /**
     * 注册
     */
    @Operation(summary = "注册")
    @PostMapping("/v1/user/register")
    public Result<Boolean> userRegister(@RequestBody UserRegisterReqDTO requestParam) {
        return Results.success(userService.userRegister(requestParam));
    }

    /**
     * 获取登录用户信息
     */
    @Operation(summary = "获取登录用户信息")
    @GetMapping("/v1/user")
    public Result<UserRespDTO> getUser() {
        return Results.success(userService.getLoginUser());
    }
}
