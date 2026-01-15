package com.vv.cloudfarming.shop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.shop.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.shop.dao.entity.AdoptOrderDO;
import com.vv.cloudfarming.shop.dao.mapper.AdoptItemMapper;
import com.vv.cloudfarming.shop.dao.mapper.AdoptOrderMapper;
import com.vv.cloudfarming.shop.dto.req.AdoptOrderCreateReqDTO;
import com.vv.cloudfarming.shop.dto.resp.AdoptOrderRespDTO;
import com.vv.cloudfarming.shop.enums.AdoptOrderStatusEnum;
import com.vv.cloudfarming.shop.service.AdoptOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Calendar;

/**
 * 认养订单服务实现类
 */
@Service
@RequiredArgsConstructor
public class AdoptOrderServiceImpl extends ServiceImpl<AdoptOrderMapper, AdoptOrderDO> implements AdoptOrderService {

    private final AdoptItemMapper adoptItemMapper;
    private final AdoptOrderMapper adoptOrderMapper;

    // 审核状态常量
    private static final Integer REVIEW_STATUS_APPROVED = 1; // 通过

    // 上架状态常量
    private static final Integer STATUS_ON_SHELF = 1; // 上架

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdoptOrderRespDTO createAdoptOrder(Long userId, AdoptOrderCreateReqDTO reqDTO) {
        // 1. 校验认养项目是否存在，且未被逻辑删除
        AdoptItemDO adoptItem = adoptItemMapper.selectById(reqDTO.getAdoptItemId());
        if (adoptItem == null) {
            throw new ClientException("认养项目不存在");
        }

        // 2. 校验认养项目审核状态
        if (!REVIEW_STATUS_APPROVED.equals(adoptItem.getReviewStatus())) {
            throw new ClientException("认养项目未审核通过，无法领养");
        }

        // 3. 校验认养项目上架状态
        if (!STATUS_ON_SHELF.equals(adoptItem.getStatus())) {
            throw new ClientException("认养项目未上架，无法领养");
        }

        // 4. 禁止用户领养自己发布的认养项目
        if (adoptItem.getUserId().equals(userId)) {
            throw new ClientException("不能领养自己发布的认养项目");
        }

        // 5. 校验同一用户是否已领养过该认养项目
        LambdaQueryWrapper<AdoptOrderDO> existQueryWrapper = new LambdaQueryWrapper<>();
        existQueryWrapper.eq(AdoptOrderDO::getBuyerId, userId);
        existQueryWrapper.eq(AdoptOrderDO::getAdoptItemId, reqDTO.getAdoptItemId());
        existQueryWrapper.eq(AdoptOrderDO::getDelFlag, 0);
        AdoptOrderDO existOrder = adoptOrderMapper.selectOne(existQueryWrapper);
        if (existOrder != null) {
            throw new ClientException("您已经领养过该认养项目，不可重复领养");
        }

        // 6. 创建认养订单
        AdoptOrderDO adoptOrder = new AdoptOrderDO();
        adoptOrder.setBuyerId(userId);
        adoptOrder.setAdoptItemId(reqDTO.getAdoptItemId());
        adoptOrder.setPrice(adoptItem.getPrice()); // 下单时快照，不可后续变更
        adoptOrder.setStartDate(new Date()); // 当前日期

        // 计算结束日期：start_date + adopt_item.adopt_days
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(adoptOrder.getStartDate());
        calendar.add(Calendar.DAY_OF_MONTH, adoptItem.getAdoptDays());
        adoptOrder.setEndDate(calendar.getTime());

        adoptOrder.setStatus(AdoptOrderStatusEnum.IN_PROGRESS.getCode()); // 初始状态为认养中

        // 7. 保存订单到数据库
        int inserted = adoptOrderMapper.insert(adoptOrder);
        if (inserted < 0) {
            throw new ServiceException("创建认养订单失败");
        }
        // 8. 转换为响应DTO并返回
        return BeanUtil.toBean(adoptOrder, AdoptOrderRespDTO.class);
    }
}
