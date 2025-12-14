package com.vv.cloudfarming.controller.user;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.user.dto.req.UserLoginReqDTO;
import com.vv.cloudfarming.user.dto.req.UserRegisterReqDTO;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import com.vv.cloudfarming.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 登录
     */
    @PostMapping("/v1/user/login")
    public Result<UserRespDTO> userLogin(@RequestBody UserLoginReqDTO requestParam) {
        return Results.success(userService.userLogin(requestParam));
    }

    /**
     * 注册
     */
    @PostMapping("/v1/user/register")
    public Result<Boolean> userRegister(@RequestBody UserRegisterReqDTO requestParam) {
        return Results.success(userService.userRegister(requestParam));
    }
}
