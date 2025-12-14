package com.vv.cloudfarming.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.enums.ReviewStatusEnum;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.user.dao.entity.FarmerDO;
import com.vv.cloudfarming.user.dao.entity.UserDO;
import com.vv.cloudfarming.user.dao.mapper.FarmerMapper;
import com.vv.cloudfarming.user.dto.req.FarmerApplyReqDO;
import com.vv.cloudfarming.user.dto.req.UpdateReviewStatusReqDTO;
import com.vv.cloudfarming.user.dto.resp.FarmerReviewRespDTO;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import com.vv.cloudfarming.user.service.FarmerService;
import com.vv.cloudfarming.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 农户服务接口实现层
 */
@Service
@RequiredArgsConstructor
public class FarmerServiceImpl extends ServiceImpl<FarmerMapper, FarmerDO> implements FarmerService {

    private final UserService userService;

    @Override
    public void submitApply(FarmerApplyReqDO requestParam, HttpServletRequest request) {
        // TODO 参数校验
        UserRespDTO loginUser = userService.getLoginUser(request);
        // 保存审核记录
        FarmerDO farmerDO = BeanUtil.toBean(requestParam, FarmerDO.class);
        farmerDO.setUserId(loginUser.getId());
        farmerDO.setReviewStatus(ReviewStatusEnum.PENDING.getStatus());
        int inserted = baseMapper.insert(farmerDO);
        if (inserted < 1){
            throw new ServiceException("申请失败");
        }
    }

    @Override
    public FarmerReviewRespDTO getReviewStatus(HttpServletRequest request) {
        // 检查登录态
        UserRespDTO loginUser = userService.getLoginUser(request);
        if (loginUser == null || loginUser.getId() == null) {
            throw new ClientException("请先登录");
        }
        LambdaQueryWrapper<FarmerDO> wrapper = Wrappers.lambdaQuery(FarmerDO.class)
                .eq(FarmerDO::getUserId, loginUser.getId());
        FarmerDO farmerDO = baseMapper.selectOne(wrapper);
        if (farmerDO == null) {
            throw new ClientException("您未申请过成为入住农户");
        }
        ReviewStatusEnum statusEnum = ReviewStatusEnum.getByStatus(farmerDO.getReviewStatus());
        return FarmerReviewRespDTO.builder()
                .status(statusEnum.getDesc())
                .reviewRemark(farmerDO.getReviewRemark())
                .build();
    }

    @Override
    public void updateReviewState(UpdateReviewStatusReqDTO requestParam, HttpServletRequest request) {
        String remark = requestParam.getRemark();
        ReviewStatusEnum statusEnum = ReviewStatusEnum.getByDesc(requestParam.getStatus());
        if (statusEnum == null){
            throw new ClientException("不存在的状态");
        }
        UserRespDTO loginUser = userService.getLoginUser(request);
        if (loginUser == null){
            throw new ClientException("请先登录");
        }
        boolean admin = userService.isAdmin(loginUser.getId());
        if (!admin){
            throw new ClientException("无权修改状态");
        }
        // 检查是否申请成为农户
        FarmerDO farmerDO = baseMapper.selectById(requestParam.getId());
        if (farmerDO == null){
            throw new ClientException("请先申请成为农户");
        }
        // 更新审核记录
        LambdaUpdateWrapper<FarmerDO> wrapper = Wrappers.lambdaUpdate(FarmerDO.class)
                .eq(FarmerDO::getId, requestParam.getId())
                .set(FarmerDO::getReviewStatus, statusEnum.getStatus())
                .set(FarmerDO::getReviewUserId, loginUser.getId())
                .set(StrUtil.isNotBlank(remark), FarmerDO::getReviewRemark, remark);
        int updated = baseMapper.update(wrapper);
        // 更新用户角色
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getId, farmerDO.getUserId())
                .set(UserDO::getUserType, 1);
        boolean updatedUserType = userService.update(updateWrapper);
        if (updated < 1 || updatedUserType){
            throw new ServiceException("更新状态失败，请稍后重试");
        }
    }
}
