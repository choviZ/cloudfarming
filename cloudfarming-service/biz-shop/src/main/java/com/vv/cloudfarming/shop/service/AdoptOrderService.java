package com.vv.cloudfarming.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.shop.dao.entity.AdoptOrderDO;
import com.vv.cloudfarming.shop.dto.req.AdoptOrderCreateReqDTO;
import com.vv.cloudfarming.shop.dto.resp.AdoptOrderRespDTO;

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
}
