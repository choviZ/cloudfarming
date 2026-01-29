package com.vv.cloudfarming.user.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.vv.cloudfarming.common.enums.UserRoleEnum;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import com.vv.cloudfarming.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * sa-token 自定义权限加载接口实现类
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserService userService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> list = new ArrayList<String>();
        return list;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        UserRespDTO user = userService.getUserById(Long.valueOf(loginId.toString()));
        return List.of(UserRoleEnum.fromCode(user.getUserType()).getDescription());
    }
}
