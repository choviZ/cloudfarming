package com.vv.cloudfarming.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.cloudfarming.common.enums.ShelfStatusEnum;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.product.dao.entity.*;
import com.vv.cloudfarming.product.dao.mapper.SkuAttrValueMapper;
import com.vv.cloudfarming.product.dao.mapper.SkuMapper;
import com.vv.cloudfarming.product.dao.mapper.SpuMapper;
import com.vv.cloudfarming.product.dto.domain.SaleAttrDTO;
import com.vv.cloudfarming.product.dto.domain.SkuItemDTO;
import com.vv.cloudfarming.product.dto.req.SkuCreateReqDTO;
import com.vv.cloudfarming.product.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.product.dto.domain.SpuPriceSummaryDTO;
import com.vv.cloudfarming.product.enums.ProductTypeEnum;
import com.vv.cloudfarming.product.service.AttributeService;
import com.vv.cloudfarming.product.service.SkuService;
import com.vv.cloudfarming.product.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * sku服务实现层
 */
@Service
@RequiredArgsConstructor
public class SkuServiceImpl extends ServiceImpl<SkuMapper, SkuDO> implements SkuService {

    private final AttributeService attributeService;
    private final SkuAttrValueMapper skuAttrValueMapper;
    private final SpuMapper spuMapper;
    private final StockService stockService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String STOCK_CACHE_KEY_PREFIX = "cloudfarming:stock:sku:";

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createSku(SkuCreateReqDTO requestParam) {
        Long spuId = requestParam.getSpuId();
        List<SaleAttrDTO> saleAttrs = requestParam.getSaleAttrs();
        List<SkuItemDTO> skuItems = requestParam.getSkuItems();
        // 基础检验
        if (CollectionUtils.isEmpty(saleAttrs)) {
            throw new RuntimeException("销售属性不能为空");
        }
        if (CollectionUtils.isEmpty(skuItems)) {
            throw new RuntimeException("SKU 列表不能为空");
        }
        // 校验 skuItem 中的 attrId 是否都存在于 saleAttrs
        Set<Long> allowedAttrIds = saleAttrs.stream()
                .map(SaleAttrDTO::getAttrId)
                .collect(Collectors.toSet());
        // 持久化sku记录
        for (SkuItemDTO skuItem : skuItems) {
            if (!allowedAttrIds.containsAll(skuItem.getAttrValues().keySet())) {
                throw new ServiceException("SKU 包含非法销售属性");
            }
            // 持久化sku
            SkuDO skuDO = new SkuDO();
            skuDO.setSpuId(spuId);
            skuDO.setPrice(skuItem.getPrice());
            skuDO.setStock(skuItem.getStock());
            skuDO.setStatus(0); // 默认下架
            boolean saved = this.save(skuDO);
            if (!saved) {
                throw new ServiceException("创建商品sku失败");
            }
            // 持久化sku属性
            Map<Long, String> attrValues = skuItem.getAttrValues();
            attrValues.forEach((attrId, attrValue) -> {
                SkuAttrValueDO skuAttrValueDO = SkuAttrValueDO.builder()
                        .skuId(skuDO.getId())
                        .attrId(attrId)
                        .attrValue(attrValue)
                        .build();
                int inserted = skuAttrValueMapper.insert(skuAttrValueDO);
                if (!SqlHelper.retBool(inserted)) {
                    throw new ServiceException("sku属性持久化失败");
                }
            });
            // 设置库存缓存
            stockService.initStock(skuDO.getId(), skuItem.getStock(), ProductTypeEnum.SPU.getCode());
        }

    }

    @Override
    public SkuRespDTO getSkuDetail(Long id) {
        SkuDO skuDO = this.getById(id);
        if (skuDO == null) {
            return null;
        }
        SkuRespDTO skuRespDTO = convertToRespDTO(skuDO);
        // 填充实时库存
        fillRealTimeStock(skuRespDTO);
        return skuRespDTO;
    }

    @Override
    public List<SkuRespDTO> getSkusBySpuId(Long spuId) {
        List<SkuDO> skuList = this.list(new LambdaQueryWrapper<SkuDO>().eq(SkuDO::getSpuId, spuId));
        if (CollUtil.isEmpty(skuList)) {
            return Collections.emptyList();
        }
        List<SkuRespDTO> result = skuList.stream()
                .map(this::convertToRespDTO)
                .collect(Collectors.toList());

        // 批量填充库存
        result.forEach(this::fillRealTimeStock);
        return result;
    }

    @Override
    public List<SkuRespDTO> listSkuDetailsByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<SkuDO> skuList = this.listByIds(ids);
        if (CollUtil.isEmpty(skuList)) {
            return Collections.emptyList();
        }
        List<SkuRespDTO> result = skuList.stream()
                .map(this::convertToRespDTO)
                .collect(Collectors.toList());
        result.forEach(this::fillRealTimeStock);
        return result;
    }

    @Override
    public List<SpuPriceSummaryDTO> listPriceSummaryBySpuIds(List<Long> spuIds) {
        if (CollUtil.isEmpty(spuIds)) {
            return Collections.emptyList();
        }

        // 查询这些 SPU 下的所有 SKU（仅需 id, spuId, price）
        List<SkuDO> skuList = this.list(new LambdaQueryWrapper<SkuDO>()
                .in(SkuDO::getSpuId, spuIds)
                .eq(SkuDO::getStatus, 1) // 仅计算上架的 SKU
                .gt(SkuDO::getStock, 0) // 仅计算有库存的 SKU (可选)
        );

        if (CollUtil.isEmpty(skuList)) {
            return Collections.emptyList();
        }

        // 按 spuId 分组
        Map<Long, List<SkuDO>> spuSkuMap = skuList.stream()
                .collect(Collectors.groupingBy(SkuDO::getSpuId));

        List<SpuPriceSummaryDTO> summaries = new ArrayList<>();
        spuSkuMap.forEach((spuId, skus) -> {
            if (CollUtil.isNotEmpty(skus)) {
                // 计算最低价、最高价
                Optional<SkuDO> minSkuOpt = skus.stream().min(Comparator.comparing(SkuDO::getPrice));
                Optional<SkuDO> maxSkuOpt = skus.stream().max(Comparator.comparing(SkuDO::getPrice));

                if (minSkuOpt.isPresent() && maxSkuOpt.isPresent()) {
                    SpuPriceSummaryDTO summary = SpuPriceSummaryDTO.builder()
                            .spuId(spuId)
                            .minPrice(minSkuOpt.get().getPrice())
                            .maxPrice(maxSkuOpt.get().getPrice())
                            .minPriceSkuId(minSkuOpt.get().getId())
                            .build();
                    summaries.add(summary);
                }
            }
        });

        return summaries;
    }

    @Override
    public void updateSkuStatus(Long id, Integer status) {
        ShelfStatusEnum newStatus = ShelfStatusEnum.of(status);
        if (newStatus == null) {
            throw new ClientException("无效的商品状态");
        }
        SkuDO sku = baseMapper.selectById(id);
        if (sku == null) {
            throw new ClientException("商品不存在");
        }
        if (Objects.equals(sku.getStatus(), sku)) {
            return;
        }
        sku.setStatus(status);
        if (baseMapper.updateById(sku) <= 0) {
            throw new ServiceException("更新商品状态失败");
        }
        // 设置的状态为上线，则尝试初始化库存缓存
        if (ShelfStatusEnum.ONLINE == newStatus) {
            stockService.initStock(sku.getId(), sku.getStock(), ProductTypeEnum.SPU.getCode());
        }
    }

    /**
     * 转换成响应DTO
     *
     * @param skuDO sku对象
     * @return 响应DTO
     */
    private SkuRespDTO convertToRespDTO(SkuDO skuDO) {
        SkuRespDTO skuRespDTO = BeanUtil.toBean(skuDO, SkuRespDTO.class);

        // 填充 SPU 信息
        SpuDO spuDO = spuMapper.selectById(skuDO.getSpuId());
        if (spuDO != null) {
            skuRespDTO.setShopId(spuDO.getShopId());
            skuRespDTO.setSpuTitle(spuDO.getTitle());
            if (StrUtil.isNotBlank(spuDO.getImages())) {
                skuRespDTO.setSpuImage(spuDO.getImages().split(",")[0]);
            }
        }

        // 填充销售属性
        List<SkuAttrValueDO> attrValues = skuAttrValueMapper.selectList(
                new LambdaQueryWrapper<SkuAttrValueDO>().eq(SkuAttrValueDO::getSkuId, skuDO.getId())
        );
        if (CollUtil.isNotEmpty(attrValues)) {
            Map<String, String> saleAttrs = new HashMap<>();
            List<Long> attrIds = attrValues.stream().map(SkuAttrValueDO::getAttrId).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(attrIds)) {
                List<AttributeDO> attributes = attributeService.listByIds(attrIds);
                Map<Long, String> attrNameMap = attributes.stream()
                        .collect(Collectors.toMap(AttributeDO::getId, AttributeDO::getName));

                for (SkuAttrValueDO av : attrValues) {
                    String attrName = attrNameMap.get(av.getAttrId());
                    if (attrName != null) {
                        saleAttrs.put(attrName, av.getAttrValue());
                    }
                }
            }
            skuRespDTO.setSaleAttrs(saleAttrs);
        }
        return skuRespDTO;
    }

    /**
     * 填充实时库存
     */
    private void fillRealTimeStock(SkuRespDTO skuRespDTO) {
        String stockStr = stringRedisTemplate.opsForValue().get(STOCK_CACHE_KEY_PREFIX + skuRespDTO.getId());
        if (StrUtil.isNotBlank(stockStr)) {
            skuRespDTO.setStock(Integer.parseInt(stockStr));
        }
    }
}
