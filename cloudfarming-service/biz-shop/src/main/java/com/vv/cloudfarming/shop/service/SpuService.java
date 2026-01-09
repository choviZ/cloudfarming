package com.vv.cloudfarming.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.shop.dao.entity.SpuDO;
import com.vv.cloudfarming.shop.dto.req.SpuCreateOrUpdateReqDTO;
import com.vv.cloudfarming.shop.dto.req.SpuPageQueryReqDTO;
import com.vv.cloudfarming.shop.dto.resp.SpuRespDTO;

/**
 * SPU服务接口层
 */
public interface SpuService extends IService<SpuDO> {

    /**
     * 创建或修改SPU
     * @param requestParam 请求参数
     */
    public void saveOrUpdateSpu(SpuCreateOrUpdateReqDTO requestParam);

    /**
     * 根据id删除SPU
     * @param id id
     */
    public void deleteSpuById(Long id);

    /**
     * 根据id获取单个SPU详情
     * @param id spuId
     * @return spu信息
     */
    public SpuRespDTO getSpuById(Long id);

    /**
     * 分页查询SPU列表
     * @param queryParam 请求参数
     * @return 分页信息
     */
    public IPage<SpuRespDTO> listSpuByPage(SpuPageQueryReqDTO queryParam);

    /**
     * 更新SPU状态
     * @param id     spuId
     * @param status 状态
     */
    public void updateSpuStatus(Long id, Integer status);
}
