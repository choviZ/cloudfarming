package com.vv.cloudfarming.product.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.common.enums.ShelfStatusEnum;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.product.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.product.dao.entity.SpuDO;
import com.vv.cloudfarming.product.dao.mapper.AdoptItemMapper;
import com.vv.cloudfarming.product.dao.mapper.SkuMapper;
import com.vv.cloudfarming.product.dao.mapper.SpuMapper;
import com.vv.cloudfarming.product.dto.req.SearchPageReqDTO;
import com.vv.cloudfarming.product.dto.resp.SearchItemRespDTO;
import com.vv.cloudfarming.product.dto.resp.SearchPageRespDTO;
import com.vv.cloudfarming.product.enums.AnimalCategoryEnum;
import com.vv.cloudfarming.product.enums.AuditStatusEnum;
import com.vv.cloudfarming.product.enums.ProductTypeEnum;
import com.vv.cloudfarming.product.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 10L;
    private static final long MAX_SIZE = 20L;

    private final SpuMapper spuMapper;
    private final AdoptItemMapper adoptItemMapper;
    private final SkuMapper skuMapper;

    @Override
    public SearchPageRespDTO search(SearchPageReqDTO requestParam) {
        SearchPageRespDTO response = new SearchPageRespDTO();
        if (requestParam == null) {
            return response;
        }
        String keyword = StrUtil.trim(requestParam.getKeyword());
        Long current = normalizeCurrent(requestParam.getCurrent());
        Long size = normalizeSize(requestParam.getSize());
        response.setKeyword(keyword);
        response.setProductType(requestParam.getProductType());
        response.setCurrent(current);
        response.setSize(size);
        if (StrUtil.isBlank(keyword)) {
            return response;
        }
        Integer productType = requestParam.getProductType();
        if (productType == null) {
            IPage<SpuDO> spuPage = pageSpu(keyword, DEFAULT_CURRENT, size);
            IPage<AdoptItemDO> adoptPage = pageAdopt(keyword, DEFAULT_CURRENT, size);
            response.setSpuTotal(spuPage.getTotal());
            response.setSpuRecords(convertSpuRecords(spuPage.getRecords()));
            response.setAdoptTotal(adoptPage.getTotal());
            response.setAdoptRecords(convertAdoptRecords(adoptPage.getRecords()));
            response.setTotal(spuPage.getTotal() + adoptPage.getTotal());
            return response;
        }
        ProductTypeEnum productTypeEnum = ProductTypeEnum.of(productType);
        if (productTypeEnum == null) {
            throw new ClientException("商品类型错误");
        }
        switch (productTypeEnum) {
            case SPU -> {
                IPage<SpuDO> spuPage = pageSpu(keyword, current, size);
                response.setRecords(convertSpuRecords(spuPage.getRecords()));
                response.setTotal(spuPage.getTotal());
                response.setSpuTotal(spuPage.getTotal());
            }
            case ADOPT -> {
                IPage<AdoptItemDO> adoptPage = pageAdopt(keyword, current, size);
                response.setRecords(convertAdoptRecords(adoptPage.getRecords()));
                response.setTotal(adoptPage.getTotal());
                response.setAdoptTotal(adoptPage.getTotal());
            }
            default -> throw new ClientException("商品类型错误");
        }
        return response;
    }

    private IPage<SpuDO> pageSpu(String keyword, Long current, Long size) {
        LambdaQueryWrapper<SpuDO> queryWrapper = Wrappers.lambdaQuery(SpuDO.class)
                .like(StrUtil.isNotBlank(keyword), SpuDO::getTitle, keyword)
                .eq(SpuDO::getStatus, ShelfStatusEnum.ONLINE.getCode())
                .eq(SpuDO::getAuditStatus, AuditStatusEnum.APPROVED.getCode())
                .orderByDesc(SpuDO::getCreateTime);
        return spuMapper.selectPage(new Page<>(current, size), queryWrapper);
    }

    private IPage<AdoptItemDO> pageAdopt(String keyword, Long current, Long size) {
        LambdaQueryWrapper<AdoptItemDO> queryWrapper = Wrappers.lambdaQuery(AdoptItemDO.class)
                .like(StrUtil.isNotBlank(keyword), AdoptItemDO::getTitle, keyword)
                .eq(AdoptItemDO::getStatus, ShelfStatusEnum.ONLINE.getCode())
                .eq(AdoptItemDO::getAuditStatus, AuditStatusEnum.APPROVED.getCode())
                .orderByDesc(AdoptItemDO::getCreateTime);
        return adoptItemMapper.selectPage(new Page<>(current, size), queryWrapper);
    }

    private List<SearchItemRespDTO> convertSpuRecords(List<SpuDO> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream().map(each -> {
            SearchItemRespDTO item = new SearchItemRespDTO();
            item.setId(each.getId());
            item.setProductType(ProductTypeEnum.SPU.getCode());
            item.setProductTypeDesc(ProductTypeEnum.SPU.getDesc());
            item.setTitle(each.getTitle());
            item.setImage(extractFirstImage(each.getImages()));
            Double minPrice = skuMapper.queryLowestPrice(each.getId());
            item.setPrice(minPrice == null ? null : BigDecimal.valueOf(minPrice));
            item.setSubtitle("农产品");
            item.setTargetPath("/product/" + each.getId());
            return item;
        }).toList();
    }

    private List<SearchItemRespDTO> convertAdoptRecords(List<AdoptItemDO> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream().map(each -> {
            SearchItemRespDTO item = new SearchItemRespDTO();
            item.setId(each.getId());
            item.setProductType(ProductTypeEnum.ADOPT.getCode());
            item.setProductTypeDesc(ProductTypeEnum.ADOPT.getDesc());
            item.setTitle(each.getTitle());
            item.setImage(each.getCoverImage());
            item.setPrice(each.getPrice());
            item.setSubtitle(resolveAnimalCategoryName(each.getAnimalCategory()));
            item.setDescription(each.getAdoptDays() == null ? null : each.getAdoptDays() + "天认养周期");
            item.setTargetPath("/adopt/detail/" + each.getId());
            return item;
        }).toList();
    }

    private String resolveAnimalCategoryName(String animalCategory) {
        if (StrUtil.isBlank(animalCategory)) {
            return "认养项目";
        }
        return Arrays.stream(AnimalCategoryEnum.values())
                .filter(each -> each.getCode().equals(animalCategory))
                .findFirst()
                .map(AnimalCategoryEnum::getName)
                .orElse("认养项目");
    }

    private String extractFirstImage(String images) {
        if (StrUtil.isBlank(images)) {
            return null;
        }
        return Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .findFirst()
                .orElse(null);
    }

    private Long normalizeCurrent(Long current) {
        if (current == null || current < 1) {
            return DEFAULT_CURRENT;
        }
        return current;
    }

    private Long normalizeSize(Long size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
