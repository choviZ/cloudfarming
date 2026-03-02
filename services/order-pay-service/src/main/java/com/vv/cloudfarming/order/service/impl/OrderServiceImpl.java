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
import com.vv.cloudfarming.order.dto.resp.AdoptOrderDetailRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderCreateRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageWithProductInfoRespDTO;
import com.vv.cloudfarming.order.dto.resp.SkuOrderDetailRespDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
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

        // 标记是否已经进入锁库存阶段，失败时用于补偿释放库存
        boolean lockStockInvoked = false;
        try {
            // 2. 锁定库存（先锁库存，失败快速返回）
            lockStockInvoked = true;
            strategy.lockedStock(items);

            // 3. 按店铺拆单，逐单写入订单主表和明细表
            List<OrderDO> orders = new ArrayList<>();
            Map<Long, List<ProductItemDTO>> shopGroup = items.stream()
                    .collect(Collectors.groupingBy(item -> strategy.resolveShopId(item, orderContext)));

            for (Map.Entry<Long, List<ProductItemDTO>> entry : shopGroup.entrySet()) {
                Long shopId = entry.getKey();
                List<ProductItemDTO> itemList = entry.getValue();

                ReceiveAddressRespDTO receiveAddress = orderContext.getReceiveAddress();
                BigDecimal amount = strategy.calculateOrderAmount(itemList, orderContext);
                OrderDO order = OrderDO.builder()
                        .orderNo(generateOrderNo(userId))
                        .userId(userId)
                        .shopId(shopId)
                        .orderType(strategy.bizType())
                        .totalAmount(amount)
                        .actualPayAmount(amount)
                        .freightAmount(BigDecimal.ZERO)
                        .discountAmount(BigDecimal.ZERO)
                        .orderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode())
                        .receiveName(receiveAddress.getReceiverName())
                        .receivePhone(receiveAddress.getReceiverPhone())
                        .provinceName(receiveAddress.getProvinceName())
                        .cityName(receiveAddress.getCityName())
                        .districtName(receiveAddress.getDistrictName())
                        .detailAddress(receiveAddress.getDetailAddress())
                        .build();

                int orderInserted = baseMapper.insert(order);
                if (!SqlHelper.retBool(orderInserted)) {
                    throw new ServiceException("创建订单失败");
                }
                orders.add(order);

                // 4. 发送订单状态延迟检查消息，发送失败直接抛异常触发事务回滚
                MultiDelayMessage<String> msg = MultiDelayMessage.of(order.getOrderNo(),
                        15000, 30000, 120000, 300000, 43500, 900000);
                try {
                    rabbitTemplate.convertAndSend(
                            MqConstant.DELAY_EXCHANGE,
                            MqConstant.DELAY_ORDER_ROUTING_KEY,
                            msg,
                            new DelayMessageProcessor(msg.removeNextDelay())
                    );
                    log.info("发送延迟消息成功，orderNo={}", order.getOrderNo());
                } catch (AmqpException ex) {
                    log.error("发送延迟消息失败，orderNo={}", order.getOrderNo(), ex);
                    throw new ServiceException("发送延迟消息失败");
                }

                strategy.createOrderDetails(order, itemList, orderContext);
            }

            // 5. 创建支付单
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
                throw new ServiceException("创建支付单失败");
            }

            // 6. 回填支付单号到所有拆单后的订单
            List<String> orderNos = orders.stream().map(OrderDO::getOrderNo).toList();
            int updatedOrders = baseMapper.updatePayNoByOrderNo(payDO.getPayOrderNo(), userId, orderNos);
            if (updatedOrders != orderNos.size()) {
                throw new ServiceException("更新订单支付单号失败");
            }

            // 7. 返回支付信息
            return OrderCreateRespDTO.builder()
                    .payOrderNo(payDO.getPayOrderNo())
                    .payAmount(payTotalAmount)
                    .expireTime(DateUtil.toInstant(payDO.getExpireTime()).toEpochMilli())
                    .build();
        } catch (Exception ex) {
            // 8. 下单流程任意环节失败时，补偿释放已锁库存，避免库存长期占用
            if (lockStockInvoked) {
                try {
                    strategy.unlockStock(items);
                } catch (Exception unlockEx) {
                    log.error("下单失败后释放锁定库存失败，userId={}", userId, unlockEx);
                }
            }
            throw ex;
        }
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
        return orderPage.convert(each -> BeanUtil.toBean(each, OrderPageRespDTO.class));
    }

    @Override
    public List<AdoptOrderDetailRespDTO> getAdoptOrderDetail(String orderNo) {
        LambdaQueryWrapper<OrderDetailAdoptDO> wrapper = Wrappers.lambdaQuery(OrderDetailAdoptDO.class)
                .eq(OrderDetailAdoptDO::getOrderNo, orderNo);
        List<OrderDetailAdoptDO> orderDetails = orderDetailAdoptMapper.selectList(wrapper);
        return orderDetails.stream().map(each -> BeanUtil.toBean(each, AdoptOrderDetailRespDTO.class)).toList();
    }

    @Override
    public List<SkuOrderDetailRespDTO> getSkuOrderDetail(String orderNo) {
        LambdaQueryWrapper<OrderDetailSkuDO> wrapper = Wrappers.lambdaQuery(OrderDetailSkuDO.class)
                .eq(OrderDetailSkuDO::getOrderNo, orderNo);
        List<OrderDetailSkuDO> orderDetailSkus = orderDetailSkuMapper.selectList(wrapper);
        return orderDetailSkus.stream().map(each -> BeanUtil.toBean(each, SkuOrderDetailRespDTO.class)).toList();
    }

    private String generateOrderNo(Long userId) {
        long userTail = Math.floorMod(userId, 1_000_000L);
        return redisIdWorker.generateId("orderSN").toString() + String.format("%06d", userTail);
    }
}
