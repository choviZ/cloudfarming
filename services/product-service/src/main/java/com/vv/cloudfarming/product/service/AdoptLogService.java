package com.vv.cloudfarming.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.product.dao.entity.AdoptLogDO;
import com.vv.cloudfarming.product.dto.req.AdoptLogCreateReqDTO;

/**
 * 养殖实例操作服务接口
 */
public interface AdoptLogService extends IService<AdoptLogDO> {

    /**
     * 上传日志
     */
    void createAdoptLog(AdoptLogCreateReqDTO requestParam);
}
