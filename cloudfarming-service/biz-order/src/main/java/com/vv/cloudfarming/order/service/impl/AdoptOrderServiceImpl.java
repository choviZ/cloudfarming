package com.vv.cloudfarming.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.enums.ReviewStatusEnum;
import com.vv.cloudfarming.common.enums.ShelfStatusEnum;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.dao.entity.AdoptOrderDO;
import com.vv.cloudfarming.order.dto.req.AdoptOrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.AdoptOrderRespDTO;
import com.vv.cloudfarming.order.service.AdoptOrderService;
import com.vv.cloudfarming.shop.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.shop.dao.entity.AdoptInstanceDO;
import com.vv.cloudfarming.shop.dao.mapper.AdoptItemMapper;
import com.vv.cloudfarming.order.dao.mapper.AdoptOrderMapper;
import com.vv.cloudfarming.shop.enums.AdoptOrderStatusEnum;
import com.vv.cloudfarming.shop.enums.LivestockStatusEnum;
import com.vv.cloudfarming.shop.service.AdoptInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * 认养订单服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdoptOrderServiceImpl extends ServiceImpl<AdoptOrderMapper, AdoptOrderDO> implements AdoptOrderService {

    private final AdoptItemMapper adoptItemMapper;
    private final AdoptOrderMapper adoptOrderMapper;
    private final AdoptInstanceService adoptInstanceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdoptOrderRespDTO createAdoptOrder(Long userId, AdoptOrderCreateReqDTO reqDTO) {
        Integer quantity = reqDTO.getQuantity();
        // 1. 校验认养项目是否存在，且未被逻辑删除
        AdoptItemDO adoptItem = adoptItemMapper.selectById(reqDTO.getAdoptItemId());
        if (adoptItem == null) {
            throw new ClientException("认养项目不存在");
        }
        // 2. 校验认养项目审核状态
        if (!ReviewStatusEnum.APPROVED.getStatus().equals(adoptItem.getReviewStatus())) {
            throw new ClientException("认养项目未审核通过，无法领养");
        }
        // 3. 校验认养项目上架状态
        if (!ShelfStatusEnum.ONLINE.getCode().equals(adoptItem.getStatus())) {
            throw new ClientException("认养项目未上架，无法领养");
        }
        // 4. 禁止用户领养自己发布的认养项目
        if (adoptItem.getUserId().equals(userId)) {
            throw new ClientException("不能领养自己发布的认养项目");
        }

        // 5. 创建认养订单
        AdoptOrderDO adoptOrder = new AdoptOrderDO();
        adoptOrder.setBuyerId(userId);
        adoptOrder.setAdoptItemId(reqDTO.getAdoptItemId());
        adoptOrder.setPrice(adoptItem.getPrice()); // 下单时快照，不可后续变更

        // 计算结束日期：start_date + adopt_item.adopt_days
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(adoptOrder.getStartDate());
        calendar.add(Calendar.DAY_OF_MONTH, adoptItem.getAdoptDays());
        adoptOrder.setEndDate(calendar.getTime());
        adoptOrder.setOrderStatus(AdoptOrderStatusEnum.IN_PROGRESS.getCode()); // 初始状态为认养中
        adoptOrder.setReceiveId(reqDTO.getReceiveId());
        adoptOrder.setPayStatus(0); // 待支付
        // 6. 保存订单到数据库
        int inserted = adoptOrderMapper.insert(adoptOrder);
        if (inserted < 0) {
            throw new ServiceException("创建认养订单失败");
        }
        // 根据下单数量创建对应的养殖实例
        ArrayList<AdoptInstanceDO> liveStocks = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            AdoptInstanceDO livestockDO = AdoptInstanceDO.builder()
                    .itemId(adoptItem.getId())
                    .status(LivestockStatusEnum.AVAILABLE.getCode())
                    .build();
            liveStocks.add(livestockDO);
        }
        boolean saveBatch = adoptInstanceService.saveBatch(liveStocks);
        if (!saveBatch) {
            // TODO 失败处理
            log.error("批量创建牲畜记录失败，认养项目id：{}", adoptItem.getId());
        }
        // TODO 7.发送消息
        // 8. 转换为响应DTO并返回
        return BeanUtil.toBean(adoptOrder, AdoptOrderRespDTO.class);
    }
}
