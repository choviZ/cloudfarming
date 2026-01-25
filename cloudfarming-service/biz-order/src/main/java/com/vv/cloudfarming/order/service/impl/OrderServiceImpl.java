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
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderCreateRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderInfoRespDTO;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.order.service.OrderService;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.strategy.OrderCreateStrategy;
import com.vv.cloudfarming.order.strategy.OrderCreateStrategyFactory;
import com.vv.cloudfarming.shop.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.shop.service.SkuService;
import com.vv.cloudfarming.user.service.ReceiveAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final OrderCreateStrategyFactory strategyFactory;

    @Override
    @Transactional
    public OrderCreateRespDTO createOrder(OrderCreateReqDTO reqDTO) {
        long userId = StpUtil.getLoginIdAsLong();
        OrderCreateStrategy strategy = strategyFactory.getStrategy(reqDTO.getOrderType());
        return strategy.create(userId, reqDTO);
    }

    // TODO
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
