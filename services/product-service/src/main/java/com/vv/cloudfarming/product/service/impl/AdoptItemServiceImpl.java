package com.vv.cloudfarming.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.enums.ReviewStatusEnum;
import com.vv.cloudfarming.common.enums.ShelfStatusEnum;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.product.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.product.dao.mapper.AdoptItemMapper;
import com.vv.cloudfarming.product.dao.mapper.ShopMapper;
import com.vv.cloudfarming.product.dto.req.AdoptItemCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptItemPageReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptItemReviewReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptItemUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptItemRespDTO;
import com.vv.cloudfarming.product.enums.ProductTypeEnum;
import com.vv.cloudfarming.product.service.AdoptItemService;
import com.vv.cloudfarming.product.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 认养项目服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdoptItemServiceImpl extends ServiceImpl<AdoptItemMapper, AdoptItemDO> implements AdoptItemService {

    private final ShopMapper shopMapper;
    private final StockService stockService;

    @Override
    public Long createAdoptItem(Long userId, AdoptItemCreateReqDTO requestParam) {
        Long shopId = shopMapper.getIdByFarmerId(userId);
        if (shopId == null) {
            throw new ServiceException("店铺不存在");
        }
        Integer totalCount = requestParam.getTotalCount();
        // 构建认养项目实体
        AdoptItemDO adoptItem = AdoptItemDO.builder()
                .shopId(shopId)
                .title(requestParam.getTitle())
                .animalCategory(requestParam.getAnimalCategory())
                .adoptDays(requestParam.getAdoptDays())
                .price(requestParam.getPrice())
                .expectedYield(requestParam.getExpectedYield())
                .description(requestParam.getDescription())
                .coverImage(requestParam.getCoverImage())
                .reviewStatus(ReviewStatusEnum.PENDING.getStatus()) // 创建后默认待审核
                .status(ShelfStatusEnum.OFFLINE.getCode())            // 创建后默认未上架
                .totalCount(totalCount)
                .availableCount(totalCount)
                .build();
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
        if (!adoptItem.getShopId().equals(userId)) {
            throw new ClientException("没有权限修改该认养项目");
        }

        // 仅当审核状态不是通过时允许修改
        if (ReviewStatusEnum.APPROVED.getStatus().equals(adoptItem.getReviewStatus())) {
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
        adoptItem.setReviewStatus(ReviewStatusEnum.PENDING.getStatus()); // 修改后重置为待审核

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
        if (!adoptItem.getShopId().equals(userId)) {
            throw new ClientException("没有权限操作该认养项目");
        }

        // 上架时校验审核状态
        if (ShelfStatusEnum.ONLINE.getCode().equals(status) && !ReviewStatusEnum.APPROVED.getStatus().equals(adoptItem.getReviewStatus())) {
            throw new ClientException("仅审核通过的认养项目允许上架");
        }

        // 更新上架状态
        adoptItem.setStatus(status);

        // 保存到数据库
        if (!this.updateById(adoptItem)) {
            throw new ServiceException("更新认养项目状态失败");
        }
        // 设置状态为上架时，初始化库存缓存
        if (ShelfStatusEnum.ONLINE.getCode().equals(status)) {
            stockService.initStock(adoptItemId, adoptItem.getAvailableCount(), ProductTypeEnum.ADOPT.getCode());
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
        if (!adoptItem.getShopId().equals(userId)) {
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
            isOwner = userId == null ? false : adoptItem.getShopId().equals(userId);
        }
        if (!isOwner && !ReviewStatusEnum.APPROVED.getStatus().equals(adoptItem.getReviewStatus())) {
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
    public IPage<AdoptItemRespDTO> pageAdoptItems(AdoptItemPageReqDTO requestParam) {
        String animalCategory = requestParam.getAnimalCategory();
        String title = requestParam.getTitle();
        Integer reviewStatus = requestParam.getReviewStatus();
        Integer status = requestParam.getStatus();
        Long userId = requestParam.getUserId();
        // 构建查询条件
        LambdaQueryWrapper<AdoptItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(StrUtil.isNotBlank(animalCategory), AdoptItemDO::getAnimalCategory, animalCategory) // 分类
                .like(StrUtil.isNotBlank(title), AdoptItemDO::getTitle, title) // 标题
                .eq(ObjectUtil.isNotNull(reviewStatus), AdoptItemDO::getReviewStatus, reviewStatus) // 审核状态
                .eq(ObjectUtil.isNotNull(status), AdoptItemDO::getStatus, status) // 上架状态
                .eq(ObjectUtil.isNotNull(userId), AdoptItemDO::getShopId, requestParam.getUserId()) // 用户id
                .orderByDesc(AdoptItemDO::getCreateTime)
        ;
        // 分页查询
        IPage<AdoptItemDO> pageResult = this.page(requestParam, queryWrapper);
        // 转换为响应DTO
        return pageResult.convert(adoptItem -> {
            AdoptItemRespDTO respDTO = BeanUtil.toBean(adoptItem, AdoptItemRespDTO.class);
            // 仅发布者本人可查看审核说明
            if (userId != null && userId.equals(adoptItem.getShopId())) {
                respDTO.setReviewText(adoptItem.getReviewText());
            }
            return respDTO;
        });
    }

    @Override
    public void updateReviewStatus(Long userId, AdoptItemReviewReqDTO requestParam) {
        Long id = requestParam.getId();
        Integer status = requestParam.getStatus();
        String message = requestParam.getMessage();

        AdoptItemDO item = this.getById(id);
        if (item.getStatus().equals(status)) {
            throw new ClientException("请勿重复修改状态");
        }
        item.setStatus(status);
        if (StrUtil.isNotBlank(message)) {
            item.setReviewText(message);
        }
        boolean updated = this.updateById(item);
        if (!updated) {
            throw new ServiceException("审核状态修改失败");
        }
    }
}
