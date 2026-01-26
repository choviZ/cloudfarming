package com.vv.cloudfarming.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.PayOrderDO;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.PayOrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderCreateRespDTO;
import com.vv.cloudfarming.order.service.OrderService;
import com.vv.cloudfarming.order.service.PayService;
import com.vv.cloudfarming.order.strategy.OrderCreateStrategy;
import com.vv.cloudfarming.order.strategy.OrderStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务实现类 (Refactored)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderDO> implements OrderService {

    private final OrderStrategyFactory strategyFactory;
    private final PayService payService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateRespDTO createOrder(Long userId, OrderCreateReqDTO reqDTO) {
        // 1. 获取对应的创建策略
        OrderCreateStrategy strategy = strategyFactory.getStrategy(reqDTO.getOrderType());

        // 2. 生成全局唯一的支付单号
        String payOrderNo = generatePayOrderNo(userId);
        
        // 3. 执行策略，生成业务订单列表
        List<OrderDO> orders = strategy.createOrders(userId, payOrderNo, reqDTO.getBizData());
        if (orders == null || orders.isEmpty()) {
            throw new ServiceException("订单创建失败：未生成有效订单");
        }
        
        // 4. 计算总金额并生成支付单
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderDO order : orders) {
            if (order.getActualPayAmount() != null) {
                totalAmount = totalAmount.add(order.getActualPayAmount());
            }
        }

        PayOrderCreateReqDTO payOrderCreateReq = new PayOrderCreateReqDTO();
        payOrderCreateReq.setBuyerId(userId);
        payOrderCreateReq.setPayOrderNo(payOrderNo);
        payOrderCreateReq.setTotalAmount(totalAmount);
        PayOrderDO payOrder = payService.createPayOrder(payOrderCreateReq);

        // 5. 构建响应
        OrderCreateRespDTO response = new OrderCreateRespDTO();
        response.setPayAmount(totalAmount);
        response.setPayOrderNo(payOrderNo);
        response.setExpireTime(payOrder.getExpireTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        return response;
    }


    private String generatePayOrderNo(Long userId) {
        long timestamp = System.currentTimeMillis() / 1000;
        String timePart = String.format("%08d", timestamp % 100000000);
        String userIdPart = String.format("%06d", userId % 1000000);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String randomPart = String.format("%04d", random.nextInt(10000));
        return "P" + timePart + userIdPart + randomPart;
    }
}
