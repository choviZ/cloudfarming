package com.vv.cloudfarming.order.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderItemDO;
import com.vv.cloudfarming.order.dao.mapper.OrderItemMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dto.ReceiveInfoDTO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.order.service.OrderService;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.shop.dto.resp.ProductRespDTO;
import com.vv.cloudfarming.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务实现层
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderDO> implements OrderService {

    private final ProductService productService;
    private final OrderItemMapper orderItemMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private static final String STOCK_CACHE_KEY_PREFIX = "cloudfarming:stock:";

    @Transactional
    @Override
    public void createOrder(OrderCreateReqDTO requestParam) {
        // 参数校验
        Long productId = requestParam.getProductId();
        Integer quantity = requestParam.getQuantity();
        ReceiveInfoDTO receiveInfo = requestParam.getReceiveInfo();
        String remark = requestParam.getRemark();
        ProductRespDTO product = productService.getProductById(productId);
        if (product == null) {
            throw new ClientException("商品不存在");
        }
        // 锁定库存 - 使用Redis原子操作确保库存一致性
        String stockCacheKey = STOCK_CACHE_KEY_PREFIX + productId;
        String stock = stringRedisTemplate.opsForValue().get(stockCacheKey);
        if (stock == null){
            // 缓存不存在，构建缓存
            stringRedisTemplate.opsForValue().set(stockCacheKey,product.getStock().toString());
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

            orderDO.setOrderStatus(OrderStatusEnum.UNPAID.getCode());
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
            orderItemDO.setOrderStatus(OrderStatusEnum.UNPAID.getCode());
            int insertedOrderItem = orderItemMapper.insert(orderItemDO);
            if (insertedOrderItem != 1) {
                throw new ServiceException("订单创建失败");
            }
            // TODO 延迟队列处理订单超时
        } catch (ServiceException e) {
            // 订单创建失败，释放库存锁
            stringRedisTemplate.opsForValue().increment(stockCacheKey, quantity);
            // 重新抛出异常
            throw e;
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
