package com.vv.cloudfarming.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.shop.dao.entity.SpuDO;
import com.vv.cloudfarming.shop.dto.req.SpuAttrValueCreateReqDTO;
import com.vv.cloudfarming.shop.dto.req.SpuAttrValueUpdateReqDTO;
import com.vv.cloudfarming.shop.dto.req.SpuCreateOrUpdateReqDTO;
import com.vv.cloudfarming.shop.dto.req.SpuPageQueryReqDTO;
import com.vv.cloudfarming.shop.dto.resp.SpuAttrValueRespDTO;
import com.vv.cloudfarming.shop.dto.resp.SpuRespDTO;

import java.util.List;

/**
 * SPU服务接口层
 */
public interface SpuService extends IService<SpuDO> {

    /**
     * 创建或修改SPU
     * @param requestParam 请求参数
     */
    Long saveSpu(SpuCreateOrUpdateReqDTO requestParam);

    /**
     * 根据id删除SPU
     * @param id id
     */
    void deleteSpuById(Long id);

    /**
     * 根据id获取单个SPU详情
     * @param id spuId
     * @return spu信息
     */
    SpuRespDTO getSpuById(Long id);

    /**
     * 分页查询SPU列表
     * @param queryParam 请求参数
     * @return 分页信息
     */
    IPage<SpuRespDTO> listSpuByPage(SpuPageQueryReqDTO queryParam);

    /**
     * 更新SPU状态
     * @param id     spuId
     * @param status 状态
     */
    void updateSpuStatus(Long id, Integer status);

    /**
     * 创建SPU属性值
     *
     * @param requestParam 请求参数
     */
    void createSpuAttrValue(SpuAttrValueCreateReqDTO requestParam);

    /**
     * 更新SPU属性值
     *
     * @param requestParam 请求参数
     * @return 是否成功
     */
    boolean updateSpuAttrValue(SpuAttrValueUpdateReqDTO requestParam);

    /**
     * 删除SPU属性值
     *
     * @param id 主键ID
     * @return 是否成功
     */
    boolean deleteSpuAttrValue(Long id);

    /**
     * 根据ID查询SPU属性值
     *
     * @param id 主键ID
     * @return SPU属性值信息
     */
    SpuAttrValueRespDTO getSpuAttrValueById(Long id);

    /**
     * 根据SPU ID查询属性值列表
     *
     * @param spuId SPU ID
     * @return 属性值列表
     */
    List<SpuAttrValueRespDTO> listSpuAttrValuesBySpuId(Long spuId);

    /**
     * 根据SPU ID和属性ID查询属性值
     *
     * @param spuId  SPU ID
     * @param attrId 属性ID
     * @return 属性值信息
     */
    SpuAttrValueRespDTO getSpuAttrValueBySpuIdAndAttrId(Long spuId, Long attrId);

    /**
     * 批量保存SPU属性值
     *
     * @param requestParams 请求参数列表
     */
    void batchCreateSpuAttrValues(List<SpuAttrValueCreateReqDTO> requestParams);

    /**
     * 批量删除SPU属性值
     *
     * @param ids 主键ID列表
     * @return 是否成功
     */
    boolean batchDeleteSpuAttrValues(List<Long> ids);

    /**
     * 根据SPU ID删除所有属性值
     *
     * @param spuId SPU ID
     * @return 是否成功
     */
    boolean deleteSpuAttrValuesBySpuId(Long spuId);
}
