package com.vv.cloudfarming.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.cloudfarming.order.constant.BizStatusConstant;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailAdoptDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.entity.PayDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailAdoptMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dao.mapper.PayOrderMapper;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.order.remote.AdoptItemRemoteService;
import com.vv.cloudfarming.order.remote.SkuRemoteService;
import com.vv.cloudfarming.order.service.OrderExpireService;
import com.vv.cloudfarming.product.dto.req.LockStockReqDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 已过期未支付订单关闭实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderExpireServiceImpl implements OrderExpireService {

    private static final int DEFAULT_BATCH_LIMIT = 100;

    private final OrderMapper orderMapper;
    private final PayOrderMapper payOrderMapper;
    private final OrderDetailAdoptMapper orderDetailAdoptMapper;
    private final OrderDetailSkuMapper orderDetailSkuMapper;
    private final AdoptItemRemoteService adoptItemRemoteService;
    private final SkuRemoteService skuRemoteService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeExpiredPayOrder(String payOrderNo) {
        if (payOrderNo == null || payOrderNo.isBlank()) {
            return false;
        }

        LambdaUpdateWrapper<PayDO> payUpdateWrapper = Wrappers.lambdaUpdate(PayDO.class)
            .eq(PayDO::getPayOrderNo, payOrderNo)
            .eq(PayDO::getPayStatus, PayStatusEnum.UNPAID.getCode())
            .eq(PayDO::getBizStatus, BizStatusConstant.WAIT_PAY)
            .le(PayDO::getExpireTime, LocalDateTime.now())
            .set(PayDO::getBizStatus, BizStatusConstant.CLOSED);
        int payUpdated = payOrderMapper.update(null, payUpdateWrapper);
        if (!SqlHelper.retBool(payUpdated)) {
            return false;
        }

        List<OrderDO> orders = orderMapper.selectList(
            Wrappers.lambdaQuery(OrderDO.class)
                .eq(OrderDO::getPayOrderNo, payOrderNo)
                .eq(OrderDO::getOrderStatus, OrderStatusEnum.PENDING_PAYMENT.getCode())
        );
        for (OrderDO order : safeList(orders)) {
            closeSinglePendingOrder(order);
        }
        log.info("已关闭过期未支付订单，payOrderNo={}, orderCount={}", payOrderNo, safeList(orders).size());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeExpiredPayOrderByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            return false;
        }
        OrderDO order = orderMapper.selectOne(
            Wrappers.lambdaQuery(OrderDO.class).eq(OrderDO::getOrderNo, orderNo)
        );
        if (order == null) {
            return false;
        }
        return closeExpiredPayOrder(order.getPayOrderNo());
    }

    @Override
    public int closeExpiredPayOrders(int limit) {
        int batchSize = limit > 0 ? limit : DEFAULT_BATCH_LIMIT;
        List<PayDO> expiredPayOrders = payOrderMapper.selectList(
            Wrappers.lambdaQuery(PayDO.class)
                .eq(PayDO::getPayStatus, PayStatusEnum.UNPAID.getCode())
                .eq(PayDO::getBizStatus, BizStatusConstant.WAIT_PAY)
                .le(PayDO::getExpireTime, LocalDateTime.now())
                .orderByAsc(PayDO::getExpireTime)
                .last("LIMIT " + batchSize)
        );
        if (expiredPayOrders == null || expiredPayOrders.isEmpty()) {
            return 0;
        }

        int closedCount = 0;
        for (PayDO payOrder : expiredPayOrders) {
            try {
                if (closeExpiredPayOrder(payOrder.getPayOrderNo())) {
                    closedCount++;
                }
            } catch (Exception ex) {
                log.error("关闭过期未支付订单失败，payOrderNo={}", payOrder.getPayOrderNo(), ex);
            }
        }
        return closedCount;
    }

    private void closeSinglePendingOrder(OrderDO order) {
        int orderUpdated = orderMapper.update(
            null,
            Wrappers.lambdaUpdate(OrderDO.class)
                .eq(OrderDO::getOrderNo, order.getOrderNo())
                .eq(OrderDO::getOrderStatus, OrderStatusEnum.PENDING_PAYMENT.getCode())
                .set(OrderDO::getOrderStatus, OrderStatusEnum.CANCEL.getCode())
        );
        if (!SqlHelper.retBool(orderUpdated)) {
            return;
        }

        try {
            unlockStock(order);
        } catch (Exception ex) {
            log.error("释放过期订单锁定库存失败，orderNo={}", order.getOrderNo(), ex);
        }
    }

    private void unlockStock(OrderDO order) {
        if (OrderTypeConstant.ADOPT == order.getOrderType()) {
            List<OrderDetailAdoptDO> adoptDetails = orderDetailAdoptMapper.selectList(
                Wrappers.lambdaQuery(OrderDetailAdoptDO.class).eq(OrderDetailAdoptDO::getOrderNo, order.getOrderNo())
            );
            for (OrderDetailAdoptDO detail : safeList(adoptDetails)) {
                LockStockReqDTO requestParam = new LockStockReqDTO();
                requestParam.setId(detail.getAdoptItemId());
                requestParam.setQuantity(detail.getQuantity());
                adoptItemRemoteService.unlockAdoptItemStock(requestParam);
            }
            return;
        }

        if (OrderTypeConstant.GOODS == order.getOrderType()) {
            List<OrderDetailSkuDO> skuDetails = orderDetailSkuMapper.selectList(
                Wrappers.lambdaQuery(OrderDetailSkuDO.class).eq(OrderDetailSkuDO::getOrderNo, order.getOrderNo())
            );
            for (OrderDetailSkuDO skuDetail : safeList(skuDetails)) {
                LockStockReqDTO requestParam = new LockStockReqDTO();
                requestParam.setId(skuDetail.getSkuId());
                requestParam.setQuantity(skuDetail.getQuantity());
                skuRemoteService.unlockStock(requestParam);
            }
        }
    }

    private <T> List<T> safeList(List<T> items) {
        return items == null ? Collections.emptyList() : items;
    }
}
