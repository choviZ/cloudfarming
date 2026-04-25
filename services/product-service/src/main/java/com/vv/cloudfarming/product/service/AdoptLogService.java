package com.vv.cloudfarming.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.product.dao.entity.AdoptLogDO;
import com.vv.cloudfarming.product.dto.req.AdoptLogCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptLogPageReqDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptLogRespDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptWeightPointRespDTO;

import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 养殖实例操作服务接口
 */
public interface AdoptLogService extends IService<AdoptLogDO> {

    /**
     * 上传日志
     */
    void createAdoptLog(AdoptLogCreateReqDTO requestParam);

    /**
     * 分页查询养殖日志
     */
    IPage<AdoptLogRespDTO> pageAdoptLogs(AdoptLogPageReqDTO requestParam);

    /**
     * 查询体重变化趋势
     */
    List<AdoptWeightPointRespDTO> listWeightTrend(Long instanceId, String viewType);
}
