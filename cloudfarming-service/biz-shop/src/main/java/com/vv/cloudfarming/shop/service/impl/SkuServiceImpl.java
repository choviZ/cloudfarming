package com.vv.cloudfarming.shop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.shop.dao.entity.*;
import com.vv.cloudfarming.shop.dao.mapper.SkuAttrValueMapper;
import com.vv.cloudfarming.shop.dao.mapper.SkuMapper;
import com.vv.cloudfarming.shop.dao.mapper.SpuMapper;
import com.vv.cloudfarming.shop.dto.SaleAttrDTO;
import com.vv.cloudfarming.shop.dto.SkuItemDTO;
import com.vv.cloudfarming.shop.dto.req.SkuCreateReqDTO;
import com.vv.cloudfarming.shop.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.shop.service.AttributeService;
import com.vv.cloudfarming.shop.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
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
                if (!SqlHelper.retBool(inserted)){
                    throw new ServiceException("sku属性持久化失败");
                }
            });
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
    public boolean lockStock(Long skuId, Integer count) {
        String key = STOCK_CACHE_KEY_PREFIX + skuId;
        String script = "if (redis.call('exists', KEYS[1]) == 1) then " +
                "    local stock = tonumber(redis.call('get', KEYS[1])); " +
                "    local num = tonumber(ARGV[1]); " +
                "    if (stock >= num) then " +
                "        return redis.call('decrby', KEYS[1], num); " +
                "    end; " +
                "    return -1; " +
                "end; " +
                "return -2;";
        
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = stringRedisTemplate.execute(redisScript, Collections.singletonList(key), String.valueOf(count));
        
        if (result == -2) {
            // 缓存不存在，尝试从数据库加载并设置缓存
            SkuDO sku = this.getById(skuId);
            if (sku != null && sku.getStatus() == 1) {
                stringRedisTemplate.opsForValue().set(key, sku.getStock().toString());
                // 再次尝试扣减
                return lockStock(skuId, count);
            }
            throw new ClientException("商品未上架或库存异常");
        }
        if (result == -1) {
            throw new ClientException("库存不足");
        }
        return result >= 0;
    }

    @Override
    public boolean unlockStock(Long skuId, Integer count) {
        String key = STOCK_CACHE_KEY_PREFIX + skuId;
        // 如果key存在，则增加；如果不存在，说明可能已下架，此时只更新数据库即可（异步或定时任务）
        // 这里简化处理：只操作Redis。如果Redis key不存在，说明无需回滚缓存（下次上架会重置）
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            stringRedisTemplate.opsForValue().increment(key, count);
        }
        // TODO: 最好发送消息队列异步更新数据库库存，保持最终一致性
        return true;
    }

    /**
     * 转换成响应DTO
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
