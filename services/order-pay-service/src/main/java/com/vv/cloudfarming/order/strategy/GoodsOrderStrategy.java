package com.vv.cloudfarming.order.strategy;

import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.model.GoodsOrderCreate;
import com.vv.cloudfarming.order.model.OrderItem;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.product.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.product.service.SkuService;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import com.vv.cloudfarming.user.service.ReceiveAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 普通商品订单创建策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoodsOrderStrategy implements OrderCreateStrategy {

    private final SkuService skuService;
    private final ReceiveAddressService receiveAddressService;
    private final OrderMapper orderMapper;
    private final OrderDetailSkuMapper orderDetailSkuMapper;

    @Override
    public List<OrderDO> createOrders(Long userId, String payOrderNo, JSONObject bizData) {
        // 1. 解析参数
        GoodsOrderCreate req = bizData.toBean(GoodsOrderCreate.class);
        validate(req);
        
        // 2. 准备数据 (SKU信息, 地址信息)
        Map<Long, SkuRespDTO> skuMap = getSkuMap(req.getItems());
        ReceiveAddressRespDTO address = receiveAddressService.getReceiveAddressById(req.getReceiveId());
        if (address == null) throw new ClientException("收货地址不存在");
        
        // 3. 锁库存 (简化处理，实际应有分布式锁或Lua脚本)
        lockStock(req.getItems());
        
        // 4. 按店铺分组
        Map<Long, List<OrderItem>> shopItemsMap = new HashMap<>();
        for (OrderItem item : req.getItems()) {
            SkuRespDTO sku = skuMap.get(item.getSkuId());
            if (sku.getShopId() == null) throw new ServiceException("商品缺少店铺信息");
            shopItemsMap.computeIfAbsent(sku.getShopId(), k -> new ArrayList<>()).add(item);
        }
        
        List<OrderDO> createdOrders = new ArrayList<>();
        
        // 5. 循环生成店铺订单
        for (Map.Entry<Long, List<OrderItem>> entry : shopItemsMap.entrySet()) {
            Long shopId = entry.getKey();
            List<OrderItem> shopItems = entry.getValue();
            
            // 5.1 计算金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItem item : shopItems) {
                SkuRespDTO sku = skuMap.get(item.getSkuId());
                BigDecimal itemAmount = sku.getPrice().multiply(new BigDecimal(item.getQuantity()));
                totalAmount = totalAmount.add(itemAmount);
            }
            
            // 5.2 创建主订单
            OrderDO order = OrderDO.builder()
                    .orderNo(generateOrderNo(userId))
                    .payOrderNo(payOrderNo)
                    .userId(userId)
                    .shopId(shopId)
                    .orderType(OrderTypeConstant.GOODS)
                    .totalAmount(totalAmount)
                    .actualPayAmount(totalAmount) // 暂无优惠计算
                    .freightAmount(BigDecimal.ZERO) // 暂无运费计算
                    .discountAmount(BigDecimal.ZERO)
                    .orderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode())
                    .receiveName(address.getReceiverName())
                    .receivePhone(address.getReceiverPhone())
                    .receiveAddress(address.getProvinceName() + address.getCityName() + address.getDistrictName() + address.getDetailAddress())
                    .build();
            
            orderMapper.insert(order);
            createdOrders.add(order);
            
            // 5.3 创建订单明细
            for (OrderItem item : shopItems) {
                SkuRespDTO sku = skuMap.get(item.getSkuId());
                BigDecimal itemAmount = sku.getPrice().multiply(new BigDecimal(item.getQuantity()));
                
                OrderDetailSkuDO detail = OrderDetailSkuDO.builder()
                        .orderId(order.getId())
                        .orderNo(order.getOrderNo())
                        .skuId(sku.getId())
                        .spuId(sku.getSpuId())
                        .skuName(sku.getSpuTitle())
                        .skuImage(sku.getSpuImage())
                        .skuSpecs(JSONUtil.toJsonStr(sku.getSaleAttrs()))
                        .price(sku.getPrice())
                        .quantity(item.getQuantity())
                        .totalAmount(itemAmount)
                        .build();
                orderDetailSkuMapper.insert(detail);
            }
        }
        
        return createdOrders;
    }
    
    private void validate(GoodsOrderCreate req) {
        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
            throw new ClientException("商品列表不能为空");
        }
        if (req.getReceiveId() == null) {
            throw new ClientException("收货地址不能为空");
        }
    }
    
    private Map<Long, SkuRespDTO> getSkuMap(List<OrderItem> items) {
        List<Long> skuIds = items.stream().map(OrderItem::getSkuId).collect(Collectors.toList());
        List<SkuRespDTO> skus = skuService.listSkuDetailsByIds(skuIds);
        if (skus.size() != skuIds.size()) {
            throw new ClientException("部分商品不存在或已下架");
        }
        return skus.stream().collect(Collectors.toMap(SkuRespDTO::getId, s -> s));
    }
    
    private void lockStock(List<OrderItem> items) {
        // 简单聚合
        Map<Long, Integer> stockMap = new HashMap<>();
        items.forEach(i -> stockMap.merge(i.getSkuId(), i.getQuantity(), Integer::sum));
        
        stockMap.forEach((skuId, qty) -> {
            boolean success = skuService.lockStock(skuId, qty);
            if (!success) {
                // TODO: 实际生产中这里应该抛出异常触发回滚，并释放已锁定的其他商品库存
                throw new ClientException("商品库存不足，SKU: " + skuId);
            }
        });
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
