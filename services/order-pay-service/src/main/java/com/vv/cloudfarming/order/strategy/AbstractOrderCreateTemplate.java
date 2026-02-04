package com.vv.cloudfarming.order.strategy;

import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dto.common.ItemDTO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.utils.RedisIdWorker;
import com.vv.cloudfarming.product.service.StockService;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import com.vv.cloudfarming.user.service.ReceiveAddressService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 创建订单抽象模板
 * 使用上下文对象（OrderCreateContext）传递流程数据，简化方法签名
 *
 * @param <P> 商品对象类型
 * @param <D> 订单详情对象类型
 */
@RequiredArgsConstructor
public abstract class AbstractOrderCreateTemplate<P, D> {

    protected final ReceiveAddressService addressService;
    protected final OrderMapper orderMapper;
    protected final RedisIdWorker redisIdWorker;
    protected final StockService stockService;

    /**
     * 模板方法 - 定义订单创建骨架流程
     */
    public final List<OrderDO> createOrder(Long userId, String payOrderNo, OrderCreateReqDTO req) {
        // 创建上下文对象
        OrderCreateContext<P, D> ctx = OrderCreateContext.create(userId, payOrderNo, req);

        // 1. 参数校验
        validateRequest(ctx);

        // 2. 查询收货地址
        ctx.setAddress(addressService.getReceiveAddressById(req.getReceiveId()));

        // 3. 批量查询商品信息（抽象方法，子类实现）
        ctx.setProductMap(fetchProductInfo(ctx));

        // 4. 扣减库存（抽象方法，子类实现）
        ctx.getItems().forEach(each -> stockService.lock(each.getBizId(),each.getQuantity(),each.getBizType()));

        // 5. 按店铺分组
        Map<Long, List<ItemDTO>> shopItemsMap = ctx.getItems().stream()
                .collect(Collectors.groupingBy(item -> getShopId(item, ctx)));
        ctx.setShopItemsMap(shopItemsMap);

        // 6. 遍历每个店铺创建订单
        for (Map.Entry<Long, List<ItemDTO>> entry : shopItemsMap.entrySet()) {
            // 设置当前店铺上下文
            ctx.setCurrentShopId(entry.getKey());
            ctx.setCurrentShopItems(entry.getValue());

            // 6.1 计算金额（抽象方法）
            BigDecimal totalAmount = calculateTotalAmount(ctx);

            // 6.2 构建订单（通用逻辑）
            OrderDO order = buildOrder(ctx, totalAmount);
            orderMapper.insert(order);
            ctx.setCurrentOrder(order);
            ctx.addCreatedOrder(order);

            // 6.3 构建订单明细（抽象方法，子类实现）
            List<D> details = buildOrderDetails(ctx);
            ctx.addDetails(details);
        }

        // 7. 批量保存明细（抽象方法）
        if (!ctx.getAllDetails().isEmpty()) {
            saveOrderDetails(ctx);
        }

        // 8. 后置处理钩子（可选覆写）
        afterOrderCreated(ctx);

        return ctx.getCreatedOrders();
    }

    // =================== 抽象方法（子类必须实现） ===================

    /**
     * 获取订单类型
     */
    protected abstract Integer getOrderType();

    /**
     * 参数校验
     */
    protected abstract void validateRequest(OrderCreateContext<P, D> ctx);

    /**
     * 批量查询商品信息
     */
    protected abstract Map<Long, P> fetchProductInfo(OrderCreateContext<P, D> ctx);

    /**
     * 获取商品所属店铺ID（用于分组）
     */
    protected abstract Long getShopId(ItemDTO item, OrderCreateContext<P, D> ctx);

    /**
     * 计算当前店铺订单总金额
     */
    protected abstract BigDecimal calculateTotalAmount(OrderCreateContext<P, D> ctx);

    /**
     * 构建当前店铺的订单明细
     */
    protected abstract List<D> buildOrderDetails(OrderCreateContext<P, D> ctx);

    /**
     * 批量保存明细
     */
    protected abstract void saveOrderDetails(OrderCreateContext<P, D> ctx);

    // =================== 钩子方法（可选覆写） ===================

    /**
     * 订单创建后的后置处理
     */
    protected void afterOrderCreated(OrderCreateContext<P, D> ctx) {
        // 默认空实现，子类可覆写
    }

    // =================== 通用方法 ===================

    /**
     * 构建订单实体
     */
    private OrderDO buildOrder(OrderCreateContext<P, D> ctx, BigDecimal totalAmount) {
        ReceiveAddressRespDTO address = ctx.getAddress();
        return OrderDO.builder()
                .orderNo(generateOrderNo(ctx.getUserId()))
                .payOrderNo(ctx.getPayOrderNo())
                .userId(ctx.getUserId())
                .shopId(ctx.getCurrentShopId())
                .orderType(getOrderType())
                .totalAmount(totalAmount)
                .actualPayAmount(totalAmount)
                .freightAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .orderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode())
                .receiveName(address.getReceiverName())
                .receivePhone(address.getReceiverPhone())
                .receiveAddress(buildFullAddress(address))
                .build();
    }

    private String buildFullAddress(ReceiveAddressRespDTO addr) {
        return addr.getProvinceName() + addr.getCityName()
                + addr.getDistrictName() + addr.getDetailAddress();
    }

    private String generateOrderNo(Long userId) {
        return String.valueOf(redisIdWorker.generateId("orderSN")) + (userId % 1000000);
    }
}
