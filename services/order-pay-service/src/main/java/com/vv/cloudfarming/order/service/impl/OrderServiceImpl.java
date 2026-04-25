package com.vv.cloudfarming.order.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailAdoptDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.entity.OrderSkuReviewDO;
import com.vv.cloudfarming.order.dao.entity.PayDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailAdoptMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderSkuReviewMapper;
import com.vv.cloudfarming.order.dao.mapper.PayOrderMapper;
import com.vv.cloudfarming.order.dto.common.FarmerOrderTrendAggDTO;
import com.vv.cloudfarming.order.dto.common.OrderReviewAggDTO;
import com.vv.cloudfarming.order.dto.common.ProductItemDTO;
import com.vv.cloudfarming.order.dto.common.ProductSummaryDTO;
import com.vv.cloudfarming.order.dto.req.OrderAssignAdoptItemReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderAssignAdoptReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderAdminUpdateReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderFulfillAdoptReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderPageReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderReceiveReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderShipReqDTO;
import com.vv.cloudfarming.order.dto.req.SeckillCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.AdoptOrderDetailRespDTO;
import com.vv.cloudfarming.order.dto.resp.FarmerOrderStatisticsRespDTO;
import com.vv.cloudfarming.order.dto.resp.FarmerOrderTrendPointRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderCreateRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderLogisticsRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageWithProductInfoRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderSimpleRespDTO;
import com.vv.cloudfarming.order.dto.resp.SkuOrderDetailRespDTO;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.order.mq.DelayMessageProcessor;
import com.vv.cloudfarming.order.mq.constant.MqConstant;
import com.vv.cloudfarming.order.mq.modal.MultiDelayMessage;
import com.vv.cloudfarming.order.mq.modal.SeckillOrderMessage;
import com.vv.cloudfarming.order.remote.AdoptItemRemoteService;
import com.vv.cloudfarming.order.remote.ShopRemoteService;
import com.vv.cloudfarming.order.service.OrderService;
import com.vv.cloudfarming.order.service.basics.chain.OrderChainContext;
import com.vv.cloudfarming.order.service.basics.chain.OrderContext;
import com.vv.cloudfarming.order.service.query.AliyunLogisticsQueryService;
import com.vv.cloudfarming.order.service.query.OrderProductSummaryQueryService;
import com.vv.cloudfarming.order.strategy.OrderCreateStrategy;
import com.vv.cloudfarming.order.strategy.StrategyFactory;
import com.vv.cloudfarming.order.utils.RedisIdWorker;
import com.vv.cloudfarming.product.dto.req.AdoptInstanceAssignReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptInstanceCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptInstanceFulfillReqDTO;
import com.vv.cloudfarming.product.dao.entity.SeckillActivityDO;
import com.vv.cloudfarming.product.dto.resp.AdoptInstanceFulfillRespDTO;
import com.vv.cloudfarming.product.dto.resp.ShopRespDTO;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderDO> implements OrderService {

    private static final String SECKILL_CACHE_KEY = "cloudfarming:seckill";
    private static final String SECKILL_CLAIM_RECORD = "cloudfarming:seckill:record:";
    private static final String STOCK_DECREMENT_AND_SAVE_USER_RECEIVE_LUA_PATH = "lua/stock_decrement_and_save_user_receive.lua";
    private static final String STOCK_ROLLBACK_AND_REDUCE_USER_RECEIVE_LUA_PATH = "lua/stock_rollback_and_reduce_user_receive.lua";
    private static final List<Integer> FARMER_STAT_EXCLUDED_ORDER_STATUSES = List.of(
        OrderStatusEnum.PENDING_PAYMENT.getCode(),
        OrderStatusEnum.CANCEL.getCode()
    );
    private final ShopRemoteService shopRemoteService;
    private final OrderProductSummaryQueryService orderProductSummaryQueryService;
    private final AliyunLogisticsQueryService aliyunLogisticsQueryService;
    private final RedisIdWorker redisIdWorker;
    private final OrderDetailAdoptMapper orderDetailAdoptMapper;
    private final OrderDetailSkuMapper orderDetailSkuMapper;
    private final OrderSkuReviewMapper orderSkuReviewMapper;
    private final OrderChainContext orderChainContext;
    private final OrderContext orderContext;
    private final StrategyFactory strategyFactory;
    private final PayOrderMapper payOrderMapper;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final AdoptItemRemoteService adoptItemRemoteService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateRespDTO createOrderV2(Long userId, OrderCreateReqDTO requestParam) {
        List<ProductItemDTO> items = requestParam.getItems();
        OrderCreateStrategy strategy = null;

        boolean lockStockInvoked = false;
        orderContext.clear();
        try {
            orderChainContext.handler(requestParam);
            Integer orderType = requestParam.getOrderType();
            OrderCreateStrategy resolvedStrategy = strategyFactory.get(orderType);
            strategy = resolvedStrategy;
            lockStockInvoked = true;
            // 锁定库存
            resolvedStrategy.lockedStock(items);

            List<OrderDO> orders = new ArrayList<>();
            String paySN = redisIdWorker.generateId("PaySN").toString();
            Map<Long, List<ProductItemDTO>> shopGroup = items.stream()
                .collect(Collectors.groupingBy(item -> resolvedStrategy.resolveShopId(item, orderContext)));

            // 根据店铺拆分
            for (Map.Entry<Long, List<ProductItemDTO>> entry : shopGroup.entrySet()) {
                Long shopId = entry.getKey();
                List<ProductItemDTO> itemList = entry.getValue();

                ReceiveAddressRespDTO receiveAddress = orderContext.getReceiveAddress();
                BigDecimal amount = resolvedStrategy.calculateOrderAmount(itemList, orderContext);
                OrderDO order = OrderDO.builder()
                    .orderNo(generateOrderNo(userId))
                    .payOrderNo(paySN)
                    .userId(userId)
                    .shopId(shopId)
                    .orderType(resolvedStrategy.bizType())
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
                orders.add(order);
                // 创建订单详情
                resolvedStrategy.createOrderDetails(order, itemList, orderContext);
            }
            boolean orderSaved = this.saveBatch(orders);
            /*
                BigDecimal payTotalAmount = orders.stream()
                    .map(OrderDO::getActualPayAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                PayDO payDO = PayDO.builder()
                    .payOrderNo(paySN)
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
                orders.forEach(order -> applicationEventPublisher.publishEvent(new OrderDelayCheckEvent(this, order.getOrderNo())));
                return OrderCreateRespDTO.builder()
                    .payOrderNo(payDO.getPayOrderNo())
                    .payAmount(payTotalAmount)
                    .expireTime(DateUtil.toInstant(payDO.getExpireTime()).toEpochMilli())
                    .build();
            }
            */
            if (!orderSaved) {
                throw new ServiceException("订单创建失败");
            }
            // 发送延迟消息
            for (OrderDO order : orders) {
                MultiDelayMessage<String> msg = MultiDelayMessage.of(
                    order.getOrderNo(), 15000, 30000, 120000, 300000, 435000, 900000
                );
                try {
                    rabbitTemplate.convertAndSend(
                        MqConstant.DELAY_EXCHANGE,
                        MqConstant.DELAY_ORDER_ROUTING_KEY,
                        msg,
                        new DelayMessageProcessor(msg.removeNextDelay())
                    );
                    log.info("订单延迟消息发送成功，orderNo={}", order.getOrderNo());
                } catch (AmqpException ex) {
                    log.error("订单延迟消息发送失败，orderNo={}", order.getOrderNo(), ex);
                    throw new ServiceException("发送订单延迟消息失败");
                }
            }
            // 创建支付单
            BigDecimal payTotalAmount = orders.stream()
                .map(OrderDO::getActualPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            PayDO payDO = PayDO.builder()
                .payOrderNo(paySN)
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
            return OrderCreateRespDTO.builder()
                .payOrderNo(payDO.getPayOrderNo())
                .payAmount(payTotalAmount)
                .expireTime(DateUtil.toInstant(payDO.getExpireTime()).toEpochMilli())
                .build();
        } catch (Exception ex) {
            if (lockStockInvoked && strategy != null) {
                try {
                    strategy.unlockStock(items);
                } catch (Exception unlockEx) {
                    log.error("创建订单失败后回滚库存异常，userId={}", userId, unlockEx);
                }
            }
            throw ex;
        } finally {
            orderContext.clear();
        }
    }

    @Override
    public String createSeckillOrder(SeckillCreateReqDTO requestParam) {
        Long seckillId = requestParam.getSeckillId();
        Integer nums = requestParam.getNums();
        Long receiveId = requestParam.getReceiveId();
        long userId = StpUtil.getLoginIdAsLong();

        String seckillCacheKey = SECKILL_CACHE_KEY + seckillId;
        Map<String, String> map = stringRedisTemplate.<String, String>opsForHash().entries(seckillCacheKey);
        if (CollUtil.isEmpty(map)) {
            throw new ClientException("秒杀活动不存在");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = parseDateTime(map.get("startTime"), formatter);
        LocalDateTime endTime = parseDateTime(map.get("endTime"), formatter);
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime) || now.isAfter(endTime)) {
            throw new ClientException("当前不在秒杀时间范围内");
        }

        Integer limit = Convert.toInt(map.get("limitPerUser"), 0);
        if (limit <= 0) {
            throw new ServiceException("秒杀活动限购参数异常");
        }

        String userCountKey = SECKILL_CLAIM_RECORD + seckillId;
        long expireSeconds = Math.max(1, Duration.between(now, endTime).getSeconds());

        DefaultRedisScript<Long> buildLuaScript = Singleton.get(STOCK_DECREMENT_AND_SAVE_USER_RECEIVE_LUA_PATH, () -> {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(STOCK_DECREMENT_AND_SAVE_USER_RECEIVE_LUA_PATH)));
            redisScript.setResultType(Long.class);
            return redisScript;
        });

        Long stockDecrementLuaResult = stringRedisTemplate.execute(
            buildLuaScript,
            List.of(seckillCacheKey, userCountKey),
            String.valueOf(nums),
            String.valueOf(userId),
            String.valueOf(expireSeconds),
            String.valueOf(limit)
        );
        if (ObjectUtil.isNull(stockDecrementLuaResult)) {
            throw new ServiceException("秒杀库存扣减失败");
        }
        if (stockDecrementLuaResult == -1) {
            throw new ClientException("库存不足");
        }
        if (stockDecrementLuaResult == -2) {
            throw new ClientException("购买数量超出限购");
        }
        if (stockDecrementLuaResult != 0) {
            throw new ServiceException("秒杀库存扣减失败");
        }

        String paySN = redisIdWorker.generateId("PaySN").toString();
        SeckillOrderMessage msg = SeckillOrderMessage.builder()
            .seckillActivityDO(buildSeckillActivity(map, formatter))
            .payNo(paySN)
            .orderNo(generateOrderNo(userId))
            .userId(userId)
            .receiveId(receiveId)
            .nums(Long.valueOf(nums))
            .build();

        try {
            rabbitTemplate.convertAndSend("order-event-exange", "order.seckill.order", msg);
        } catch (AmqpException ex) {
            rollbackSeckillCacheQuietly(seckillId, userId, nums);
            log.error("秒杀下单消息发送失败，已回滚 Redis 预扣库存，seckillId={}, userId={}, nums={}",
                seckillId, userId, nums, ex);
            throw new ServiceException("秒杀下单失败，请稍后重试");
        }
        return paySN;
    }

    @Override
    public IPage<OrderPageWithProductInfoRespDTO> listOrderWithProductInfo(OrderPageReqDTO requestParam) {
        Long id = requestParam.getId();
        Integer orderStatus = requestParam.getOrderStatus();
        Long userId = requestParam.getUserId();
        Integer orderType = requestParam.getOrderType();

        LambdaQueryWrapper<OrderDO> wrapper = Wrappers.lambdaQuery(OrderDO.class)
            .eq(ObjectUtil.isNotNull(id), OrderDO::getId, id)
            .eq(ObjectUtil.isNotNull(orderStatus), OrderDO::getOrderStatus, orderStatus)
            .eq(ObjectUtil.isNotNull(userId), OrderDO::getUserId, userId)
            .eq(ObjectUtil.isNotNull(orderType), OrderDO::getOrderType, orderType)
            .orderByDesc(OrderDO::getCreateTime)
            .orderByDesc(OrderDO::getId);

        IPage<OrderDO> orderPage = baseMapper.selectPage(requestParam, wrapper);
        List<String> orderNos = orderPage.getRecords().stream()
            .map(OrderDO::getOrderNo)
            .collect(Collectors.toList());
        Map<String, List<ProductSummaryDTO>> productSummariesByOrderNo = orderProductSummaryQueryService.mapByOrderNos(orderNos);
        Map<String, OrderReviewAggDTO> reviewAggMap = mapOrderReviewAggByOrderNos(orderNos);
        return orderPage.convert(each -> buildOrderPageWithProductInfoResp(each, productSummariesByOrderNo, reviewAggMap));
    }

    @Override
    public IPage<OrderPageRespDTO> listOrders(OrderPageReqDTO requestParam) {
        Long id = requestParam.getId();
        String orderNo = requestParam.getOrderNo();
        Integer orderStatus = requestParam.getOrderStatus();
        Long userId = requestParam.getUserId();
        Integer orderType = requestParam.getOrderType();
        Long shopId = requestParam.getShopId();

        LambdaQueryWrapper<OrderDO> wrapper = Wrappers.lambdaQuery(OrderDO.class)
            .eq(ObjectUtil.isNotNull(id), OrderDO::getId, id)
            .eq(StrUtil.isNotBlank(orderNo), OrderDO::getOrderNo, orderNo)
            .eq(ObjectUtil.isNotNull(orderStatus), OrderDO::getOrderStatus, orderStatus)
            .eq(ObjectUtil.isNotNull(userId), OrderDO::getUserId, userId)
            .eq(ObjectUtil.isNotNull(orderType), OrderDO::getOrderType, orderType)
            .eq(ObjectUtil.isNotNull(shopId), OrderDO::getShopId, shopId)
            .orderByDesc(OrderDO::getCreateTime)
            .orderByDesc(OrderDO::getId);

        IPage<OrderDO> orderPage = baseMapper.selectPage(requestParam, wrapper);
        return orderPage.convert(this::buildOrderPageResp);
    }

    @Override
    public IPage<OrderPageRespDTO> listCurrentFarmerOrders(OrderPageReqDTO requestParam) {
        ShopRespDTO shop = getCurrentFarmerShop();
        requestParam.setShopId(shop.getId());
        return listOrders(requestParam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderByAdmin(OrderAdminUpdateReqDTO requestParam) {
        String orderNo = StrUtil.trim(requestParam.getOrderNo());
        OrderDO order = baseMapper.selectOne(Wrappers.lambdaQuery(OrderDO.class)
            .eq(OrderDO::getOrderNo, orderNo));
        if (order == null) {
            throw new ClientException("订单不存在");
        }

        boolean hasUpdateField = ObjectUtil.isNotNull(requestParam.getOrderStatus())
            || requestParam.getLogisticsNo() != null
            || requestParam.getLogisticsCompany() != null
            || requestParam.getReceiveName() != null
            || requestParam.getReceivePhone() != null;
        if (!hasUpdateField) {
            throw new ClientException("请至少填写一个需要更新的字段");
        }

        Integer targetOrderStatus = requestParam.getOrderStatus();
        if (targetOrderStatus != null) {
            OrderStatusEnum.getByCode(targetOrderStatus);
        }
        Integer finalOrderStatus = targetOrderStatus == null ? order.getOrderStatus() : targetOrderStatus;
        String finalLogisticsNo = requestParam.getLogisticsNo() == null
            ? order.getLogisticsNo()
            : normalizeOptionalText(requestParam.getLogisticsNo());
        String finalLogisticsCompany = requestParam.getLogisticsCompany() == null
            ? order.getLogisticsCompany()
            : normalizeOptionalText(requestParam.getLogisticsCompany());
        if (Objects.equals(finalOrderStatus, OrderStatusEnum.SHIPPED.getCode())
            && (StrUtil.isBlank(finalLogisticsNo) || StrUtil.isBlank(finalLogisticsCompany))) {
            throw new ClientException("订单状态为已发货时，物流单号和物流公司不能为空");
        }

        OrderDO updateOrder = new OrderDO();
        updateOrder.setId(order.getId());
        boolean changed = false;

        if (targetOrderStatus != null && !Objects.equals(targetOrderStatus, order.getOrderStatus())) {
            updateOrder.setOrderStatus(targetOrderStatus);
            changed = true;
            if (Objects.equals(targetOrderStatus, OrderStatusEnum.SHIPPED.getCode()) && order.getDeliveryTime() == null) {
                updateOrder.setDeliveryTime(LocalDateTime.now());
            }
            if (Objects.equals(targetOrderStatus, OrderStatusEnum.COMPLETED.getCode()) && order.getReceiveTime() == null) {
                updateOrder.setReceiveTime(LocalDateTime.now());
            }
        }

        if (requestParam.getLogisticsNo() != null) {
            String normalizedLogisticsNo = normalizeOptionalText(requestParam.getLogisticsNo());
            if (!Objects.equals(order.getLogisticsNo(), normalizedLogisticsNo)) {
                updateOrder.setLogisticsNo(normalizedLogisticsNo);
                changed = true;
            }
        }
        if (requestParam.getLogisticsCompany() != null) {
            String normalizedLogisticsCompany = normalizeOptionalText(requestParam.getLogisticsCompany());
            if (!Objects.equals(order.getLogisticsCompany(), normalizedLogisticsCompany)) {
                updateOrder.setLogisticsCompany(normalizedLogisticsCompany);
                changed = true;
            }
        }
        if (requestParam.getReceiveName() != null) {
            String normalizedReceiveName = normalizeOptionalText(requestParam.getReceiveName());
            if (!Objects.equals(order.getReceiveName(), normalizedReceiveName)) {
                updateOrder.setReceiveName(normalizedReceiveName);
                changed = true;
            }
        }
        if (requestParam.getReceivePhone() != null) {
            String normalizedReceivePhone = normalizeOptionalText(requestParam.getReceivePhone());
            if (!Objects.equals(order.getReceivePhone(), normalizedReceivePhone)) {
                updateOrder.setReceivePhone(normalizedReceivePhone);
                changed = true;
            }
        }

        if (!changed) {
            return;
        }

        int updated = baseMapper.updateById(updateOrder);
        if (!SqlHelper.retBool(updated)) {
            throw new ServiceException("订单更新失败，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipCurrentFarmerOrder(OrderShipReqDTO requestParam) {
        ShopRespDTO shop = getCurrentFarmerShop();
        String orderNo = StrUtil.trim(requestParam.getOrderNo());
        OrderDO order = baseMapper.selectOne(Wrappers.lambdaQuery(OrderDO.class)
            .eq(OrderDO::getOrderNo, orderNo)
            .eq(OrderDO::getShopId, shop.getId()));
        if (order == null) {
            throw new ClientException("订单不存在或无权操作");
        }
        if (!Objects.equals(order.getOrderType(), OrderTypeConstant.GOODS)
            && !Objects.equals(order.getOrderType(), OrderTypeConstant.ADOPT)) {
            throw new ClientException("当前订单不支持发货");
        }
        if (!Objects.equals(order.getOrderStatus(), OrderStatusEnum.PENDING_SHIPMENT.getCode())) {
            throw new ClientException("当前订单状态不支持发货");
        }

        int updated = baseMapper.update(null, Wrappers.lambdaUpdate(OrderDO.class)
            .eq(OrderDO::getOrderNo, order.getOrderNo())
            .eq(OrderDO::getShopId, shop.getId())
            .eq(OrderDO::getOrderStatus, OrderStatusEnum.PENDING_SHIPMENT.getCode())
            .set(OrderDO::getLogisticsCompany, StrUtil.trim(requestParam.getLogisticsCompany()))
            .set(OrderDO::getLogisticsNo, StrUtil.trim(requestParam.getLogisticsNo()))
            .set(OrderDO::getDeliveryTime, LocalDateTime.now())
            .set(OrderDO::getOrderStatus, OrderStatusEnum.SHIPPED.getCode()));
        if (!SqlHelper.retBool(updated)) {
            throw new ServiceException("订单发货失败，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveCurrentUserOrder(OrderReceiveReqDTO requestParam) {
        long userId = StpUtil.getLoginIdAsLong();
        String orderNo = StrUtil.trim(requestParam.getOrderNo());
        OrderDO order = baseMapper.selectOne(Wrappers.lambdaQuery(OrderDO.class)
            .eq(OrderDO::getOrderNo, orderNo)
            .eq(OrderDO::getUserId, userId));
        if (order == null) {
            throw new ClientException("订单不存在或无权操作");
        }
        if (!Objects.equals(order.getOrderStatus(), OrderStatusEnum.SHIPPED.getCode())) {
            throw new ClientException("当前订单状态不支持确认收货");
        }

        int updated = baseMapper.update(null, Wrappers.lambdaUpdate(OrderDO.class)
            .eq(OrderDO::getOrderNo, order.getOrderNo())
            .eq(OrderDO::getUserId, userId)
            .eq(OrderDO::getOrderStatus, OrderStatusEnum.SHIPPED.getCode())
            .set(OrderDO::getReceiveTime, LocalDateTime.now())
            .set(OrderDO::getOrderStatus, OrderStatusEnum.COMPLETED.getCode()));
        if (!SqlHelper.retBool(updated)) {
            OrderDO latestOrder = baseMapper.selectOne(Wrappers.lambdaQuery(OrderDO.class)
                .eq(OrderDO::getOrderNo, order.getOrderNo())
                .eq(OrderDO::getUserId, userId));
            if (latestOrder != null && Objects.equals(latestOrder.getOrderStatus(), OrderStatusEnum.COMPLETED.getCode())) {
                return;
            }
            throw new ServiceException("确认收货失败，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignCurrentFarmerAdoptOrder(OrderAssignAdoptReqDTO requestParam) {
        ShopRespDTO shop = getCurrentFarmerShop();
        String orderNo = StrUtil.trim(requestParam.getOrderNo());
        OrderDO order = baseMapper.selectOne(Wrappers.lambdaQuery(OrderDO.class)
            .eq(OrderDO::getOrderNo, orderNo)
            .eq(OrderDO::getShopId, shop.getId()));
        if (order == null) {
            throw new ClientException("订单不存在或无权操作");
        }
        if (!Objects.equals(order.getOrderType(), OrderTypeConstant.ADOPT)) {
            throw new ClientException("当前订单无需分配牲畜");
        }
        if (!Objects.equals(order.getOrderStatus(), OrderStatusEnum.PENDING_ASSIGNMENT.getCode())) {
            throw new ClientException("当前订单状态不支持分配牲畜");
        }

        List<OrderDetailAdoptDO> orderDetails = orderDetailAdoptMapper.selectList(
            Wrappers.lambdaQuery(OrderDetailAdoptDO.class)
                .eq(OrderDetailAdoptDO::getOrderNo, orderNo)
        );
        if (CollUtil.isEmpty(orderDetails)) {
            throw new ClientException("认养订单明细不存在");
        }

        Map<Long, Integer> requiredQuantityMap = orderDetails.stream()
            .collect(Collectors.groupingBy(
                OrderDetailAdoptDO::getAdoptItemId,
                Collectors.summingInt(OrderDetailAdoptDO::getQuantity)
            ));
        Map<Long, String> itemNameMap = orderDetails.stream()
            .collect(Collectors.toMap(
                OrderDetailAdoptDO::getAdoptItemId,
                OrderDetailAdoptDO::getItemName,
                (left, right) -> left
            ));
        Map<Long, List<Long>> assignedEarTagMap = buildAssignedEarTagMap(requestParam.getItems());

        validateAssignedEarTags(requiredQuantityMap, assignedEarTagMap, itemNameMap);

        List<AdoptInstanceCreateReqDTO> instanceList = assignedEarTagMap.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream().map(earTagNo -> {
                AdoptInstanceCreateReqDTO instance = new AdoptInstanceCreateReqDTO();
                instance.setItemId(entry.getKey());
                instance.setEarTagNo(earTagNo);
                return instance;
            }))
            .toList();

        AdoptInstanceAssignReqDTO assignReqDTO = new AdoptInstanceAssignReqDTO();
        assignReqDTO.setFarmerId(shop.getFarmerId());
        assignReqDTO.setOwnerUserId(order.getUserId());
        assignReqDTO.setOwnerOrderId(order.getId());
        assignReqDTO.setInstances(instanceList);

        var assignResult = adoptItemRemoteService.assignAdoptInstances(assignReqDTO);
        if (assignResult == null || !assignResult.isSuccess() || assignResult.getData() == null) {
            throw new ServiceException("创建养殖实例失败");
        }
        if (!Objects.equals(assignResult.getData(), instanceList.size())) {
            throw new ServiceException("养殖实例创建数量异常");
        }

        int updated = baseMapper.update(null, Wrappers.lambdaUpdate(OrderDO.class)
            .eq(OrderDO::getId, order.getId())
            .eq(OrderDO::getOrderStatus, OrderStatusEnum.PENDING_ASSIGNMENT.getCode())
            .set(OrderDO::getOrderStatus, OrderStatusEnum.BREEDING.getCode()));
        if (!SqlHelper.retBool(updated)) {
            throw new ServiceException("更新订单状态失败，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fulfillCurrentFarmerAdoptInstance(OrderFulfillAdoptReqDTO requestParam) {
        ShopRespDTO shop = getCurrentFarmerShop();
        AdoptInstanceFulfillReqDTO fulfillReqDTO = new AdoptInstanceFulfillReqDTO();
        fulfillReqDTO.setInstanceId(requestParam.getInstanceId());
        var fulfillResult = adoptItemRemoteService.fulfillAdoptInstance(fulfillReqDTO);
        if (fulfillResult == null || !fulfillResult.isSuccess() || fulfillResult.getData() == null) {
            throw new ServiceException("完成认养履约失败");
        }

        AdoptInstanceFulfillRespDTO fulfillRespDTO = fulfillResult.getData();
        Long ownerOrderId = fulfillRespDTO.getOwnerOrderId();
        if (ownerOrderId == null || ownerOrderId <= 0) {
            throw new ServiceException("养殖实例未绑定认养订单");
        }

        OrderDO order = baseMapper.selectById(ownerOrderId);
        if (order == null || !Objects.equals(order.getShopId(), shop.getId())) {
            throw new ClientException("认养订单不存在或无权操作");
        }
        if (StrUtil.isBlank(order.getOrderNo())) {
            throw new ServiceException("认养订单号不存在");
        }
        if (!Objects.equals(order.getOrderType(), OrderTypeConstant.ADOPT)) {
            throw new ClientException("当前订单无需完成履约");
        }
        if (!Boolean.TRUE.equals(fulfillRespDTO.getAllFulfilled())) {
            return;
        }
        if (Objects.equals(order.getOrderStatus(), OrderStatusEnum.COMPLETED.getCode())) {
            return;
        }
        if (!Objects.equals(order.getOrderStatus(), OrderStatusEnum.BREEDING.getCode())) {
            throw new ClientException("当前认养订单状态不支持完成履约");
        }

        int updated = baseMapper.update(null, Wrappers.lambdaUpdate(OrderDO.class)
            .eq(OrderDO::getOrderNo, order.getOrderNo())
            .eq(OrderDO::getShopId, shop.getId())
            .eq(OrderDO::getOrderStatus, OrderStatusEnum.BREEDING.getCode())
            .set(OrderDO::getOrderStatus, OrderStatusEnum.PENDING_SHIPMENT.getCode()));
        if (!SqlHelper.retBool(updated)) {
            OrderDO latestOrder = baseMapper.selectOne(Wrappers.lambdaQuery(OrderDO.class)
                .eq(OrderDO::getOrderNo, order.getOrderNo())
                .eq(OrderDO::getShopId, shop.getId()));
            if (latestOrder != null && Objects.equals(latestOrder.getOrderStatus(), OrderStatusEnum.PENDING_SHIPMENT.getCode())) {
                return;
            }
            throw new ServiceException("更新认养订单状态失败，请刷新后重试");
        }
    }

    @Override
    public List<AdoptOrderDetailRespDTO> getAdoptOrderDetail(String orderNo) {
        getAccessibleOrderByOrderNo(orderNo);
        LambdaQueryWrapper<OrderDetailAdoptDO> wrapper = Wrappers.lambdaQuery(OrderDetailAdoptDO.class)
            .eq(OrderDetailAdoptDO::getOrderNo, orderNo);
        List<OrderDetailAdoptDO> orderDetails = orderDetailAdoptMapper.selectList(wrapper);
        return orderDetails.stream().map(each -> BeanUtil.toBean(each, AdoptOrderDetailRespDTO.class)).toList();
    }

    @Override
    public List<SkuOrderDetailRespDTO> getSkuOrderDetail(String orderNo) {
        OrderDO order = getAccessibleOrderByOrderNo(orderNo);
        LambdaQueryWrapper<OrderDetailSkuDO> wrapper = Wrappers.lambdaQuery(OrderDetailSkuDO.class)
            .eq(OrderDetailSkuDO::getOrderNo, orderNo);
        List<OrderDetailSkuDO> orderDetailSkus = orderDetailSkuMapper.selectList(wrapper);
        Map<Long, OrderSkuReviewDO> reviewMap = orderSkuReviewMapper.selectList(Wrappers.lambdaQuery(OrderSkuReviewDO.class)
                .eq(OrderSkuReviewDO::getOrderNo, order.getOrderNo()))
            .stream()
            .collect(Collectors.toMap(OrderSkuReviewDO::getOrderDetailSkuId, each -> each, (left, right) -> left));
        return orderDetailSkus.stream().map(each -> buildSkuOrderDetailResp(each, reviewMap.get(each.getId()))).toList();
    }

    @Override
    public OrderLogisticsRespDTO getOrderLogistics(String orderNo) {
        OrderDO order = getAccessibleOrderByOrderNo(orderNo);
        if (StrUtil.isBlank(order.getLogisticsNo())) {
            throw new ClientException("当前订单暂无物流信息");
        }
        return aliyunLogisticsQueryService.queryOrderLogistics(order);
    }

    @Override
    public FarmerOrderStatisticsRespDTO getFarmerOrderStatistics() {
        ShopRespDTO shop = getCurrentFarmerShop();
        LocalDate today = LocalDate.now();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime last30DaysStart = today.minusDays(29).atStartOfDay();
        List<FarmerOrderTrendAggDTO> last30DaysTrendAggList = baseMapper.selectFarmerOrderTrend(
            shop.getId(),
            FARMER_STAT_EXCLUDED_ORDER_STATUSES,
            last30DaysStart,
            tomorrowStart
        );
        List<OrderDO> totalOrders = baseMapper.selectList(Wrappers.lambdaQuery(OrderDO.class)
            .select(OrderDO::getId, OrderDO::getActualPayAmount)
            .eq(OrderDO::getDelFlag, 0)
            .eq(OrderDO::getShopId, shop.getId())
            .notIn(OrderDO::getOrderStatus, FARMER_STAT_EXCLUDED_ORDER_STATUSES));
        BigDecimal totalSalesAmount = BigDecimal.ZERO;
        long totalOrderCount = 0L;
        if (CollUtil.isNotEmpty(totalOrders)) {
            totalOrderCount = totalOrders.size();
            for (OrderDO order : totalOrders) {
                totalSalesAmount = totalSalesAmount.add(safeAmount(order.getActualPayAmount()));
            }
        }
        Map<String, FarmerOrderTrendAggDTO> trendMap = new HashMap<>();
        if (CollUtil.isNotEmpty(last30DaysTrendAggList)) {
            last30DaysTrendAggList.forEach(item -> trendMap.put(item.getStatDate(), item));
        }

        List<FarmerOrderTrendPointRespDTO> trendPoints = new ArrayList<>();
        BigDecimal todaySalesAmount = BigDecimal.ZERO;
        long todayOrderCount = 0L;
        BigDecimal last7DaysSalesAmount = BigDecimal.ZERO;
        long last7DaysOrderCount = 0L;
        BigDecimal last30DaysSalesAmount = BigDecimal.ZERO;
        long last30DaysOrderCount = 0L;
        for (int i = 0; i < 30; i++) {
            LocalDate statDate = today.minusDays(29L - i);
            String statDateText = statDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            FarmerOrderTrendAggDTO trendAggDTO = trendMap.get(statDateText);
            BigDecimal dailySalesAmount = safeAmount(trendAggDTO == null ? null : trendAggDTO.getSalesAmount());
            long dailyOrderCount = safeCount(trendAggDTO == null ? null : trendAggDTO.getOrderCount());

            last30DaysSalesAmount = last30DaysSalesAmount.add(dailySalesAmount);
            last30DaysOrderCount += dailyOrderCount;
            if (!statDate.isBefore(today.minusDays(6))) {
                last7DaysSalesAmount = last7DaysSalesAmount.add(dailySalesAmount);
                last7DaysOrderCount += dailyOrderCount;
            }
            if (statDate.isEqual(today)) {
                todaySalesAmount = dailySalesAmount;
                todayOrderCount = dailyOrderCount;
            }

            trendPoints.add(FarmerOrderTrendPointRespDTO.builder()
                .statDate(statDateText)
                .salesAmount(dailySalesAmount)
                .orderCount(dailyOrderCount)
                .build());
        }

        return FarmerOrderStatisticsRespDTO.builder()
            .shopId(shop.getId())
            .shopName(shop.getShopName())
            .salesAmount(totalSalesAmount)
            .orderCount(totalOrderCount)
            .todaySalesAmount(todaySalesAmount)
            .todayOrderCount(todayOrderCount)
            .last7DaysSalesAmount(last7DaysSalesAmount)
            .last7DaysOrderCount(last7DaysOrderCount)
            .last30DaysSalesAmount(last30DaysSalesAmount)
            .last30DaysOrderCount(last30DaysOrderCount)
            .trendPoints(trendPoints)
            .build();
    }

    @Override
    public List<OrderSimpleRespDTO> listSimpleOrdersByIds(List<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        List<Long> distinctOrderIds = orderIds.stream()
            .filter(Objects::nonNull)
            .filter(each -> each > 0)
            .distinct()
            .toList();
        if (CollUtil.isEmpty(distinctOrderIds)) {
            return Collections.emptyList();
        }
        List<OrderDO> orders = baseMapper.selectBatchIds(distinctOrderIds);
        if (CollUtil.isEmpty(orders)) {
            return Collections.emptyList();
        }
        return orders.stream()
            .map(each -> OrderSimpleRespDTO.builder()
                .id(each.getId())
                .orderNo(each.getOrderNo())
                .orderStatus(each.getOrderStatus())
                .build())
            .toList();
    }

    private String generateOrderNo(Long userId) {
        long userTail = Math.floorMod(userId, 1_000_000L);
        return redisIdWorker.generateId("orderSN").toString() + String.format("%06d", userTail);
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private Long safeCount(Long count) {
        return count == null ? 0L : count;
    }

    private ShopRespDTO getCurrentFarmerShop() {
        var result = shopRemoteService.getMyShop();
        if (result == null || !result.isSuccess() || Objects.isNull(result.getData())) {
            throw new ServiceException("查询农户店铺信息失败");
        }
        return result.getData();
    }

    private Map<Long, List<Long>> buildAssignedEarTagMap(List<OrderAssignAdoptItemReqDTO> requestItems) {
        Map<Long, List<Long>> assignedEarTagMap = new HashMap<>();
        Set<Long> uniqueEarTags = new HashSet<>();
        for (OrderAssignAdoptItemReqDTO requestItem : requestItems) {
            Long adoptItemId = requestItem.getAdoptItemId();
            if (adoptItemId == null || adoptItemId <= 0) {
                throw new ClientException("认养项目不能为空");
            }
            List<String> rawEarTagNos = requestItem.getEarTagNos();
            if (CollUtil.isEmpty(rawEarTagNos)) {
                throw new ClientException("耳标号不能为空");
            }
            List<Long> normalizedEarTags = new ArrayList<>();
            for (String rawEarTagNo : rawEarTagNos) {
                String normalizedEarTagNo = StrUtil.trim(rawEarTagNo);
                if (StrUtil.isBlank(normalizedEarTagNo)) {
                    continue;
                }
                if (!normalizedEarTagNo.matches("\\d+")) {
                    throw new ClientException("耳标号只能填写数字");
                }
                Long earTagNo;
                try {
                    earTagNo = Long.parseLong(normalizedEarTagNo);
                } catch (NumberFormatException ex) {
                    throw new ClientException("耳标号格式不正确");
                }
                if (earTagNo <= 0) {
                    throw new ClientException("耳标号必须大于0");
                }
                if (!uniqueEarTags.add(earTagNo)) {
                    throw new ClientException("耳标号不能重复");
                }
                normalizedEarTags.add(earTagNo);
            }
            if (CollUtil.isEmpty(normalizedEarTags)) {
                throw new ClientException("耳标号不能为空");
            }
            assignedEarTagMap.computeIfAbsent(adoptItemId, unused -> new ArrayList<>())
                .addAll(normalizedEarTags);
        }
        return assignedEarTagMap;
    }

    private void validateAssignedEarTags(
        Map<Long, Integer> requiredQuantityMap,
        Map<Long, List<Long>> assignedEarTagMap,
        Map<Long, String> itemNameMap
    ) {
        if (!requiredQuantityMap.keySet().equals(assignedEarTagMap.keySet())) {
            throw new ClientException("请为订单中的每个认养项目分配耳标号");
        }
        for (Map.Entry<Long, Integer> entry : requiredQuantityMap.entrySet()) {
            Long adoptItemId = entry.getKey();
            Integer requiredQuantity = entry.getValue();
            int assignedQuantity = CollUtil.size(assignedEarTagMap.get(adoptItemId));
            if (!Objects.equals(requiredQuantity, assignedQuantity)) {
                String itemName = itemNameMap.getOrDefault(adoptItemId, "认养项目");
                throw new ClientException(itemName + "需要分配" + requiredQuantity + "个耳标号");
            }
        }
    }

    private OrderPageRespDTO buildOrderPageResp(OrderDO order) {
        OrderPageRespDTO response = BeanUtil.toBean(order, OrderPageRespDTO.class);
        response.setReceiveAddress(buildReceiveAddress(order));
        return response;
    }

    private OrderPageWithProductInfoRespDTO buildOrderPageWithProductInfoResp(
        OrderDO order,
        Map<String, List<ProductSummaryDTO>> productSummariesByOrderNo,
        Map<String, OrderReviewAggDTO> reviewAggMap
    ) {
        ShopRespDTO shop = shopRemoteService.getShopById(order.getShopId()).getData();
        long pendingReviewCount = 0L;
        boolean allReviewed = true;
        if (Objects.equals(order.getOrderType(), OrderTypeConstant.GOODS)
            && Objects.equals(order.getOrderStatus(), OrderStatusEnum.COMPLETED.getCode())) {
            OrderReviewAggDTO reviewAggDTO = reviewAggMap.get(order.getOrderNo());
            pendingReviewCount = safeCount(reviewAggDTO == null ? null : reviewAggDTO.getPendingReviewCount());
            allReviewed = pendingReviewCount <= 0;
        }
        return OrderPageWithProductInfoRespDTO.builder()
            .id(order.getId())
            .orderNo(order.getOrderNo())
            .payOrderNo(order.getPayOrderNo())
            .shopName(shop == null ? "" : shop.getShopName())
            .items(productSummariesByOrderNo.getOrDefault(order.getOrderNo(), Collections.emptyList()))
            .totalPrice(order.getTotalAmount())
            .totalAmount(order.getTotalAmount())
            .actualPayAmount(order.getActualPayAmount())
            .orderType(order.getOrderType())
            .orderStatus(order.getOrderStatus())
            .logisticsNo(order.getLogisticsNo())
            .logisticsCompany(order.getLogisticsCompany())
            .pendingReviewCount(pendingReviewCount)
            .allReviewed(allReviewed)
            .createTime(order.getCreateTime())
            .build();
    }

    private Map<String, OrderReviewAggDTO> mapOrderReviewAggByOrderNos(List<String> orderNos) {
        if (CollUtil.isEmpty(orderNos)) {
            return Collections.emptyMap();
        }
        return orderSkuReviewMapper.selectOrderReviewAggByOrderNos(orderNos).stream()
            .collect(Collectors.toMap(OrderReviewAggDTO::getOrderNo, each -> each, (left, right) -> left, LinkedHashMap::new));
    }

    private SkuOrderDetailRespDTO buildSkuOrderDetailResp(OrderDetailSkuDO orderDetailSku, OrderSkuReviewDO review) {
        SkuOrderDetailRespDTO respDTO = BeanUtil.toBean(orderDetailSku, SkuOrderDetailRespDTO.class);
        if (review == null) {
            respDTO.setReviewed(Boolean.FALSE);
            respDTO.setReviewImageUrls(Collections.emptyList());
            return respDTO;
        }
        respDTO.setReviewed(Boolean.TRUE);
        respDTO.setReviewId(review.getId());
        respDTO.setReviewScore(review.getScore());
        respDTO.setReviewContent(review.getContent());
        respDTO.setReviewImageUrls(splitImageUrls(review.getImageUrls()));
        respDTO.setReviewCreateTime(review.getCreateTime());
        return respDTO;
    }

    private List<String> splitImageUrls(String rawImageUrls) {
        if (StrUtil.isBlank(rawImageUrls)) {
            return Collections.emptyList();
        }
        return StrUtil.split(rawImageUrls, ',').stream()
            .map(StrUtil::trim)
            .filter(StrUtil::isNotBlank)
            .toList();
    }

    private String buildReceiveAddress(OrderDO order) {
        return Stream.of(
                order.getProvinceName(),
                order.getCityName(),
                order.getDistrictName(),
                order.getDetailAddress()
            )
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.joining());
    }

    private OrderDO getAccessibleOrderByOrderNo(String orderNo) {
        if (!StpUtil.isLogin()) {
            throw new ClientException("请先登录");
        }
        String normalizedOrderNo = StrUtil.trim(orderNo);
        OrderDO order = baseMapper.selectOne(Wrappers.lambdaQuery(OrderDO.class)
            .eq(OrderDO::getOrderNo, normalizedOrderNo));
        if (order == null) {
            throw new ClientException("订单不存在");
        }
        long currentUserId = StpUtil.getLoginIdAsLong();
        if (StpUtil.hasRole(UserRoleConstant.ADMIN_DESC)) {
            return order;
        }
        if (Objects.equals(order.getUserId(), currentUserId)) {
            return order;
        }
        if (StpUtil.hasRole(UserRoleConstant.FARMER_DESC)) {
            ShopRespDTO shop = getCurrentFarmerShop();
            if (shop != null && Objects.equals(shop.getId(), order.getShopId())) {
                return order;
            }
        }
        throw new ClientException("订单不存在或无权查看");
    }

    private SeckillActivityDO buildSeckillActivity(Map<String, String> map, DateTimeFormatter formatter) {
        SeckillActivityDO seckillActivity = new SeckillActivityDO();
        seckillActivity.setId(Convert.toLong(map.get("id")));
        seckillActivity.setActivityName(map.get("activityName"));
        seckillActivity.setSpuId(Convert.toLong(map.get("spuId")));
        seckillActivity.setSkuId(Convert.toLong(map.get("skuId")));
        seckillActivity.setShopId(Convert.toLong(map.get("shopId")));
        seckillActivity.setOriginalPrice(Convert.toBigDecimal(map.get("originalPrice")));
        seckillActivity.setSeckillPrice(Convert.toBigDecimal(map.get("seckillPrice")));
        seckillActivity.setTotalStock(Convert.toInt(map.get("totalStock")));
        seckillActivity.setStock(Convert.toInt(map.get("stock")));
        seckillActivity.setLockStock(Convert.toInt(map.get("lockStock")));
        seckillActivity.setLimitPerUser(Convert.toInt(map.get("limitPerUser")));
        seckillActivity.setStartTime(parseDateTime(map.get("startTime"), formatter));
        seckillActivity.setEndTime(parseDateTime(map.get("endTime"), formatter));
        seckillActivity.setStatus(Convert.toInt(map.get("status")));
        seckillActivity.setAuditStatus(Convert.toInt(map.get("auditStatus")));
        return seckillActivity;
    }

    private void rollbackSeckillCacheQuietly(Long seckillId, Long userId, Integer nums) {
        if (ObjectUtil.hasNull(seckillId, userId, nums) || nums <= 0) {
            return;
        }
        String seckillCacheKey = SECKILL_CACHE_KEY + seckillId;
        String userCountKey = SECKILL_CLAIM_RECORD + seckillId;
        DefaultRedisScript<Long> rollbackLuaScript = Singleton.get(STOCK_ROLLBACK_AND_REDUCE_USER_RECEIVE_LUA_PATH, () -> {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(STOCK_ROLLBACK_AND_REDUCE_USER_RECEIVE_LUA_PATH)));
            redisScript.setResultType(Long.class);
            return redisScript;
        });
        try {
            Long rollbackResult = stringRedisTemplate.execute(
                rollbackLuaScript,
                List.of(seckillCacheKey, userCountKey),
                String.valueOf(nums),
                String.valueOf(userId)
            );
            log.warn("秒杀缓存回滚完成，seckillId={}, userId={}, nums={}, rollbackResult={}",
                seckillId, userId, nums, rollbackResult);
        } catch (Exception ex) {
            log.error("秒杀缓存回滚异常，seckillId={}, userId={}, nums={}", seckillId, userId, nums, ex);
        }
    }

    private String normalizeOptionalText(String text) {
        String trimmed = StrUtil.trim(text);
        return StrUtil.isBlank(trimmed) ? null : trimmed;
    }

    private LocalDateTime parseDateTime(String value, DateTimeFormatter formatter) {
        if (ObjectUtil.isEmpty(value)) {
            throw new ServiceException("秒杀活动时间数据为空");
        }
        try {
            return LocalDateTime.parse(value, formatter);
        } catch (Exception ignored) {
            try {
                return LocalDateTime.parse(value);
            } catch (Exception ex) {
                throw new ServiceException("秒杀活动时间格式错误");
            }
        }
    }
}
