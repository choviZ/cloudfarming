package com.vv.cloudfarming.order.strategy;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.vv.cloudfarming.common.enums.ReviewStatusEnum;
import com.vv.cloudfarming.common.enums.ShelfStatusEnum;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailAdoptDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailAdoptMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.model.AdoptOrderCreate;
import com.vv.cloudfarming.shop.dao.entity.AdoptInstanceDO;
import com.vv.cloudfarming.shop.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.shop.dao.mapper.AdoptItemMapper;
import com.vv.cloudfarming.shop.enums.LivestockStatusEnum;
import com.vv.cloudfarming.shop.service.AdoptInstanceService;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import com.vv.cloudfarming.user.service.ReceiveAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 认养订单创建策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdoptOrderStrategy implements OrderCreateStrategy {

    private final AdoptItemMapper adoptItemMapper;
    private final AdoptInstanceService adoptInstanceService;
    private final ReceiveAddressService receiveAddressService;
    private final OrderMapper orderMapper;
    private final OrderDetailSkuMapper orderDetailSkuMapper;
    private final OrderDetailAdoptMapper orderDetailAdoptMapper;

    @Override
    public List<OrderDO> createOrders(Long userId, String payOrderNo, JSONObject bizData) {
        // 1. 解析参数
        AdoptOrderCreate req = bizData.toBean(AdoptOrderCreate.class);
        validate(req, userId);
        
        // 2. 准备数据
        AdoptItemDO item = adoptItemMapper.selectById(req.getAdoptItemId());
        ReceiveAddressRespDTO address = receiveAddressService.getReceiveAddressById(req.getReceiveId());
        
        // 3. 扣库存
        lockStock(req.getAdoptItemId(), req.getQuantity());
        
        // 4. 创建订单
        BigDecimal totalAmount = item.getPrice().multiply(new BigDecimal(req.getQuantity()));
        
        OrderDO order = OrderDO.builder()
                .orderNo(generateOrderNo(userId))
                .payOrderNo(payOrderNo)
                .userId(userId)
                .shopId(item.getFarmerId()) // 认养项目的发布者作为店铺
                .orderType(OrderTypeConstant.ADOPT)
                .totalAmount(totalAmount)
                .actualPayAmount(totalAmount)
                .freightAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .orderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode())
                .receiveName(address.getReceiverName())
                .receivePhone(address.getReceiverPhone())
                .receiveAddress(address.getProvinceName() + address.getCityName() + address.getDistrictName() + address.getDetailAddress())
                .build();
        
        orderMapper.insert(order);
        
        // 5. 创建认养明细 (AdoptDetail)
        // 注意：实际的认养周期(startDate/endDate)通常在支付成功后确定或更新
        // 这里先根据当前时间预估，确保订单详情能查到认养信息
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, item.getAdoptDays());

        OrderDetailAdoptDO adoptDetail = OrderDetailAdoptDO.builder()
                .orderId(order.getId())
                .adoptItemId(item.getId())
                .itemName(item.getTitle())
                .itemImage(item.getCoverImage())
                .price(item.getPrice())
                .quantity(req.getQuantity())
                .totalAmount(totalAmount)
                .startDate(new Date()) // 预估开始时间
                .endDate(calendar.getTime()) // 预估结束时间
                .build();
        orderDetailAdoptMapper.insert(adoptDetail);
        
        // 7. 预占养殖实例 (逻辑保留，状态设为不可用，支付后才正式归属)
        // 注意：这里简单实现为直接创建实例，实际可能需要等待支付回调
        // createInstances(userId, order.getId(), item, req.getQuantity());
        
        return Collections.singletonList(order);
    }
    
    private void validate(AdoptOrderCreate req, Long userId) {
        if (req == null || req.getAdoptItemId() == null || req.getQuantity() <= 0) {
            throw new ClientException("参数错误");
        }
        AdoptItemDO item = adoptItemMapper.selectById(req.getAdoptItemId());
        if (item == null) throw new ClientException("认养项目不存在");
        
        if (!ReviewStatusEnum.APPROVED.getStatus().equals(item.getReviewStatus()) || 
            !ShelfStatusEnum.ONLINE.getCode().equals(item.getStatus())) {
            throw new ClientException("项目未上架");
        }
        if (item.getFarmerId().equals(userId)) {
            throw new ClientException("不能认养自己的项目");
        }
    }
    
    private void lockStock(Long itemId, Integer quantity) {
        LambdaUpdateWrapper<AdoptItemDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AdoptItemDO::getId, itemId)
                .ge(AdoptItemDO::getAvailableCount, quantity)
                .setSql("available_count = available_count - " + quantity);
        int updated = adoptItemMapper.update(null, wrapper);
        if (updated != 1) throw new ClientException("库存不足");
    }
    
    private void createInstances(Long userId, Long orderId, AdoptItemDO item, Integer quantity) {
        List<AdoptInstanceDO> instances = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            instances.add(AdoptInstanceDO.builder()
                    .itemId(item.getId())
                    .farmerId(item.getFarmerId())
                    .ownerUserId(userId)
                    .ownerOrderId(orderId)
                    .status(LivestockStatusEnum.AVAILABLE.getCode()) // 待支付状态? 这里暂用 AVAILABLE
                    .build());
        }
        adoptInstanceService.saveBatch(instances);
    }
    
    private String generateOrderNo(Long userId) {
        long timestamp = System.currentTimeMillis() / 1000;
        String timePart = String.format("%08d", timestamp % 100000000);
        String userIdPart = String.format("%06d", userId % 1000000);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String randomPart = String.format("%04d", random.nextInt(10000));
        return timePart + userIdPart + randomPart;
    }
}
