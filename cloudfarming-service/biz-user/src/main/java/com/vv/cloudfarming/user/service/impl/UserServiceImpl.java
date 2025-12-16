package com.vv.cloudfarming.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.errorcode.BaseErrorCode;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.user.dao.entity.UserDO;
import com.vv.cloudfarming.user.dao.mapper.UserMapper;
import com.vv.cloudfarming.user.dto.req.*;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import com.vv.cloudfarming.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * 用户相关服务接口实现层
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Override
    public UserRespDTO userLogin(UserLoginReqDTO requestParam) {
        String username = requestParam.getUsername();
        String password = requestParam.getPassword();
        LambdaQueryWrapper<UserDO> wrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username)
                .eq(UserDO::getPassword, password);
        UserDO user = baseMapper.selectOne(wrapper);
        if (user == null) {
            throw new ClientException(BaseErrorCode.USERNAME_PASSWORD_ERROR);
        }
        if (user.getStatus() == 1) {
            throw new ClientException(BaseErrorCode.ACCOUNT_STATUS_ERROR);
        }
        StpUtil.login(user.getId());
        return BeanUtil.toBean(user, UserRespDTO.class);
    }

    @Override
    public boolean userRegister(UserRegisterReqDTO requestParam) {
        String username = requestParam.getUsername();
        LambdaQueryWrapper<UserDO> wrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        Long count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            throw new ClientException(BaseErrorCode.USER_NAME_EXIST_ERROR);
        }
        String password = requestParam.getPassword();
        String checkPassword = requestParam.getCheckPassword();
        if (!password.equals(checkPassword)) {
            throw new ClientException(BaseErrorCode.PASSWORD_NOT_CONSISTENT);
        }
        int inserted = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
        if (inserted < 1) {
            throw new ClientException(BaseErrorCode.USER_REGISTER_ERROR);
        }
        return true;
    }

    @Override
    public UserRespDTO getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute("USER_STATE");
        UserRespDTO userRespDTO = BeanUtil.toBean(((UserDO) userObj), UserRespDTO.class);
        if (userRespDTO == null){
            throw new ClientException("未获取到登录信息");
        }
        return userRespDTO;
    }

    @Override
    public UserRespDTO getLoginUser() {
        long loginId = StpUtil.getLoginIdAsLong();
        UserDO userDO = baseMapper.selectById(loginId);
        if (userDO == null) {
            throw new ClientException("请先登录");
        }
        return BeanUtil.toBean(userDO, UserRespDTO.class);
    }

    @Override
    public UserRespDTO getUserById(Long id) {
        if (ObjectUtil.isNull(id) || id <= 0) {
            throw new ClientException("用户ID格式错误");
        }
        UserDO userDO = baseMapper.selectById(id);
        return BeanUtil.toBean(userDO, UserRespDTO.class);
    }

    @Override
    public boolean createUser(UserCreateReqDTO requestParam) {
        UserDO userDO = BeanUtil.toBean(requestParam, UserDO.class);
        int inserted = baseMapper.insert(userDO);
        if (inserted == 0) {
            throw new ServiceException("新增用户失败");
        }
        return inserted > 0;
    }

    @Override
    public boolean updateUser(UserUpdateReqDTO requestParam) {
        LambdaQueryWrapper<UserDO> wrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getId, requestParam.getId());
        boolean exists = baseMapper.exists(wrapper);
        if (!exists) {
            throw new ClientException("用户不存在");
        }
        UserDO userDO = BeanUtil.toBean(requestParam, UserDO.class);
        int updated = baseMapper.updateById(userDO);
        if (updated == 0) {
            throw new ServiceException("更新用户失败");
        }
        return updated > 0;
    }

    @Override
    public Boolean deleteUserById(Long id) {
        LambdaQueryWrapper<UserDO> wrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getId, id);
        boolean exists = baseMapper.exists(wrapper);
        if (!exists) {
            throw new ClientException("用户不存在");
        }
        int deleted = baseMapper.deleteById(id);
        if (deleted == 0) {
            throw new ServiceException("删除用户失败");
        }
        return deleted > 0;
    }

    @Override
    public IPage<UserRespDTO> pageUser(UserPageQueryReqDTO requestParam) {
        String username = requestParam.getUsername();
        Integer userType = requestParam.getUserType();
        Integer status = requestParam.getStatus();
        LambdaQueryWrapper<UserDO> wrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(StrUtil.isNotBlank(username), UserDO::getUsername, username)
                .eq(ObjectUtil.isNotNull(userType), UserDO::getUserType, userType)
                .eq(ObjectUtil.isNotNull(status), UserDO::getStatus, status);
        IPage<UserDO> page = baseMapper.selectPage(requestParam, wrapper);
        return page.convert(each -> BeanUtil.toBean(each, UserRespDTO.class));
    }

    @Override
    public boolean isAdmin(Long userId) {
        UserDO userDO = baseMapper.selectById(userId);
        return userDO.getUserType() == 2;
    }

    @Override
    public void userLogout() {
        StpUtil.logout();
    }
}
