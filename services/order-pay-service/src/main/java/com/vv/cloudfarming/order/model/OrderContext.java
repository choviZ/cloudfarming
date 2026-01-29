package com.vv.cloudfarming.order.model;

import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailAdoptDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderContext {
    /**
     * 业务订单 (t_order)
     */
    private OrderDO order;

    /**
     * 订单明细 (t_order_detail_sku)
     */
    private List<OrderDetailSkuDO> items;

    /**
     * 认养扩展信息 (t_order_detail_adopt) - 可选
     */
    private OrderDetailAdoptDO adoptExtend;
    
    /**
     * 原始业务数据对象，用于后续处理（如库存锁定）
     */
    private Object originalBizData;
}
