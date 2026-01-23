package com.vv.cloudfarming.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.shop.dao.entity.AdoptInstanceDO;
import com.vv.cloudfarming.shop.dto.req.AdoptInstancePageReqDTO;
import com.vv.cloudfarming.shop.dto.resp.AdoptInstanceRespDTO;

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
