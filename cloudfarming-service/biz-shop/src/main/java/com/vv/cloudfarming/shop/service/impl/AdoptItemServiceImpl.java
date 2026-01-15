package com.vv.cloudfarming.shop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.shop.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.shop.dao.mapper.AdoptItemMapper;
import com.vv.cloudfarming.shop.dto.req.AdoptItemCreateReqDTO;
import com.vv.cloudfarming.shop.dto.req.AdoptItemPageReqDTO;
import com.vv.cloudfarming.shop.dto.req.AdoptItemUpdateReqDTO;
import com.vv.cloudfarming.shop.dto.resp.AdoptItemRespDTO;
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
            throw new ServiceException("创建认养项目失败");
        }
        return adoptItem.getId();
    }

    @Override
    public void updateAdoptItem(Long userId, AdoptItemUpdateReqDTO reqDTO) {
        // 查询认养项目
        AdoptItemDO adoptItem = this.getById(reqDTO.getId());
        if (adoptItem == null) {
            throw new ClientException("认养项目不存在");
        }

        // 校验用户权限
        if (!adoptItem.getUserId().equals(userId)) {
            throw new ClientException("没有权限修改该认养项目");
        }

        // 仅当审核状态不是通过时允许修改
        if (REVIEW_STATUS_APPROVED.equals(adoptItem.getReviewStatus())) {
            throw new ClientException("已审核通过的认养项目不允许修改");
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
            throw new ServiceException("更新认养项目失败");
        }
    }

    @Override
    public void updateAdoptItemStatus(Long userId, Long adoptItemId, Integer status) {
        // 查询认养项目
        AdoptItemDO adoptItem = this.getById(adoptItemId);
        if (adoptItem == null) {
            throw new ClientException("认养项目不存在");
        }

        // 校验用户权限
        if (!adoptItem.getUserId().equals(userId)) {
            throw new ClientException("没有权限操作该认养项目");
        }

        // 上架时校验审核状态
        if (STATUS_ON_SHELF.equals(status) && !REVIEW_STATUS_APPROVED.equals(adoptItem.getReviewStatus())) {
            throw new ClientException("仅审核通过的认养项目允许上架");
        }

        // 更新上架状态
        adoptItem.setStatus(status);

        // 保存到数据库
        if (!this.updateById(adoptItem)) {
            throw new ServiceException("更新认养项目状态失败");
        }
    }

    @Override
    public void deleteAdoptItem(Long userId, Long adoptItemId) {
        // 查询认养项目
        AdoptItemDO adoptItem = this.getById(adoptItemId);
        if (adoptItem == null) {
            throw new ClientException("认养项目不存在");
        }

        // 校验用户权限
        if (!adoptItem.getUserId().equals(userId)) {
            throw new ClientException("没有权限删除该认养项目");
        }

        // 这里可以添加已被领养的校验逻辑，目前需求中未提供领养相关表结构，暂不实现
        // TODO: 后续添加已被领养的校验

        // 逻辑删除认养项目
        if (!this.removeById(adoptItemId)) {
            throw new ServiceException("删除认养项目失败");
        }
    }

    @Override
    public AdoptItemRespDTO getAdoptItemDetail(Long userId, Long adoptItemId) {
        // 使用LambdaQueryWrapper构建查询条件
        LambdaQueryWrapper<AdoptItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdoptItemDO::getId, adoptItemId);
        queryWrapper.eq(AdoptItemDO::getDelFlag, 0); // 未删除
        // 查询认养项目
        AdoptItemDO adoptItem = this.getOne(queryWrapper);
        if (adoptItem == null) {
            throw new ClientException("认养项目不存在");
        }
        // 业务规则校验：普通用户只能查看审核通过的项目
        boolean isOwner;
        if (userId == null) {
            isOwner = false;
        } else {
            isOwner = userId == null ? false : adoptItem.getUserId().equals(userId);
        }
        if (!isOwner && !REVIEW_STATUS_APPROVED.equals(adoptItem.getReviewStatus())) {
            throw new ClientException("认养项目不存在");
        }
        // 转换为响应DTO
        AdoptItemRespDTO respDTO = BeanUtil.toBean(adoptItem, AdoptItemRespDTO.class);
        // 仅发布者本人可查看审核说明
        if (isOwner) {
            respDTO.setReviewText(adoptItem.getReviewText());
        }
        return respDTO;
    }

    @Override
    public IPage<AdoptItemRespDTO> pageAdoptItems(AdoptItemPageReqDTO reqDTO) {
        // 使用LambdaQueryWrapper构建查询条件
        LambdaQueryWrapper<AdoptItemDO> queryWrapper = new LambdaQueryWrapper<>();
        // 动物分类条件
        if (reqDTO.getAnimalCategory() != null) {
            queryWrapper.eq(AdoptItemDO::getAnimalCategory, reqDTO.getAnimalCategory());
        }
        // 标题模糊查询
        if (reqDTO.getTitle() != null) {
            queryWrapper.like(AdoptItemDO::getTitle, reqDTO.getTitle());
        }
        // 未删除
        queryWrapper.eq(AdoptItemDO::getDelFlag, 0);
        // 业务规则：普通用户查询时，仅返回审核通过且已上架的项目
        boolean isMyPublish = reqDTO.getUserId() != null;
        if (!isMyPublish) {
            queryWrapper.eq(AdoptItemDO::getReviewStatus, REVIEW_STATUS_APPROVED);
            queryWrapper.eq(AdoptItemDO::getStatus, STATUS_ON_SHELF);
        } else {
            // 查询"我的发布"时，根据user_id返回自己的全部项目
            queryWrapper.eq(AdoptItemDO::getUserId, reqDTO.getUserId());
            // 审核状态条件（可选）
            if (reqDTO.getReviewStatus() != null) {
                queryWrapper.eq(AdoptItemDO::getReviewStatus, reqDTO.getReviewStatus());
            }
        }
        // 上架状态条件（可选）
        if (reqDTO.getStatus() != null) {
            queryWrapper.eq(AdoptItemDO::getStatus, reqDTO.getStatus());
        }
        // 默认按创建时间倒序排列
        queryWrapper.orderByDesc(AdoptItemDO::getCreateTime);
        // 分页查询
        IPage<AdoptItemDO> pageResult = this.page(reqDTO, queryWrapper);
        // 转换为响应DTO
        return pageResult.convert(adoptItem -> {
            AdoptItemRespDTO respDTO = BeanUtil.toBean(adoptItem, AdoptItemRespDTO.class);
            // 仅发布者本人可查看审核说明
            if (isMyPublish && reqDTO.getUserId().equals(adoptItem.getUserId())) {
                respDTO.setReviewText(adoptItem.getReviewText());
            }
            return respDTO;
        });
    }
}
