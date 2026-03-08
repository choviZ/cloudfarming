package com.vv.cloudfarming.order.mq.listener;

import cn.hutool.core.lang.Singleton;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.entity.PayDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dao.mapper.PayOrderMapper;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.order.mq.modal.SeckillOrderMessage;
import com.vv.cloudfarming.order.remote.ReceiveAddressRemoteService;
import com.vv.cloudfarming.order.remote.SkuRemoteService;
import com.vv.cloudfarming.product.dao.entity.SeckillActivityDO;
import com.vv.cloudfarming.product.dto.req.LockStockReqDTO;
import com.vv.cloudfarming.product.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.starter.idempotent.NoDuplicateConsumption;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderCreateListener {

    private static final String SECKILL_CACHE_KEY = "cloudfarming:seckill";
    private static final String SECKILL_CLAIM_RECORD = "cloudfarming:seckill:record:";
    private static final String STOCK_ROLLBACK_AND_REDUCE_USER_RECEIVE_LUA_PATH = "lua/stock_rollback_and_reduce_user_receive.lua";

    private final SkuRemoteService skuRemoteService;
    private final ReceiveAddressRemoteService receiveAddressRemoteService;
    private final StringRedisTemplate stringRedisTemplate;
    private final OrderMapper orderMapper;
    private final OrderDetailSkuMapper orderDetailSkuMapper;
    private final PayOrderMapper payOrderMapper;
    private final TransactionTemplate transactionTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "order.seckill.queue", durable = "true"),
            exchange = @Exchange(value = "order-event-exange", type = ExchangeTypes.DIRECT),
            key = "order.seckill.order"
    ))
    @NoDuplicateConsumption(
            uniqueKeyPrefix = "mq:seckill:create-order:",
            key = "#p0 == null ? 'null' : (#p0.orderNo == null ? 'null' : #p0.orderNo)",
            message = "秒杀订单消息消费中，请稍后重试"
    )
    public void createOrder(SeckillOrderMessage message) {
        LockStockReqDTO lockStockReqDTO = null;
        String orderNo = message == null ? null : message.getOrderNo();
        boolean needUnlockOnError = false;
        boolean needRollbackRedis = false;

        SeckillActivityDO seckillActivity = message == null ? null : message.getSeckillActivityDO();
        Long nums = message == null ? null : message.getNums();
        Long userId = message == null ? null : message.getUserId();
        Long receiveId = message == null ? null : message.getReceiveId();

        try {
            if (seckillActivity == null || nums == null || nums <= 0 || userId == null || receiveId == null || orderNo == null) {
                log.error("秒杀下单消息参数不完整，message={}", JSONUtil.toJsonStr(message));
                return;
            }

            Long duplicateCount = orderMapper.selectCount(Wrappers.lambdaQuery(OrderDO.class)
                    .eq(OrderDO::getOrderNo, orderNo));
            if (duplicateCount != null && duplicateCount > 0) {
                log.info("秒杀订单消息重复消费，已忽略，orderNo={}", orderNo);
                return;
            }
            needRollbackRedis = true;

            Result<ReceiveAddressRespDTO> addressResult = receiveAddressRemoteService.getReceiveAddressByIdAndUserId(receiveId, userId);
            ReceiveAddressRespDTO receiveAddress = addressResult == null ? null : addressResult.getData();
            if (receiveAddress == null) {
                rollbackSeckillCacheQuietly(seckillActivity, userId, nums, orderNo);
                log.error("获取收货地址失败，已回滚秒杀缓存，orderNo={}, receiveId={}, userId={}", orderNo, receiveId, userId);
                return;
            }

            BigDecimal amount = seckillActivity.getSeckillPrice().multiply(new BigDecimal(nums));
            Result<List<SkuRespDTO>> skuResult = skuRemoteService.listSkuDetailsByIds(List.of(seckillActivity.getSkuId()));
            List<SkuRespDTO> skuList = skuResult == null ? null : skuResult.getData();
            if (skuList == null || skuList.isEmpty()) {
                rollbackSeckillCacheQuietly(seckillActivity, userId, nums, orderNo);
                log.error("查询 SKU 详情失败，已回滚秒杀缓存，orderNo={}, skuId={}, result={}",
                        orderNo, seckillActivity.getSkuId(), JSONUtil.toJsonStr(skuResult));
                return;
            }
            SkuRespDTO skuRespDTO = skuList.get(0);

            lockStockReqDTO = new LockStockReqDTO();
            lockStockReqDTO.setId(seckillActivity.getId());
            lockStockReqDTO.setQuantity(nums.intValue());
            needUnlockOnError = true;
            Result<Integer> lockResult = skuRemoteService.lockSeckillStock(lockStockReqDTO);
            Integer updated = lockResult == null ? null : lockResult.getData();
            if (updated == null || updated == 0) {
                needUnlockOnError = false;
                rollbackSeckillCacheQuietly(seckillActivity, userId, nums, orderNo);
                log.error("锁定秒杀库存失败，已回滚秒杀缓存，orderNo={}, activityId={}, lockResult={}",
                        orderNo, seckillActivity.getId(), JSONUtil.toJsonStr(lockResult));
                return;
            }

            transactionTemplate.executeWithoutResult(status -> {
                OrderDO order = OrderDO.builder()
                        .orderNo(orderNo)
                        .userId(userId)
                        .shopId(seckillActivity.getShopId())
                        .orderType(OrderTypeConstant.GOODS)
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
                int insertedOrder = orderMapper.insert(order);

                OrderDetailSkuDO orderDetailSkuDO = OrderDetailSkuDO.builder()
                        .orderNo(order.getOrderNo())
                        .skuId(seckillActivity.getSkuId())
                        .spuId(seckillActivity.getSpuId())
                        .skuName(seckillActivity.getActivityName())
                        .skuImage(skuRespDTO.getSkuImage())
                        .skuSpecs(JSONUtil.toJsonStr(skuRespDTO.getSaleAttribute()))
                        .price(seckillActivity.getSeckillPrice())
                        .quantity(nums.intValue())
                        .totalAmount(amount)
                        .build();
                int insertedOrderDetail = orderDetailSkuMapper.insert(orderDetailSkuDO);

                PayDO payDO = PayDO.builder()
                        .payOrderNo(message.getPayNo())
                        .buyerId(userId)
                        .totalAmount(amount)
                        .payStatus(PayStatusEnum.UNPAID.getCode())
                        .bizStatus(OrderStatusEnum.PENDING_PAYMENT.getCode())
                        .payChannel(0)
                        .expireTime(LocalDateTime.now().plusMinutes(15))
                        .build();
                int insertedPayOrder = payOrderMapper.insert(payDO);

                if (insertedOrder == 0 || insertedOrderDetail == 0 || insertedPayOrder == 0) {
                    status.setRollbackOnly();
                    throw new IllegalStateException("数据库写入失败");
                }
            });

            needUnlockOnError = false;
            needRollbackRedis = false;
        } catch (Exception ex) {
            if (needUnlockOnError && lockStockReqDTO != null) {
                rollbackStockQuietly(lockStockReqDTO, orderNo);
            }
            if (needRollbackRedis) {
                rollbackSeckillCacheQuietly(seckillActivity, userId, nums, orderNo);
            }
            log.error("消费秒杀下单消息失败，orderNo={}", orderNo, ex);
        }
    }

    private void rollbackStockQuietly(LockStockReqDTO lockStockReqDTO, String orderNo) {
        try {
            Result<Integer> unlockResult = skuRemoteService.unlockSeckillStock(lockStockReqDTO);
            log.warn("秒杀订单消费失败后回滚库存，orderNo={}, result={}", orderNo, JSONUtil.toJsonStr(unlockResult));
        } catch (Exception ex) {
            log.error("回滚秒杀库存失败，orderNo={}", orderNo, ex);
        }
    }

    private void rollbackSeckillCacheQuietly(SeckillActivityDO seckillActivity, Long userId, Long nums, String orderNo) {
        if (seckillActivity == null || userId == null || nums == null || nums <= 0) {
            return;
        }
        String seckillCacheKey = SECKILL_CACHE_KEY + seckillActivity.getId();
        String userCountKey = SECKILL_CLAIM_RECORD + seckillActivity.getId();
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
            log.warn("秒杀缓存回滚完成，orderNo={}, seckillId={}, userId={}, nums={}, rollbackResult={}",
                    orderNo, seckillActivity.getId(), userId, nums, rollbackResult);
        } catch (Exception ex) {
            log.error("秒杀缓存回滚异常，orderNo={}, seckillId={}, userId={}, nums={}",
                    orderNo, seckillActivity.getId(), userId, nums, ex);
        }
    }
}