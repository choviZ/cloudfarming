package com.vv.cloudfarming.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.order.dao.entity.PayOrderDO;
import com.vv.cloudfarming.order.dto.req.PayOrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.PayOrderPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.PayOrderRespDTO;

/**
 * 支付相关服务
 */
public interface PayService extends IService<PayOrderDO> {

    /**
     * 创建支付单
     */
    PayOrderDO createPayOrder(PayOrderCreateReqDTO requestParam);

    /**
     * 查询支付单列表
     */
    IPage<PayOrderRespDTO> listPayOrder(PayOrderPageReqDTO requestParam);
}
