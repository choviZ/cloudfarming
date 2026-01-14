package com.vv.cloudfarming.shop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.shop.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.shop.dao.mapper.AdoptItemMapper;
import com.vv.cloudfarming.shop.dto.req.AdoptItemCreateReqDTO;
import com.vv.cloudfarming.shop.dto.req.AdoptItemUpdateReqDTO;
import com.vv.cloudfarming.shop.service.AdoptItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 认养项目服务实现类
 */
@Service
@RequiredArgsConstructor
public class AdoptItemServiceImpl extends ServiceImpl<AdoptItemMapper, AdoptItemDO> implements AdoptItemService {

    // 审核状态常量
    private static final Integer REVIEW_STATUS_PENDING = 0; // 待审核
    private static final Integer REVIEW_STATUS_APPROVED = 1; // 通过
    private static final Integer REVIEW_STATUS_REJECTED = 2; // 拒绝

    // 上架状态常量
    private static final Integer STATUS_ON_SHELF = 1; // 上架
    private static final Integer STATUS_OFF_SHELF = 0; // 下架

    @Override
    public Long createAdoptItem(Long userId, AdoptItemCreateReqDTO reqDTO) {
        // 构建认养项目实体
        AdoptItemDO adoptItem = new AdoptItemDO();
        adoptItem.setUserId(userId);
        adoptItem.setTitle(reqDTO.getTitle());
        adoptItem.setAnimalCategory(reqDTO.getAnimalCategory());
        adoptItem.setAdoptDays(reqDTO.getAdoptDays());
        adoptItem.setPrice(reqDTO.getPrice());
        adoptItem.setExpectedYield(reqDTO.getExpectedYield());
        adoptItem.setDescription(reqDTO.getDescription());
        adoptItem.setCoverImage(reqDTO.getCoverImage());
        adoptItem.setReviewStatus(REVIEW_STATUS_PENDING); // 创建后默认待审核
        adoptItem.setStatus(STATUS_OFF_SHELF); // 创建后默认未上架

        // 保存到数据库
        if (!this.save(adoptItem)) {
            throw new com.vv.cloudfarming.common.exception.ServiceException("创建认养项目失败");
        }

        return adoptItem.getId();
    }

    @Override
    public void updateAdoptItem(Long userId, AdoptItemUpdateReqDTO reqDTO) {
        // 查询认养项目
        AdoptItemDO adoptItem = this.getById(reqDTO.getId());
        if (adoptItem == null) {
            throw new com.vv.cloudfarming.common.exception.ClientException("认养项目不存在");
        }

        // 校验用户权限
        if (!adoptItem.getUserId().equals(userId)) {
            throw new com.vv.cloudfarming.common.exception.ClientException("没有权限修改该认养项目");
        }

        // 仅当审核状态不是通过时允许修改
        if (REVIEW_STATUS_APPROVED.equals(adoptItem.getReviewStatus())) {
            throw new com.vv.cloudfarming.common.exception.ClientException("已审核通过的认养项目不允许修改");
        }

        // 更新认养项目信息
        adoptItem.setTitle(reqDTO.getTitle());
        adoptItem.setAnimalCategory(reqDTO.getAnimalCategory());
        adoptItem.setAdoptDays(reqDTO.getAdoptDays());
        adoptItem.setPrice(reqDTO.getPrice());
        adoptItem.setExpectedYield(reqDTO.getExpectedYield());
        adoptItem.setDescription(reqDTO.getDescription());
        adoptItem.setCoverImage(reqDTO.getCoverImage());
        adoptItem.setReviewStatus(REVIEW_STATUS_PENDING); // 修改后重置为待审核

        // 保存到数据库
        if (!this.updateById(adoptItem)) {
            throw new com.vv.cloudfarming.common.exception.ServiceException("更新认养项目失败");
        }
    }

    @Override
    public void updateAdoptItemStatus(Long userId, Long adoptItemId, Integer status) {
        // 查询认养项目
        AdoptItemDO adoptItem = this.getById(adoptItemId);
        if (adoptItem == null) {
            throw new com.vv.cloudfarming.common.exception.ClientException("认养项目不存在");
        }

        // 校验用户权限
        if (!adoptItem.getUserId().equals(userId)) {
            throw new com.vv.cloudfarming.common.exception.ClientException("没有权限操作该认养项目");
        }

        // 上架时校验审核状态
        if (STATUS_ON_SHELF.equals(status) && !REVIEW_STATUS_APPROVED.equals(adoptItem.getReviewStatus())) {
            throw new com.vv.cloudfarming.common.exception.ClientException("仅审核通过的认养项目允许上架");
        }

        // 更新上架状态
        adoptItem.setStatus(status);

        // 保存到数据库
        if (!this.updateById(adoptItem)) {
            throw new com.vv.cloudfarming.common.exception.ServiceException("更新认养项目状态失败");
        }
    }

    @Override
    public void deleteAdoptItem(Long userId, Long adoptItemId) {
        // 查询认养项目
        AdoptItemDO adoptItem = this.getById(adoptItemId);
        if (adoptItem == null) {
            throw new com.vv.cloudfarming.common.exception.ClientException("认养项目不存在");
        }

        // 校验用户权限
        if (!adoptItem.getUserId().equals(userId)) {
            throw new com.vv.cloudfarming.common.exception.ClientException("没有权限删除该认养项目");
        }

        // 这里可以添加已被领养的校验逻辑，目前需求中未提供领养相关表结构，暂不实现
        // TODO: 后续添加已被领养的校验

        // 逻辑删除认养项目
        if (!this.removeById(adoptItemId)) {
            throw new com.vv.cloudfarming.common.exception.ServiceException("删除认养项目失败");
        }
    }
}
