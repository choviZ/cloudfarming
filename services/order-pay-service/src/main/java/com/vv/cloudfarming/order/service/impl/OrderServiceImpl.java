package com.vv.cloudfarming.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailAdoptDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.entity.PayDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailAdoptMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dao.mapper.PayOrderMapper;
import com.vv.cloudfarming.order.dto.common.ProductItemDTO;
import com.vv.cloudfarming.order.dto.common.ProductSummaryDTO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.*;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.order.mq.DelayMessageProcessor;
import com.vv.cloudfarming.order.mq.constant.MqConstant;
import com.vv.cloudfarming.order.mq.modal.MultiDelayMessage;
import com.vv.cloudfarming.order.remote.ShopRemoteService;
import com.vv.cloudfarming.order.service.OrderService;
import com.vv.cloudfarming.order.service.basics.chain.OrderChainContext;
import com.vv.cloudfarming.order.service.basics.chain.OrderContext;
import com.vv.cloudfarming.order.strategy.OrderCreateStrategy;
import com.vv.cloudfarming.order.strategy.StrategyFactory;
import com.vv.cloudfarming.order.utils.ProductUtil;
import com.vv.cloudfarming.order.utils.RedisIdWorker;
import com.vv.cloudfarming.product.dto.resp.ShopRespDTO;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务实现类 (Refactored)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderDO> implements OrderService {

    private final ShopRemoteService shopRemoteService;
    private final ProductUtil productUtil;
    private final RedisIdWorker redisIdWorker;
    private final OrderDetailAdoptMapper orderDetailAdoptMapper;
    private final OrderDetailSkuMapper orderDetailSkuMapper;
    private final OrderChainContext orderChainContext;
    private final OrderContext orderContext;
    private final StrategyFactory strategyFactory;
    private final PayOrderMapper payOrderMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateRespDTO createOrderV2(Long userId, OrderCreateReqDTO requestParam) {
        // 1. 参数校验
        orderChainContext.handler(requestParam);
        Integer orderType = requestParam.getOrderType();
        List<ProductItemDTO> items = requestParam.getItems();
        OrderCreateStrategy strategy = strategyFactory.get(orderType);

        // 2. 锁定库存
        strategy.lockedStock(items);

        // 3. 根据店铺拆单
        List<OrderDO> orders = new ArrayList<>();
        Map<Long, List<ProductItemDTO>> shopGroup = items.stream().collect(Collectors.groupingBy(item -> strategy.resolveShopId(item, orderContext)));
        for (Map.Entry<Long, List<ProductItemDTO>> entry : shopGroup.entrySet()) {
            Long shopId = entry.getKey();
            List<ProductItemDTO> itemList = entry.getValue();

            ReceiveAddressRespDTO receiveAddress = orderContext.getReceiveAddress();
            // 计算价格
            BigDecimal amount = strategy.calculateOrderAmount(itemList, orderContext);
            // 4. 创建订单
            OrderDO order = OrderDO.builder()
                    .orderNo(generateOrderNo(userId))
                    .userId(userId)
                    .shopId(shopId)
                    .orderType(strategy.bizType())
                    .totalAmount(amount)
                    .actualPayAmount(amount)
                    // 运费和优惠金额，暂时都为0
                    .freightAmount(new BigDecimal("0"))
                    .discountAmount(new BigDecimal("0"))
                    .orderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode())
                    .receiveName(receiveAddress.getReceiverName())
                    .receivePhone(receiveAddress.getReceiverPhone())
                    .provinceName(receiveAddress.getProvinceName())
                    .cityName(receiveAddress.getCityName())
                    .districtName(receiveAddress.getDistrictName())
                    .detailAddress(receiveAddress.getDetailAddress())
                    .build();
            baseMapper.insert(order);
            orders.add(order);

            // 组织延迟消息消息
            MultiDelayMessage<Long> msg = MultiDelayMessage.of(order.getId(),
                    15000, 30000, 120000, 300000, 43500, 900000);
            // 5. 发送消息延迟消息
            try {
                rabbitTemplate.convertAndSend(
                        MqConstant.DELAY_EXCHANGE,
                        MqConstant.DELAY_ORDER_ROUTING_KEY,
                        msg,
                        new DelayMessageProcessor(msg.removeNextDelay())
                );
                log.info("发送延迟消息成功，订单ID：{}", order.getId());
            } catch (AmqpException ex) {
                log.error("发送延迟消息失败", ex);
                // TODO 延迟消息补偿
            }

            // 6. 创建订单详情
            strategy.createOrderDetails(order, itemList, orderContext);
        }

        // 7. 创建支付单
        BigDecimal payTotalAmount = orders.stream()
                .map(OrderDO::getActualPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PayDO payDO = PayDO.builder()
                .payOrderNo(redisIdWorker.generateId("PaySN").toString())
                .buyerId(userId)
                .totalAmount(payTotalAmount)
                .payStatus(PayStatusEnum.UNPAID.getCode())
                .bizStatus(OrderStatusEnum.PENDING_PAYMENT.getCode())
                .payChannel(0)
                .expireTime(LocalDateTime.now().plusMinutes(15))
                .build();
        int inserted = payOrderMapper.insert(payDO);
        if (!SqlHelper.retBool(inserted)) {
            throw new ServiceException("支付单创建失败");
        }
        // 更新订单的支付单号
        List<Long> ids = orders.stream().map(OrderDO::getId).toList();
        baseMapper.updatePayOrderNoById(payDO.getPayOrderNo(), ids);

        return OrderCreateRespDTO.builder()
                .payOrderNo(payDO.getPayOrderNo())
                .payAmount(payTotalAmount)
                .expireTime(DateUtil.toInstant(payDO.getExpireTime()).toEpochMilli())
                .build();
    }

    @Override
    public IPage<OrderPageWithProductInfoRespDTO> listOrderWithProductInfo(OrderPageReqDTO requestParam) {
        Long id = requestParam.getId();
        Integer orderStatus = requestParam.getOrderStatus();
        Long userId = requestParam.getUserId();

        LambdaQueryWrapper<OrderDO> wrapper = Wrappers.lambdaQuery(OrderDO.class)
                .eq(ObjectUtil.isNotNull(id), OrderDO::getId, id)
                .eq(ObjectUtil.isNotNull(orderStatus), OrderDO::getOrderStatus, orderStatus)
                .eq(ObjectUtil.isNotNull(userId), OrderDO::getUserId, userId);
        IPage<OrderDO> orderPage = baseMapper.selectPage(requestParam, wrapper);
        // 转换
        return orderPage.convert(each -> {
            ShopRespDTO shop = shopRemoteService.getShopById(each.getShopId()).getData();
            ArrayList<ProductSummaryDTO> summaryList = new ArrayList<>();
            productUtil.buildProductSummary(each.getId(), each.getOrderType(), summaryList);
            return OrderPageWithProductInfoRespDTO.builder()
                    .id(each.getId())
                    .shopName(shop.getShopName())
                    .items(summaryList)
                    .totalPrice(each.getTotalAmount())
                    .build();
        });
    }

    @Override
    public IPage<OrderPageRespDTO> listOrders(OrderPageReqDTO requestParam) {
        Long id = requestParam.getId();
        String payOrderNo = requestParam.getOrderNo();
        Integer orderStatus = requestParam.getOrderStatus();
        Long userId = requestParam.getUserId();
        Long shopId = requestParam.getShopId();

        LambdaQueryWrapper<OrderDO> wrapper = Wrappers.lambdaQuery(OrderDO.class)
                .eq(ObjectUtil.isNotNull(id), OrderDO::getId, id)
                .eq(ObjectUtil.isNotNull(payOrderNo), OrderDO::getPayOrderNo, payOrderNo)
                .eq(ObjectUtil.isNotNull(orderStatus), OrderDO::getOrderStatus, orderStatus)
                .eq(ObjectUtil.isNotNull(userId), OrderDO::getUserId, userId)
                .eq(ObjectUtil.isNotNull(shopId), OrderDO::getShopId, shopId);
        IPage<OrderDO> orderPage = baseMapper.selectPage(requestParam, wrapper);
        return orderPage.convert(each -> {
            return BeanUtil.toBean(each, OrderPageRespDTO.class);
        });
    }

    @Override
    public List<AdoptOrderDetailRespDTO> getAdoptOrderDetail(String orderNo) {
        LambdaQueryWrapper<OrderDetailAdoptDO> wrapper = Wrappers.lambdaQuery(OrderDetailAdoptDO.class)
                .eq(OrderDetailAdoptDO::getOrderNo, orderNo);
        List<OrderDetailAdoptDO> orderDetails = orderDetailAdoptMapper.selectList(wrapper);
        return orderDetails.stream().map(each -> {
            return BeanUtil.toBean(each, AdoptOrderDetailRespDTO.class);
        }).toList();
    }

    @Override
    public List<SkuOrderDetailRespDTO> getSkuOrderDetail(String orderNo) {
        LambdaQueryWrapper<OrderDetailSkuDO> wrapper = Wrappers.lambdaQuery(OrderDetailSkuDO.class)
                .eq(OrderDetailSkuDO::getOrderNo, orderNo);
        List<OrderDetailSkuDO> orderDetailSkus = orderDetailSkuMapper.selectList(wrapper);
        return orderDetailSkus.stream().map(each -> {
            return BeanUtil.toBean(each, SkuOrderDetailRespDTO.class);
        }).toList();
    }

    private String generateOrderNo(Long userId) {
        return redisIdWorker.generateId("orderSN").toString();
    }
}
