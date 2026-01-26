package com.vv.cloudfarming.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.order.dao.entity.PayOrderDO;
import com.vv.cloudfarming.order.dto.req.PayOrderCreateReqDTO;

/**
 * 支付相关服务
 */
public interface PayService extends IService<PayOrderDO> {

    /**
     * 创建支付单
     */
    PayOrderDO createPayOrder(PayOrderCreateReqDTO requestParam);
}
