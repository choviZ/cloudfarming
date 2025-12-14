package com.vv.cloudfarming.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.user.dao.entity.UserDO;
import com.vv.cloudfarming.user.dto.req.*;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户相关服务接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 用户登录
     * @return 用户信息
     */
    UserRespDTO userLogin(UserLoginReqDTO requestParam);

    /**
     * 用户注册
     */
    boolean userRegister(UserRegisterReqDTO requestParam);

    /**
     * 获取当前登录用户信息
     * @param request http请求
     * @return 当前登录用户的信息
     */
    UserRespDTO getLoginUser(HttpServletRequest request);

    /**
     * 根据用户 ID 查询用户信息
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    UserRespDTO getUserById(Long id);

    /**
     * 创建用户
     *
     * @param requestParam 创建用户参数
     * @return 是否创建成功
     */
    boolean createUser(UserCreateReqDTO requestParam);

    /**
     * 修改用户
     *
     * @param requestParam 修改用户参数
     * @return 是否成功
     */
    boolean updateUser(UserUpdateReqDTO requestParam);

    /**
     * 删除用户
     *
     * @param id 用户 ID
     * @return 是否成功
     */
    Boolean deleteUserById(Long id);

    /**
     * 分页查询用户列表
     *
     * @param requestParam 查询参数
     * @return 用户列表
     */
    IPage<UserRespDTO> pageUser(UserPageQueryReqDTO requestParam);

    /**
     * 判断用户是否是管理员
     * @param userId 用户ID
     * @return 是否是管理员
     */
    boolean isAdmin(Long userId);
}
