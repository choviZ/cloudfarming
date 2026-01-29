package com.vv.cloudfarming.order.model;

import lombok.Data;
import java.util.List;

@Data
public class GoodsOrderCreate {
    /**
     * 收货地址ID
     */
    private Long receiveId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 购买商品列表
     */
    private List<OrderItem> items;
}
