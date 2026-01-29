package com.vv.cloudfarming.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.product.dao.entity.AttributeDO;
import com.vv.cloudfarming.product.dto.req.AttributeCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.AttributeUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.AttributeRespDTO;

import java.util.List;

/**
* 针对表【t_attribute(属性表)】的数据库操作Service
*/
public interface AttributeService extends IService<AttributeDO> {

    /**
     * 创建属性
     *
     * @param requestParam 请求参数
     */
    void createAttribute(AttributeCreateReqDTO requestParam);

    /**
     * 更新属性
     *
     * @param requestParam 请求参数
     * @return 是否成功
     */
    boolean updateAttribute(AttributeUpdateReqDTO requestParam);

    /**
     * 删除属性
     *
     * @param id 属性ID
     * @return 是否成功
     */
    boolean deleteAttribute(Long id);

    /**
     * 根据ID查询属性详情
     *
     * @param id 属性ID
     * @return 属性详情
     */
    AttributeRespDTO getAttributeById(Long id);

    /**
     * 根据分类ID查询属性列表
     *
     * @param categoryId 分类ID
     * @return 属性列表
     */
    List<AttributeRespDTO> getAttributesByCategoryId(Long categoryId);

    /**
     * 根据分类ID和属性类型查询属性列表
     *
     * @param categoryId 分类ID
     * @param attrType 属性类型：0-基本 / 1-销售
     * @return 属性列表
     */
    List<AttributeRespDTO> getAttributesByCategoryIdAndType(Long categoryId, Integer attrType);

    /**
     * 批量删除属性
     *
     * @param ids 属性ID列表
     * @return 是否成功
     */
    boolean batchDeleteAttributes(List<Long> ids);
}