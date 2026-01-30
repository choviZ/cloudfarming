package com.vv.cloudfarming.order.strategy;

import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dto.common.ItemDTO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 订单创建上下文
 * 用于在模板方法流程中传递和共享数据，避免方法签名中传递大量参数
 *
 * @param <P> 商品信息类型（如 SkuRespDTO / AdoptItemDO）
 * @param <D> 订单明细类型（如 OrderDetailSkuDO / OrderDetailAdoptDO）
 */
@Data
public class OrderCreateContext<P, D> {

    // 入参数据
    
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付单号
     */
    private String payOrderNo;

    /**
     * 原始请求参数
     */
    private OrderCreateReqDTO request;

    /**
     * 商品项列表（从 request 中提取，便于访问）
     */
    private List<ItemDTO> items;

    // 流程中查询/计算的数据

    /**
     * 收货地址信息
     */
    private ReceiveAddressRespDTO address;

    /**
     * 商品信息映射 Map<商品ID, 商品详情>
     */
    private Map<Long, P> productMap;

    /**
     * 按店铺分组后的商品映射 Map<店铺ID, 商品列表>
     */
    private Map<Long, List<ItemDTO>> shopItemsMap;

    // 当前处理的店铺数据（循环中使用）

    /**
     * 当前正在处理的店铺ID
     */
    private Long currentShopId;

    /**
     * 当前店铺的商品列表
     */
    private List<ItemDTO> currentShopItems;

    /**
     * 当前店铺的订单对象（已插入数据库，包含ID）
     */
    private OrderDO currentOrder;

    // 输出数据

    /**
     * 创建的所有订单
     */
    private List<OrderDO> createdOrders = new ArrayList<>();

    /**
     * 所有订单明细（待批量保存）
     */
    private List<D> allDetails = new ArrayList<>();

    // 便捷方法

    /**
     * 根据商品ID获取商品信息
     */
    public P getProduct(Long productId) {
        return productMap != null ? productMap.get(productId) : null;
    }

    /**
     * 添加已创建的订单
     */
    public void addCreatedOrder(OrderDO order) {
        this.createdOrders.add(order);
    }

    /**
     * 添加订单明细
     */
    public void addDetails(List<D> details) {
        this.allDetails.addAll(details);
    }

    /**
     * 静态工厂方法 - 创建上下文
     */
    public static <P, D> OrderCreateContext<P, D> create(Long userId, String payOrderNo, OrderCreateReqDTO request) {
        OrderCreateContext<P, D> context = new OrderCreateContext<>();
        context.setUserId(userId);
        context.setPayOrderNo(payOrderNo);
        context.setRequest(request);
        context.setItems(request.getItems());
        return context;
    }
}
