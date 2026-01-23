package com.vv.cloudfarming.shop.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.shop.dao.entity.AdoptInstanceDO;
import com.vv.cloudfarming.shop.dao.mapper.AdoptInstanceMapper;
import com.vv.cloudfarming.shop.dto.req.AdoptInstancePageReqDTO;
import com.vv.cloudfarming.shop.dto.resp.AdoptInstanceRespDTO;
import com.vv.cloudfarming.shop.service.AdoptInstanceService;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import com.vv.cloudfarming.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdoptInstanceServiceImpl extends ServiceImpl<AdoptInstanceMapper, AdoptInstanceDO> implements AdoptInstanceService {

    private final UserService userService;

    @Override
    public IPage<AdoptInstanceRespDTO> queryMyAdoptInstances(AdoptInstancePageReqDTO reqDTO) {
        long userId = StpUtil.getLoginIdAsLong();
        UserRespDTO userRespDTO = userService.getLoginUser();
        Integer userType = userRespDTO.getUserType();
        LambdaQueryWrapper<AdoptInstanceDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdoptInstanceDO::getDelFlag, 0);
        if (reqDTO.getStatus() != null) {
            queryWrapper.eq(AdoptInstanceDO::getStatus, reqDTO.getStatus());
        }
        if (reqDTO.getItemId() != null) {
            queryWrapper.eq(AdoptInstanceDO::getItemId, reqDTO.getItemId());
        }
        if (reqDTO.getOwnerOrderId() != null) {
            queryWrapper.eq(AdoptInstanceDO::getOwnerOrderId, reqDTO.getOwnerOrderId());
        }
        if (Integer.valueOf(1).equals(userType)) {
            queryWrapper.eq(AdoptInstanceDO::getFarmerId, userId);
        } else {
            queryWrapper.eq(AdoptInstanceDO::getOwnerUserId, userId);
        }
        queryWrapper.orderByDesc(AdoptInstanceDO::getCreateTime);
        IPage<AdoptInstanceDO> pageResult = this.page(reqDTO, queryWrapper);
        return pageResult.convert(item -> BeanUtil.toBean(item, AdoptInstanceRespDTO.class));
    }
}
