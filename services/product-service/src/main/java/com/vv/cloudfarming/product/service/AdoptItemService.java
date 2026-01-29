package com.vv.cloudfarming.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.product.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.product.dto.req.AdoptItemCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptItemPageReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptItemReviewReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptItemUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptItemRespDTO;

/**
 * 认养项目服务接口
 */
public interface AdoptItemService extends IService<AdoptItemDO> {

    /**
     * 创建认养项目
     * 
     * @param userId 用户ID
     * @param reqDTO 创建认养项目请求DTO
     * @return 认养项目ID
     */
    Long createAdoptItem(Long userId, AdoptItemCreateReqDTO reqDTO);

    /**
     * 更新认养项目基础信息
     * 
     * @param userId 用户ID
     * @param reqDTO 更新认养项目请求DTO
     */
    void updateAdoptItem(Long userId, AdoptItemUpdateReqDTO reqDTO);

    /**
     * 上下架认养项目
     * 
     * @param userId 用户ID
     * @param adoptItemId 认养项目ID
     * @param status 上架状态：1=上架 0=下架
     */
    void updateAdoptItemStatus(Long userId, Long adoptItemId, Integer status);

    /**
     * 删除认养项目（逻辑删除）
     * 
     * @param userId 用户ID
     * @param adoptItemId 认养项目ID
     */
    void deleteAdoptItem(Long userId, Long adoptItemId);

    /**
     * 查询单个认养项目详情
     * 
     * @param userId 当前登录用户ID
     * @param adoptItemId 认养项目ID
     * @return 认养项目详情DTO
     */
    AdoptItemRespDTO getAdoptItemDetail(Long userId, Long adoptItemId);

    /**
     * 分页查询认养项目
     * 
     * @param reqDTO 分页查询请求DTO
     * @return 分页查询结果
     */
    IPage<AdoptItemRespDTO> pageAdoptItems(AdoptItemPageReqDTO reqDTO);

    /**
     * 修改审核状态
     *
     * @param userId       修改人
     * @param requestParam 请求参数
     */
    void updateReviewStatus(Long userId, AdoptItemReviewReqDTO requestParam);
}
