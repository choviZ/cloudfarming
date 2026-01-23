package com.vv.cloudfarming.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.order.dao.entity.AdoptOrderDO;
import com.vv.cloudfarming.order.dto.req.AdoptOrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.AdoptOrderPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.AdoptOrderRespDTO;

/**
 * 认养订单服务接口
 */
public interface AdoptOrderService extends IService<AdoptOrderDO> {

    /**
     * 创建认养订单
     * 
     * @param userId 用户ID
     * @param reqDTO 创建认养订单请求DTO
     * @return 认养订单响应DTO
     */
    AdoptOrderRespDTO createAdoptOrder(Long userId, AdoptOrderCreateReqDTO reqDTO);

    /**
     * 查询认养订单
     * @param requestParam 请求参数
     * @return 分页列表
     */
    IPage<AdoptOrderRespDTO> pageAdoptOrders(AdoptOrderPageReqDTO requestParam);
}
