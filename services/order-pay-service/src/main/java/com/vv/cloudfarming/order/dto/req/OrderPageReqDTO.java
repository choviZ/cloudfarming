package com.vv.cloudfarming.order.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import lombok.Data;

/**
 * 分页查询订单请求参数
 */
@Data
public class OrderPageReqDTO extends Page<OrderDO> {

    /**
     * 订单id
     */
    private Long id;

    /**
     * 订单号
     */
    private String OrderNo;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 下单用户id
     */
    private Long userId;

    /**
     * 店铺id
     */
    private Long shopId;
}
