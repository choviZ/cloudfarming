package com.vv.cloudfarming.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.product.dao.entity.AdoptInstanceDO;
import com.vv.cloudfarming.product.dto.req.AdoptInstancePageReqDTO;
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

}
