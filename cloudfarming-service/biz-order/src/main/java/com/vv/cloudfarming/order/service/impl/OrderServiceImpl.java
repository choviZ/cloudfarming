package com.vv.cloudfarming.order.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.utils.FormatConvertUtil;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderItemDO;
import com.vv.cloudfarming.order.dao.mapper.OrderItemMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dto.ProductInfoDTO;
import com.vv.cloudfarming.order.dto.ReceiveInfoDTO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderInfoRespDTO;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.order.mq.DelayMessageProcessor;
import com.vv.cloudfarming.order.mq.constant.MqConstant;
import com.vv.cloudfarming.order.mq.modal.MultiDelayMessage;
import com.vv.cloudfarming.order.service.OrderService;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.shop.dto.resp.ProductRespDTO;
import com.vv.cloudfarming.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务实现层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderDO> implements OrderService {

    private final ProductService productService;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private static final String STOCK_CACHE_KEY_PREFIX = "cloudfarming:stock:";
    private final int MAX_PURCHASE_LIMIT = 1000;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @Override
    public void createOrder(OrderCreateReqDTO requestParam) {
        // 参数校验
        Long productId = requestParam.getProductId();
        Integer quantity = requestParam.getQuantity();
        ReceiveInfoDTO receiveInfo = requestParam.getReceiveInfo();
        String remark = requestParam.getRemark();
        if (quantity == null || quantity <= 0) {
            throw new ClientException("购买数量必须大于0");
        }
        if (quantity > MAX_PURCHASE_LIMIT) {
            throw new ClientException("单次购买数量超过限制");
        }
        ProductRespDTO product = productService.getProductById(productId);
        if (product == null) {
            throw new ClientException("商品不存在");
        }
        // 锁定库存 - 使用Redis原子操作确保库存一致性
        String stockCacheKey = STOCK_CACHE_KEY_PREFIX + productId;
        String stock = stringRedisTemplate.opsForValue().get(stockCacheKey);
        if (stock == null) {
            RLock lock = redissonClient.getLock("cloudfarming:" + productId);
            try {
                lock.lock();
                // 缓存不存在，构建缓存,双重检查
                stock = stringRedisTemplate.opsForValue().get(stockCacheKey);
                if (stock == null) {
                    stringRedisTemplate.opsForValue().set(stockCacheKey, product.getStock().toString());
                }
            } finally {
                lock.unlock();
            }
        }
        Long newStock = stringRedisTemplate.opsForValue().decrement(stockCacheKey, quantity);
        if (newStock < 0) {
            // 库存不足，回滚操作
            stringRedisTemplate.opsForValue().increment(stockCacheKey, quantity);
            throw new ClientException("商品库存不足");
        }
        try {
            long userId = StpUtil.getLoginIdAsLong();
            // 创建主订单
            OrderDO orderDO = new OrderDO();
            orderDO.setOrderNo(generateOrderNo(userId));
            orderDO.setUserId(userId);
            BigDecimal amount = product.getPrice().multiply(new BigDecimal(quantity));
            orderDO.setTotalAmount(amount);
            orderDO.setPayType(0); // TODO 支付相关先模拟
            orderDO.setPayStatus(PayStatusEnum.UNPAID.getCode());

            orderDO.setReceiveName(receiveInfo.getReceiveName());
            orderDO.setReceivePhone(receiveInfo.getReceivePhone());
            orderDO.setReceiveProvince(receiveInfo.getReceiveProvince());
            orderDO.setReceiveCity(receiveInfo.getReceiveCity());
            orderDO.setReceiveDistrict(receiveInfo.getReceiveDistrict());
            orderDO.setReceiveDetail(receiveInfo.getReceiveDetail());

            orderDO.setOrderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
            orderDO.setRemark(remark);

            int inserted = baseMapper.insert(orderDO);
            if (inserted != 1) {
                throw new ServiceException("订单创建失败");
            }
            // 创建子订单
            Long farmerId = product.getCreateUser().getId();
            OrderItemDO orderItemDO = new OrderItemDO();
            orderItemDO.setItemOrderNo(generateOrderNo(farmerId));
            orderItemDO.setMainOrderId(orderDO.getId());
            orderItemDO.setMainOrderNo(orderDO.getOrderNo());
            orderItemDO.setUserId(userId);
            orderItemDO.setFarmerId(farmerId);
            orderItemDO.setProductJson(JSONUtil.toJsonStr(product));
            orderItemDO.setProductTotalQuantity(quantity);
            orderItemDO.setProductTotalAmount(amount);
            orderItemDO.setActualPayAmount(amount);
            orderItemDO.setOrderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
            int insertedOrderItem = orderItemMapper.insert(orderItemDO);
            if (insertedOrderItem != 1) {
                throw new ServiceException("订单创建失败");
            }
            //  延迟队列处理订单超时
            MultiDelayMessage<Long> msg = MultiDelayMessage.of(orderDO.getId(), 30000L, 60000L, 300000L, 850000L);
            int delay = msg.removeNextDelay().intValue();
            rabbitTemplate.convertAndSend(
                    MqConstant.DELAY_EXCHANGE,
                    MqConstant.DELAY_ORDER_ROUTING_KEY,
                    msg,
                    new DelayMessageProcessor(delay)
            );
            log.info("发送延迟消息：{}，延迟时间：{}秒",msg,delay / 1000);
        } catch (Exception e) {
            // 订单创建失败，释放库存锁
            stringRedisTemplate.opsForValue().increment(stockCacheKey, quantity);
            // 重新抛出异常
            throw e;
        }
    }

    @Override
    public OrderInfoRespDTO queryOrderById(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        // 主订单
        OrderDO orderDO = baseMapper.selectById(id);
        if (orderDO == null){
            throw new ClientException("订单不存在");
        }
        // 子订单
        List<OrderItemDO> items = orderItemMapper.selectByMainOrderId(id);
        if (items == null || items.size() == 0) {
            return null;
        }
        // 构建响应
        List<ProductInfoDTO> productList = new ArrayList<>();
        items.forEach(item -> {
            JSONObject jsonObject = new JSONObject(item.getProductJson());
            ProductInfoDTO productInfoDTO = ProductInfoDTO.builder()
                    .productId(item.getId())
                    .productName(jsonObject.getStr("productName"))
                    .productImg(jsonObject.getStr("productImg").split(",")[0])
                    .description(jsonObject.getStr("description"))
                    .price(jsonObject.getBigDecimal("price"))
                    // TODO 缺失数量字段
                    .quantity(1)
                    .build();
            productList.add(productInfoDTO);
        });
        OrderInfoRespDTO respDTO = OrderInfoRespDTO.builder()
                .id(orderDO.getId())
                .orderNo(orderDO.getOrderNo())
                .productList(productList)
                .totalAmount(orderDO.getTotalAmount())
                .discountAmount(orderDO.getDiscountAmount())
                .freightAmount(orderDO.getFreightAmount())
                .actualPayAmount(orderDO.getActualPayAmount())
                .payType(orderDO.getPayType())
                .payStatus(orderDO.getPayStatus())
                .orderStatus(orderDO.getOrderStatus())
                .createTime(FormatConvertUtil.convertToLocalDateTime(orderDO.getCreateTime()))
                .payTime(orderDO.getPayTime())
                .build();
        // 订单状态为 已完成（确认收货） 、 已取消 时设置订单结束时间
        if (orderDO.getOrderStatus().equals(OrderStatusEnum.COMPLETED.getCode())
                || orderDO.getOrderStatus().equals(OrderStatusEnum.CANCEL.getCode())) {
            respDTO.setCloseTime(FormatConvertUtil.convertToLocalDateTime(orderDO.getUpdateTime()));
        }
        return respDTO;
    }

    @Override
    public PayStatusEnum queryOrderPayStatusById(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        int payStatus = orderMapper.queryOrderPayStatusById(id);
        PayStatusEnum payStatusEnum = PayStatusEnum.getByCode(payStatus);
        if (payStatusEnum == null) {
            throw new ClientException("支付状态异常");
        }
        return payStatusEnum;
    }

    @Override
    public void updateOrderPayStatus(Long id, PayStatusEnum payStatusEnum) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        if (payStatusEnum == null){
            throw new ClientException("支付状态不能为空");
        }
        OrderDO order = baseMapper.selectById(id);
        if (order == null) {
            throw new ClientException("订单不存在");
        }
        if (order.getPayStatus().equals(payStatusEnum.getCode())) {
            return;
        }
        order.setPayStatus(payStatusEnum.getCode());
        int updated = baseMapper.updateById(order);
        if (updated < 0) {
            throw new ServiceException("更新状态失败");
        }
    }

    @Override
    public OrderStatusEnum queryOrderStatusById(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        int orderStatus = orderMapper.queryOrderStatusById(id);
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.getByCode(orderStatus);
        if (orderStatusEnum == null) {
            throw new ClientException("订单状态异常");
        }
        return orderStatusEnum;
    }

    @Override
    public void updateOrderStatus(Long id, OrderStatusEnum orderStatusEnum) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        if (orderStatusEnum == null) {
            throw new ClientException("订单状态不能为空");
        }
        OrderDO order = baseMapper.selectById(id);
        if (order == null) {
            throw new ClientException("订单不存在");
        }
        if (order.getOrderStatus().equals(orderStatusEnum.getCode())) {
            return;
        }
        order.setOrderStatus(orderStatusEnum.getCode());
        int updated = baseMapper.updateById(order);
        if (updated < 0) {
            throw new ServiceException("更新状态失败");
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo(Long userId) {
        // 1. 生成8位时间戳：秒级Unix时间戳的后8位
        long timestamp = System.currentTimeMillis() / 1000;
        String timePart = String.format("%08d", timestamp % 100000000);
        // 2. 处理用户ID后6位
        String userIdPart = String.format("%06d", userId);
        // 3. 生成4位随机数
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String randomPart = String.format("%04d", random.nextInt(10000));
        return timePart + userIdPart + randomPart;
    }
}
