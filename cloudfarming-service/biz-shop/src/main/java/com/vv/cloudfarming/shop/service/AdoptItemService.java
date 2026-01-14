package com.vv.cloudfarming.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.shop.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.shop.dto.req.AdoptItemCreateReqDTO;
import com.vv.cloudfarming.shop.dto.req.AdoptItemUpdateReqDTO;

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
}
