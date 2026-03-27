package com.vv.cloudfarming.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.enums.ShelfStatusEnum;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.product.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.product.dao.mapper.AdoptItemMapper;
import com.vv.cloudfarming.product.dao.mapper.ShopMapper;
import com.vv.cloudfarming.product.dto.req.AdoptItemCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptItemPageReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptItemUpdateReqDTO;
import com.vv.cloudfarming.product.dto.req.AuditSubmitReqDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptItemRespDTO;
import com.vv.cloudfarming.product.enums.AuditStatusEnum;
import com.vv.cloudfarming.product.enums.ProductTypeEnum;
import com.vv.cloudfarming.product.service.AdoptItemService;
import com.vv.cloudfarming.product.service.AuditService;
import com.vv.cloudfarming.product.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Objects;

/**
 * 认养项目服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdoptItemServiceImpl extends ServiceImpl<AdoptItemMapper, AdoptItemDO> implements AdoptItemService {

    private final ShopMapper shopMapper;
    private final StockService stockService;
    private final AuditService auditService;

    @Override
    @Transactional
    public Long createAdoptItem(Long userId, AdoptItemCreateReqDTO requestParam) {
        Long shopId = shopMapper.getIdByFarmerId(userId);
        if (shopId == null) {
            throw new ServiceException("店铺不存在");
        }

        Integer totalCount = requestParam.getTotalCount();
        AdoptItemDO adoptItem = AdoptItemDO.builder()
                .shopId(shopId)
                .title(requestParam.getTitle())
                .animalCategory(requestParam.getAnimalCategory())
                .adoptDays(requestParam.getAdoptDays())
                .price(requestParam.getPrice())
                .expectedYield(requestParam.getExpectedYield())
                .description(requestParam.getDescription())
                .coverImage(requestParam.getCoverImage())
                .auditStatus(AuditStatusEnum.PENDING.getCode())
                .status(ShelfStatusEnum.OFFLINE.getCode())
                .totalCount(totalCount)
                .availableCount(totalCount)
                .build();

        if (!this.save(adoptItem)) {
            throw new ServiceException("创建认养项目失败");
        }

        AuditSubmitReqDTO auditSubmitReqDTO = new AuditSubmitReqDTO();
        auditSubmitReqDTO.setBizId(adoptItem.getId());
        auditSubmitReqDTO.setBizType(ProductTypeEnum.ADOPT.getCode());
        auditService.submitAudit(userId, auditSubmitReqDTO);
        return adoptItem.getId();
    }

    @Override
    public void updateAdoptItem(Long userId, AdoptItemUpdateReqDTO reqDTO) {
        AdoptItemDO adoptItem = this.getById(reqDTO.getId());
        if (adoptItem == null) {
            throw new ClientException("认养项目不存在");
        }

        Long shopId = getShopIdByUserId(userId);
        if (!Objects.equals(adoptItem.getShopId(), shopId)) {
            throw new ClientException("没有权限修改该认养项目");
        }

        if (AuditStatusEnum.APPROVED.getCode().equals(adoptItem.getAuditStatus())) {
            throw new ClientException("已审核通过的认养项目不允许修改");
        }

        adoptItem.setTitle(reqDTO.getTitle());
        adoptItem.setAnimalCategory(reqDTO.getAnimalCategory());
        adoptItem.setAdoptDays(reqDTO.getAdoptDays());
        adoptItem.setPrice(reqDTO.getPrice());
        adoptItem.setExpectedYield(reqDTO.getExpectedYield());
        adoptItem.setDescription(reqDTO.getDescription());
        adoptItem.setCoverImage(reqDTO.getCoverImage());
        adoptItem.setAuditStatus(AuditStatusEnum.PENDING.getCode());

        if (!this.updateById(adoptItem)) {
            throw new ServiceException("更新认养项目失败");
        }
    }

    @Override
    public void updateAdoptItemStatus(Long userId, Long adoptItemId, Integer status) {
        AdoptItemDO adoptItem = this.getById(adoptItemId);
        if (adoptItem == null) {
            throw new ClientException("认养项目不存在");
        }

        Long shopId = getShopIdByUserId(userId);
        if (!Objects.equals(adoptItem.getShopId(), shopId)) {
            throw new ClientException("没有权限操作该认养项目");
        }

        if (ShelfStatusEnum.ONLINE.getCode().equals(status)
                && !AuditStatusEnum.APPROVED.getCode().equals(adoptItem.getAuditStatus())) {
            throw new ClientException("仅审核通过的认养项目允许上架");
        }

        adoptItem.setStatus(status);
        if (!this.updateById(adoptItem)) {
            throw new ServiceException("更新认养项目状态失败");
        }

        if (ShelfStatusEnum.ONLINE.getCode().equals(status)) {
            stockService.initStock(adoptItemId, adoptItem.getAvailableCount(), ProductTypeEnum.ADOPT.getCode());
        }
    }

    @Override
    public void deleteAdoptItem(Long userId, Long adoptItemId) {
        AdoptItemDO adoptItem = this.getById(adoptItemId);
        if (adoptItem == null) {
            throw new ClientException("认养项目不存在");
        }

        Long shopId = getShopIdByUserId(userId);
        if (!Objects.equals(adoptItem.getShopId(), shopId)) {
            throw new ClientException("没有权限删除该认养项目");
        }

        if (!this.removeById(adoptItemId)) {
            throw new ServiceException("删除认养项目失败");
        }
    }

    @Override
    public AdoptItemRespDTO getAdoptItemDetail(Long userId, Long adoptItemId) {
        LambdaQueryWrapper<AdoptItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdoptItemDO::getId, adoptItemId);
        queryWrapper.eq(AdoptItemDO::getDelFlag, 0);

        AdoptItemDO adoptItem = this.getOne(queryWrapper);
        if (adoptItem == null) {
            throw new ClientException("认养项目不存在");
        }

        Long shopId = getShopIdByUserId(userId);
        boolean isOwner = shopId != null && Objects.equals(adoptItem.getShopId(), shopId);
        if (!isOwner && !AuditStatusEnum.APPROVED.getCode().equals(adoptItem.getAuditStatus())) {
            throw new ClientException("认养项目不存在");
        }

        return BeanUtil.toBean(adoptItem, AdoptItemRespDTO.class);
    }

    @Override
    public IPage<AdoptItemRespDTO> pageAdoptItems(AdoptItemPageReqDTO requestParam) {
        String animalCategory = requestParam.getAnimalCategory();
        String title = requestParam.getTitle();
        Integer reviewStatus = requestParam.getReviewStatus();
        Integer status = requestParam.getStatus();
        Long userId = requestParam.getUserId();
        Long shopId = getShopIdByUserId(userId);

        if (ObjectUtil.isNotNull(userId) && shopId == null) {
            Page<AdoptItemRespDTO> emptyPage = new Page<>(requestParam.getCurrent(), requestParam.getSize());
            emptyPage.setRecords(Collections.emptyList());
            emptyPage.setTotal(0);
            return emptyPage;
        }

        LambdaQueryWrapper<AdoptItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(StrUtil.isNotBlank(animalCategory), AdoptItemDO::getAnimalCategory, animalCategory)
                .like(StrUtil.isNotBlank(title), AdoptItemDO::getTitle, title)
                .eq(ObjectUtil.isNotNull(reviewStatus), AdoptItemDO::getAuditStatus, reviewStatus)
                .eq(ObjectUtil.isNotNull(status), AdoptItemDO::getStatus, status)
                .eq(ObjectUtil.isNotNull(shopId), AdoptItemDO::getShopId, shopId)
                .orderByDesc(AdoptItemDO::getCreateTime);

        IPage<AdoptItemDO> pageResult = this.page(requestParam, queryWrapper);
        return pageResult.convert(adoptItem -> BeanUtil.toBean(adoptItem, AdoptItemRespDTO.class));
    }

    private Long getShopIdByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return shopMapper.getIdByFarmerId(userId);
    }
}
