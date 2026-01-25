package com.vv.cloudfarming.order.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.order.dao.entity.AdoptOrderDO;
import com.vv.cloudfarming.order.dto.req.AdoptOrderPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.AdoptOrderRespDTO;
import com.vv.cloudfarming.order.service.AdoptOrderService;
import com.vv.cloudfarming.shop.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.shop.dao.mapper.AdoptItemMapper;
import com.vv.cloudfarming.order.dao.mapper.AdoptOrderMapper;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import com.vv.cloudfarming.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 认养订单服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdoptOrderServiceImpl extends ServiceImpl<AdoptOrderMapper, AdoptOrderDO> implements AdoptOrderService {

    private final AdoptItemMapper adoptItemMapper;
    private final UserService userService;

    @Override
    public IPage<AdoptOrderRespDTO> pageAdoptOrders(AdoptOrderPageReqDTO requestParam) {
        long userId = StpUtil.getLoginIdAsLong();
        UserRespDTO userRespDTO = userService.getLoginUser();
        Integer userType = userRespDTO.getUserType();

        Integer orderStatus = requestParam.getOrderStatus();
        Integer payStatus = requestParam.getPayStatus();
        Long adoptItemId = requestParam.getAdoptItemId();
        // 构造查询条件
        LambdaQueryWrapper<AdoptOrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdoptOrderDO::getDelFlag, 0);
        queryWrapper.eq(ObjectUtil.isNotNull(orderStatus), AdoptOrderDO::getOrderStatus, orderStatus);
        queryWrapper.eq(ObjectUtil.isNotNull(payStatus), AdoptOrderDO::getPayStatus, payStatus);
        queryWrapper.eq(ObjectUtil.isNotNull(adoptItemId), AdoptOrderDO::getAdoptItemId, adoptItemId);
        // 区分不同角色
        if (UserRoleConstant.FARMER_CODE.equals(userType)) {
            // 农户
            LambdaQueryWrapper<AdoptItemDO> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.select(AdoptItemDO::getId);
            itemWrapper.eq(AdoptItemDO::getFarmerId, userId);
            itemWrapper.eq(AdoptItemDO::getDelFlag, 0);
            List<AdoptItemDO> items = adoptItemMapper.selectList(itemWrapper);
            if (items == null || items.isEmpty()) {
                Page<AdoptOrderRespDTO> emptyPage = new Page<>(requestParam.getCurrent(), requestParam.getSize());
                emptyPage.setTotal(0);
                emptyPage.setRecords(Collections.emptyList());
                return emptyPage;
            }
            // 查到自己发布的认养项目id集合
            List<Long> itemIds = items.stream().map(AdoptItemDO::getId).collect(Collectors.toList());
            queryWrapper.in(AdoptOrderDO::getAdoptItemId, itemIds);
        } else if (UserRoleConstant.USER_CODE.equals(userType)) {
            // 普通用户，查询自己的订单
            queryWrapper.eq(AdoptOrderDO::getBuyerId, userId);
        }
        queryWrapper.orderByDesc(AdoptOrderDO::getCreateTime);
        IPage<AdoptOrderDO> pageResult = this.page(requestParam, queryWrapper);
        return pageResult.convert(item -> BeanUtil.toBean(item, AdoptOrderRespDTO.class));
    }
}
