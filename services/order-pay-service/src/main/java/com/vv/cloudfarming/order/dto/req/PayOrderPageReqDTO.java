package com.vv.cloudfarming.order.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.order.dao.entity.PayDO;
import lombok.Data;

/**
 * 分页获取支付单请求参数
 */
@Data
public class PayOrderPageReqDTO extends Page<PayDO> {

    /**
     * 支付单号
     */
    private String payOrderNo;

    /**
     * 买家id
     */
    private Long buyerId;

    /**
     * 支付状态
     */
    private Integer payStatus;
}
