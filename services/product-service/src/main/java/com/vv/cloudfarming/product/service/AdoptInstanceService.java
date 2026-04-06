package com.vv.cloudfarming.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.product.dao.entity.AdoptInstanceDO;
import com.vv.cloudfarming.product.dto.req.AdoptInstanceAssignReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptInstanceMarkDeadReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptInstancePageReqDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptInstanceDetailRespDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptInstanceFulfillRespDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptInstanceRespDTO;

/**
 * 养殖实例操作服务接口
 */
public interface AdoptInstanceService extends IService<AdoptInstanceDO> {

    /**
     * 查询“我的养殖实例”，支持用户和农户使用
     * @return
     */
    IPage<AdoptInstanceRespDTO> queryMyAdoptInstances(AdoptInstancePageReqDTO reqDTO);

    /**
     * 查询养殖实例详情
     */
    AdoptInstanceDetailRespDTO getAdoptInstanceDetail(Long instanceId);

    /**
     * 为认养订单分配牲畜并创建养殖实例
     */
    Integer assignAdoptInstances(Long currentFarmerId, AdoptInstanceAssignReqDTO reqDTO);

    /**
     * 完成指定养殖实例的履约
     */
    AdoptInstanceFulfillRespDTO fulfillAdoptInstance(Long currentFarmerId, Long instanceId);

    /**
     * 将养殖实例标记为异常死亡
     */
    void markAdoptInstanceDead(Long currentFarmerId, AdoptInstanceMarkDeadReqDTO reqDTO);

}
