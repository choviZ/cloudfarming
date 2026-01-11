package com.vv.cloudfarming.shop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.shop.dao.entity.SkuAttrValueDO;
import com.vv.cloudfarming.shop.dao.entity.SkuDO;
import com.vv.cloudfarming.shop.dao.mapper.SkuAttrValueMapper;
import com.vv.cloudfarming.shop.dao.mapper.SkuMapper;
import com.vv.cloudfarming.shop.dto.SaleAttrDTO;
import com.vv.cloudfarming.shop.dto.SkuItemDTO;
import com.vv.cloudfarming.shop.dto.req.SkuCreateReqDTO;
import com.vv.cloudfarming.shop.service.AttributeService;
import com.vv.cloudfarming.shop.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * sku服务实现层
 */
@Service
@RequiredArgsConstructor
public class SkuServiceImpl extends ServiceImpl<SkuMapper, SkuDO> implements SkuService {

    private final AttributeService attributeService;
    private final SkuAttrValueMapper skuAttrValueMapper;

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
                if (!SqlHelper.retBool(inserted)){
                    throw new ServiceException("sku属性持久化失败");
                }
            });
        }
    }


}
