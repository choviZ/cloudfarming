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
import com.vv.cloudfarming.order.dto.OrderItemDTO;
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
import com.vv.cloudfarming.shop.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.shop.service.SkuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 订单服务实现层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderDO> implements OrderService {

    private final SkuService skuService;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @Override
    public void createOrder(OrderCreateReqDTO requestParam) {
        // 参数校验
        List<OrderItemDTO> items = requestParam.getItems();
        ReceiveInfoDTO receiveInfo = requestParam.getReceiveInfo();
        String remark = requestParam.getRemark();

        if (items == null || items.isEmpty()) {
            throw new ClientException("商品列表不能为空");
        }

        // 1. 批量获取SKU详情
        List<Long> skuIds = items.stream().map(OrderItemDTO::getSkuId).toList();
        List<SkuRespDTO> skuDetails = skuService.listSkuDetailsByIds(skuIds);

        if (skuDetails.size() != skuIds.size()) {
            throw new ClientException("包含不存在的商品");
        }

        Map<Long, SkuRespDTO> skuMap = skuDetails.stream()
                .collect(
                        Collectors.toMap(SkuRespDTO::getId, sku -> sku)
                );

        // 2. 校验库存 & 锁定库存
        // 简单起见，循环锁定。生产环境建议使用Lua脚本批量锁定。
        for (OrderItemDTO item : items) {
            SkuRespDTO sku = skuMap.get(item.getSkuId());
            boolean locked = skuService.lockStock(item.getSkuId(), item.getQuantity());
            if (!locked) {
                // 回滚已锁定的库存（如果有）- 这里为了简单，如果失败抛出异常，外层事务回滚可能无法自动回滚Redis。
                // 严谨做法：记录已锁定的，发生异常时手动回滚。
                // TODO: 建议优化为批量锁定
                throw new ClientException("商品库存不足或未上架: " + sku.getSpuTitle());
            }
        }

        try {
            long userId = StpUtil.getLoginIdAsLong();

            // 3. 计算总价并分组 (按店铺拆单)
            BigDecimal totalAmount = BigDecimal.ZERO;
            Map<Long, List<OrderItemDTO>> shopItemsMap = new HashMap<>();

            for (OrderItemDTO item : items) {
                SkuRespDTO sku = skuMap.get(item.getSkuId());
                BigDecimal itemAmount = sku.getPrice().multiply(new BigDecimal(item.getQuantity()));
                totalAmount = totalAmount.add(itemAmount);

                Long shopId = sku.getShopId();
                if (shopId == null) {
                    throw new ServiceException("商品数据异常：缺少店铺信息 (SKU ID: " + sku.getId() + ")");
                }
                shopItemsMap.computeIfAbsent(shopId, k -> new ArrayList<>()).add(item);
            }

            // 4. 创建主订单
            OrderDO orderDO = new OrderDO();
            orderDO.setOrderNo(generateOrderNo(userId));
            orderDO.setUserId(userId);
            orderDO.setTotalAmount(totalAmount);
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

            // 5. 创建子订单 (按店铺)
            for (java.util.Map.Entry<Long, List<OrderItemDTO>> entry : shopItemsMap.entrySet()) {
                Long shopId = entry.getKey();
                List<OrderItemDTO> shopItems = entry.getValue();

                // 计算该子订单总价和总数量
                BigDecimal subTotalAmount = BigDecimal.ZERO;
                int subTotalQuantity = 0;
                List<SkuRespDTO> productSnapshots = new ArrayList<>();

                for (OrderItemDTO item : shopItems) {
                    SkuRespDTO sku = skuMap.get(item.getSkuId());
                    BigDecimal itemAmount = sku.getPrice().multiply(new BigDecimal(item.getQuantity()));
                    subTotalAmount = subTotalAmount.add(itemAmount);
                    subTotalQuantity += item.getQuantity();
                    // 记录快照，保留下单时的数量
                    // 注意：SkuRespDTO 是引用，不能直接修改 quantity。
                    // 这里的 productJson 存的是 SkuRespDTO 列表。
                    // 但前端展示时需要知道每个 SKU 买了多少个。
                    // 方案 A：SkuRespDTO 增加 quantity 字段 (不推荐，SkuRespDTO 是商品域对象)
                    // 方案 B：创建一个专门的 Snapshot 对象，包含 SkuRespDTO + quantity
                    // 方案 C：复用 OrderCreateReqDTO 或 Map。
                    // 这里采用方案 B 的变体：使用 Map 或 包装类。
                    // 为了简化且适配 queryOrderById，我们这里可以使用 Map 或者一个新的 DTO。
                    // 鉴于 queryOrderById 只是反序列化 SkuRespDTO，如果 JSON 里多了 quantity 字段，SkuRespDTO 会忽略吗？
                    // 如果我们存 List<Map<String, Object>>，其中包含 SkuRespDTO 所有字段 + quantity。
                    // 让我们修改下 productJson 的存储结构。
                    // 以前存的是单个 SkuRespDTO (转JSON)。现在存 List。
                    // 每个元素应该是 SkuRespDTO + quantity。
                }

                // 构建带数量的快照列表
                List<Map<String, Object>> snapshots = new ArrayList<>();
                for (OrderItemDTO item : shopItems) {
                    SkuRespDTO sku = skuMap.get(item.getSkuId());
                    java.util.Map<String, Object> map = cn.hutool.core.bean.BeanUtil.beanToMap(sku);
                    map.put("buyQuantity", item.getQuantity()); // 增加购买数量字段
                    snapshots.add(map);
                }

                OrderItemDO orderItemDO = new OrderItemDO();
                orderItemDO.setItemOrderNo(generateOrderNo(shopId)); // 子订单号用商户ID生成前缀
                orderItemDO.setMainOrderId(orderDO.getId());
                orderItemDO.setMainOrderNo(orderDO.getOrderNo());
                orderItemDO.setUserId(userId);
                orderItemDO.setFarmerId(shopId);
                orderItemDO.setProductJson(JSONUtil.toJsonStr(snapshots)); // 存储列表
                orderItemDO.setProductTotalQuantity(subTotalQuantity);
                orderItemDO.setProductTotalAmount(subTotalAmount);
                orderItemDO.setActualPayAmount(subTotalAmount); // 暂无优惠分摊
                orderItemDO.setOrderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());

                int insertedOrderItem = orderItemMapper.insert(orderItemDO);
                if (insertedOrderItem != 1) {
                    throw new ServiceException("子订单创建失败");
                }
            }

            //  延迟队列处理订单超时
            MultiDelayMessage<Long> msg = MultiDelayMessage.of(orderDO.getId(), 30000L, 60000L, 300000L, 510000L);
            int delay = msg.removeNextDelay().intValue();
            rabbitTemplate.convertAndSend(
                    MqConstant.DELAY_EXCHANGE,
                    MqConstant.DELAY_ORDER_ROUTING_KEY,
                    msg,
                    new DelayMessageProcessor(delay)
            );
            log.info("发送延迟消息：{}，延迟时间：{}秒", msg, delay / 1000);
        } catch (Exception e) {
            // 订单创建失败，释放库存锁 (回滚所有商品的库存)
            for (OrderItemDTO item : items) {
                try {
                    skuService.unlockStock(item.getSkuId(), item.getQuantity());
                } catch (Exception ex) {
                    log.error("回滚库存失败: skuId={}, qty={}", item.getSkuId(), item.getQuantity(), ex);
                }
            }
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
        if (orderDO == null) {
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
            // 反序列化快照信息（现在是 List<Map<String, Object>>）
            // 因为 productJson 存储的是 List，每个元素是一个 Map，包含 SkuRespDTO 的字段 + buyQuantity
            String json = item.getProductJson();
            if (JSONUtil.isTypeJSONArray(json)) {
                List<JSONObject> snapshotList = JSONUtil.toList(json, JSONObject.class);
                for (JSONObject snapshot : snapshotList) {
                    SkuRespDTO sku = snapshot.toBean(SkuRespDTO.class);
                    Integer buyQuantity = snapshot.getInt("buyQuantity", 1); // 默认1

                    // 拼接规格信息到商品名称
                    String displayName = sku.getSpuTitle();
                    if (sku.getSaleAttrs() != null && !sku.getSaleAttrs().isEmpty()) {
                        StringBuilder specBuilder = new StringBuilder();
                        sku.getSaleAttrs().forEach((k, v) -> specBuilder.append(" ").append(v));
                        displayName += specBuilder.toString();
                    }

                    ProductInfoDTO productInfoDTO = ProductInfoDTO.builder()
                            .productId(sku.getId())
                            .productName(displayName)
                            .productImg(sku.getSpuImage())
                            .price(sku.getPrice()) // 下单时的单价
                            .quantity(buyQuantity) // 单个商品的购买数量
                            .build();
                    productList.add(productInfoDTO);
                }
            } else {
                // 兼容旧数据 (单个 SkuRespDTO) - 虽然理论上是新系统，但防御性编程
                SkuRespDTO snapshot = JSONUtil.toBean(json, SkuRespDTO.class);
                // 拼接规格信息到商品名称
                String displayName = snapshot.getSpuTitle();
                if (snapshot.getSaleAttrs() != null && !snapshot.getSaleAttrs().isEmpty()) {
                    StringBuilder specBuilder = new StringBuilder();
                    snapshot.getSaleAttrs().forEach((k, v) -> specBuilder.append(" ").append(v));
                    displayName += specBuilder.toString();
                }

                ProductInfoDTO productInfoDTO = ProductInfoDTO.builder()
                        .productId(snapshot.getId())
                        .productName(displayName)
                        .productImg(snapshot.getSpuImage())
                        .price(snapshot.getPrice())
                        .quantity(item.getProductTotalQuantity()) // 旧逻辑：子订单总数即为商品数
                        .build();
                productList.add(productInfoDTO);
            }
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
        if (payStatusEnum == null) {
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
