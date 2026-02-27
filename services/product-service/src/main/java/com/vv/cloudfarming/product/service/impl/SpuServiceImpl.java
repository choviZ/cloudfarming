package com.vv.cloudfarming.product.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.enums.ShelfStatusEnum;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.product.constant.CacheKeyConstant;
import com.vv.cloudfarming.product.dao.entity.AttributeDO;
import com.vv.cloudfarming.product.dao.entity.SpuAttrValueDO;
import com.vv.cloudfarming.product.dao.entity.SpuDO;
import com.vv.cloudfarming.product.dao.mapper.SpuAttrValueMapper;
import com.vv.cloudfarming.product.dao.mapper.SpuMapper;
import com.vv.cloudfarming.product.dto.req.*;
import com.vv.cloudfarming.product.dto.resp.CategoryRespDTO;
import com.vv.cloudfarming.product.dto.resp.SpuAttrValueRespDTO;
import com.vv.cloudfarming.product.dto.resp.SpuRespDTO;
import com.vv.cloudfarming.product.dto.domain.SpuPriceSummaryDTO;
import com.vv.cloudfarming.product.dto.resp.ProductRespDTO;
import com.vv.cloudfarming.product.enums.AuditStatusEnum;
import com.vv.cloudfarming.product.enums.ProductTypeEnum;
import com.vv.cloudfarming.product.service.*;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * SPU服务实现层
 */
@Service
@RequiredArgsConstructor
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuDO> implements SpuService {

    private final CategoryService categoryService;
    private final SpuAttrValueMapper spuAttrValueMapper;
    private final AttributeService attributeService;
    private final SkuService skuService;
    private final AuditService auditService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    @Override
    @Transactional
    public Long saveSpu(SpuCreateReqDTO requestParam) {
        if (requestParam == null) {
            throw new ClientException("参数不能为空");
        }
        Long categoryId = requestParam.getCategoryId();
        CategoryRespDTO category = categoryService.getCategoryById(categoryId);
        if (category == null) {
            throw new ClientException("分类不存在");
        }
        // 保存
        SpuDO spu = BeanUtil.toBean(requestParam, SpuDO.class);
        spu.setStatus(ShelfStatusEnum.OFFLINE.getCode());
        spu.setAuditStatus(AuditStatusEnum.PENDING.getCode());
        boolean result = this.save(spu);
        if (!result) {
            throw new ServiceException("SPU创建或修改失败");
        }
        // 提交审核记录
        AuditSubmitReqDTO auditSubmitReqDTO = new AuditSubmitReqDTO();
        auditSubmitReqDTO.setBizId(spu.getId());
        auditSubmitReqDTO.setBizType(ProductTypeEnum.SPU.getCode());
        long userId = StpUtil.getLoginIdAsLong();
        auditService.submitAudit(userId, auditSubmitReqDTO);
        return spu.getId();
    }

    @Override
    public void deleteSpuById(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new ServiceException("SPU删除失败");
        }
    }

    @Override
    public SpuRespDTO getSpuById(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        SpuDO spuDO = this.getById(id);
        if (spuDO == null) {
            return null;
        }
        SpuRespDTO spuRespDTO = BeanUtil.toBean(spuDO, SpuRespDTO.class);
        spuRespDTO.setAttributes(JSONUtil.toJsonStr(getSpuAttributes(id)));
        return spuRespDTO;
    }

    @Override
    public ProductRespDTO getProductBySpuId(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        // 查询缓存
        String cacheKey = CacheKeyConstant.PRODUCT_SKU_DETAIL_CACHE_KEY + id;
        String cache = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isBlank(cache)) {
            RLock lock = redissonClient.getLock(CacheKeyConstant.LOCK_PRODUCT_SKU_KEY + id);
            lock.lock();
            try {
                // 双重判定锁
                String s = stringRedisTemplate.opsForValue().get(cacheKey);
                if (StrUtil.isBlank(s)) {
                    SpuDO spuDO = this.getById(id);
                    if (spuDO == null) {
                        return null;
                    }
                    SpuRespDTO spuResp = BeanUtil.toBean(spuDO, SpuRespDTO.class);
                    ProductRespDTO result = new ProductRespDTO();

                    // 1. 获取基础属性
                    LambdaQueryWrapper<SpuAttrValueDO> wrapper = Wrappers.lambdaQuery(SpuAttrValueDO.class)
                            .eq(SpuAttrValueDO::getSpuId, id);
                    List<SpuAttrValueDO> attrValues = spuAttrValueMapper.selectList(wrapper);

                    if (CollUtil.isNotEmpty(attrValues)) {
                        List<Long> attrIds = attrValues.stream().map(SpuAttrValueDO::getAttrId).collect(Collectors.toList());
                        if (CollUtil.isNotEmpty(attrIds)) {
                            Map<Long, String> attrNameMap = attributeService.listByIds(attrIds).stream()
                                    .collect(Collectors.toMap(AttributeDO::getId, AttributeDO::getName));

                            Map<String, String> baseAttrs = new HashMap<>();
                            for (SpuAttrValueDO av : attrValues) {
                                String name = attrNameMap.get(av.getAttrId());
                                if (name != null) {
                                    baseAttrs.put(name, av.getAttrValue());
                                }
                            }
                            spuResp.setAttributes(JSONUtil.toJsonStr(baseAttrs));
                        }
                        result.setProductSpu(spuResp);
                    }
                    // 2. 获取 SKU 列表
                    result.setProductSkus(skuService.getSkusBySpuId(id));
                    // 3. 构建缓存
                    stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(result), 30, TimeUnit.MINUTES);
                    return result;
                }
            } finally {
                lock.unlock();
            }
        }
        return JSONUtil.toBean(cache, ProductRespDTO.class);
    }

    @Override
    public IPage<SpuRespDTO> listSpuByPage(SpuPageQueryReqDTO queryParam) {
        if (queryParam == null) {
            throw new ClientException("参数不能为空");
        }
        Long id = queryParam.getId();
        String spuName = queryParam.getSpuName();
        Long categoryId = queryParam.getCategoryId();
        Integer status = queryParam.getStatus();
        // 构建查询条件
        LambdaQueryWrapper<SpuDO> queryWrapper = Wrappers.lambdaQuery(SpuDO.class)
                .like(!StrUtil.isBlank(spuName), SpuDO::getTitle, spuName)
                .eq(categoryId != null, SpuDO::getCategoryId, categoryId)
                .eq(status != null, SpuDO::getStatus, status);
        // 查询
        IPage<SpuDO> spuDOPage = baseMapper.selectPage(queryParam, queryWrapper);

        // 批量查询价格摘要 (避免 N+1)
        List<Long> spuIds = spuDOPage.getRecords().stream()
                .map(SpuDO::getId)
                .collect(Collectors.toList());
        List<SpuPriceSummaryDTO> priceSummaries = skuService.listPriceSummaryBySpuIds(spuIds);
        Map<Long, SpuPriceSummaryDTO> priceSummaryMap = priceSummaries.stream()
                .collect(Collectors.toMap(SpuPriceSummaryDTO::getSpuId, p -> p));

        return spuDOPage.convert(spuDO -> {
            SpuRespDTO spuRespDTO = BeanUtil.toBean(spuDO, SpuRespDTO.class);
            spuRespDTO.setAttributes(JSONUtil.toJsonStr(getSpuAttributes(spuDO.getId())));
            return spuRespDTO;
        });
    }

    @Override
    public void updateSpuStatus(Long id, Integer status) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        if (status == null) {
            throw new ClientException("状态不能为空");
        }
        SpuDO spuDO = baseMapper.selectById(id);
        if (spuDO == null) {
            throw new ClientException("SPU不存在");
        }
        if (Objects.equals(spuDO.getStatus(), status)) {
            throw new ClientException("请勿重复修改状态");
        }
        LambdaUpdateWrapper<SpuDO> updateWrapper = Wrappers.lambdaUpdate(SpuDO.class)
                .eq(SpuDO::getId, id)
                .set(SpuDO::getStatus, status);
        boolean updated = this.update(updateWrapper);
        if (!updated) {
            throw new ServiceException("SPU状态更新失败");
        }
    }

    @Override
    public void createSpuAttrValue(SpuAttrValueCreateReqDTO requestParam) {
        if (requestParam == null) {
            throw new ClientException("参数不能为空");
        }
        Long spuId = requestParam.getSpuId();
        Long attrId = requestParam.getAttrId();
        String attrValue = requestParam.getAttrValue();

        if (spuId == null || spuId <= 0) {
            throw new ClientException("SPU ID不合法");
        }
        if (attrId == null || attrId <= 0) {
            throw new ClientException("属性ID不合法");
        }
        if (StrUtil.isBlank(attrValue)) {
            throw new ClientException("属性值不能为空");
        }

        SpuDO spu = this.getById(spuId);
        if (spu == null) {
            throw new ClientException("SPU不存在");
        }

        if (spuAttrValueMapper.checkAttributeExists(attrId) == 0) {
            throw new ClientException("属性不存在");
        }

        LambdaQueryWrapper<SpuAttrValueDO> wrapper = Wrappers.lambdaQuery(SpuAttrValueDO.class)
                .eq(SpuAttrValueDO::getSpuId, spuId)
                .eq(SpuAttrValueDO::getAttrId, attrId);
        SpuAttrValueDO existing = spuAttrValueMapper.selectOne(wrapper);
        if (existing != null) {
            throw new ClientException("该SPU已存在该属性值");
        }

        SpuAttrValueDO spuAttrValue = BeanUtil.toBean(requestParam, SpuAttrValueDO.class);
        int inserted = spuAttrValueMapper.insert(spuAttrValue);
        if (inserted < 0) {
            throw new ServiceException("SPU属性值创建失败");
        }
    }

    @Override
    public boolean updateSpuAttrValue(SpuAttrValueUpdateReqDTO requestParam) {
        if (requestParam == null) {
            throw new ClientException("参数不能为空");
        }
        Long id = requestParam.getId();
        Long spuId = requestParam.getSpuId();
        Long attrId = requestParam.getAttrId();
        String attrValue = requestParam.getAttrValue();

        if (id == null || id <= 0) {
            throw new ClientException("ID不合法");
        }

        SpuAttrValueDO existing = spuAttrValueMapper.selectById(id);
        if (existing == null) {
            throw new ClientException("SPU属性值不存在");
        }

        if (ObjectUtil.isNotNull(spuId)) {
            if (this.getById(spuId) == null) {
                throw new ClientException("SPU不存在");
            }
        }

        if (ObjectUtil.isNotNull(attrId)) {
            if (spuAttrValueMapper.checkAttributeExists(attrId) == 0) {
                throw new ClientException("属性不存在");
            }
        }

        LambdaUpdateWrapper<SpuAttrValueDO> wrapper = Wrappers.lambdaUpdate(SpuAttrValueDO.class)
                .eq(SpuAttrValueDO::getId, id)
                .set(ObjectUtil.isNotNull(spuId), SpuAttrValueDO::getSpuId, spuId)
                .set(ObjectUtil.isNotNull(attrId), SpuAttrValueDO::getAttrId, attrId)
                .set(StrUtil.isNotBlank(attrValue), SpuAttrValueDO::getAttrValue, attrValue);
        int updated = spuAttrValueMapper.update(null, wrapper);
        if (updated < 0) {
            throw new ServiceException("SPU属性值更新失败");
        }
        return true;
    }

    @Override
    public boolean deleteSpuAttrValue(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("ID不合法");
        }
        SpuAttrValueDO spuAttrValue = spuAttrValueMapper.selectById(id);
        if (spuAttrValue == null) {
            throw new ClientException("SPU属性值不存在");
        }
        int deleted = spuAttrValueMapper.deleteById(id);
        if (deleted < 0) {
            throw new ServiceException("SPU属性值删除失败");
        }
        return true;
    }

    @Override
    public SpuAttrValueRespDTO getSpuAttrValueById(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("ID不合法");
        }
        SpuAttrValueDO spuAttrValueDO = spuAttrValueMapper.selectById(id);
        if (spuAttrValueDO == null) {
            return null;
        }
        return BeanUtil.toBean(spuAttrValueDO, SpuAttrValueRespDTO.class);
    }

    @Override
    public List<SpuAttrValueRespDTO> listSpuAttrValuesBySpuId(Long spuId) {
        if (spuId == null || spuId <= 0) {
            throw new ClientException("SPU ID不合法");
        }
        if (this.getById(spuId) == null) {
            throw new ClientException("SPU不存在");
        }
        LambdaQueryWrapper<SpuAttrValueDO> wrapper = Wrappers.lambdaQuery(SpuAttrValueDO.class)
                .eq(SpuAttrValueDO::getSpuId, spuId);
        List<SpuAttrValueDO> spuAttrValues = spuAttrValueMapper.selectList(wrapper);
        return spuAttrValues.stream()
                .map(spuAttrValueDO -> BeanUtil.toBean(spuAttrValueDO, SpuAttrValueRespDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public SpuAttrValueRespDTO getSpuAttrValueBySpuIdAndAttrId(Long spuId, Long attrId) {
        if (spuId == null || spuId <= 0) {
            throw new ClientException("SPU ID不合法");
        }
        if (attrId == null || attrId <= 0) {
            throw new ClientException("属性ID不合法");
        }

        if (this.getById(spuId) == null) {
            throw new ClientException("SPU不存在");
        }

        if (spuAttrValueMapper.checkAttributeExists(attrId) == 0) {
            throw new ClientException("属性不存在");
        }

        LambdaQueryWrapper<SpuAttrValueDO> wrapper = Wrappers.lambdaQuery(SpuAttrValueDO.class)
                .eq(SpuAttrValueDO::getSpuId, spuId)
                .eq(SpuAttrValueDO::getAttrId, attrId);
        SpuAttrValueDO spuAttrValue = spuAttrValueMapper.selectOne(wrapper);
        if (spuAttrValue == null) {
            return null;
        }
        return BeanUtil.toBean(spuAttrValue, SpuAttrValueRespDTO.class);
    }

    @Override
    public void batchCreateSpuAttrValues(List<SpuAttrValueCreateReqDTO> requestParams) {
        if (requestParams == null || requestParams.isEmpty()) {
            throw new ClientException("参数列表不能为空");
        }

        for (SpuAttrValueCreateReqDTO requestParam : requestParams) {
            createSpuAttrValue(requestParam);
        }
    }

    @Override
    public boolean batchDeleteSpuAttrValues(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ClientException("ID列表不能为空");
        }
        int deleted = spuAttrValueMapper.deleteBatchIds(ids);
        if (deleted < 0) {
            throw new ServiceException("批量删除SPU属性值失败");
        }
        return true;
    }

    @Override
    public boolean deleteSpuAttrValuesBySpuId(Long spuId) {
        if (spuId == null || spuId <= 0) {
            throw new ClientException("SPU ID不合法");
        }
        if (this.getById(spuId) == null) {
            throw new ClientException("SPU不存在");
        }
        LambdaQueryWrapper<SpuAttrValueDO> wrapper = Wrappers.lambdaQuery(SpuAttrValueDO.class)
                .eq(SpuAttrValueDO::getSpuId, spuId);
        int deleted = spuAttrValueMapper.delete(wrapper);
        if (deleted < 0) {
            throw new ServiceException("删除SPU属性值失败");
        }
        return true;
    }

    /**
     * 根据spuId获取相应的属性
     *
     * @param spuId spuId
     * @return 属性map
     */
    private HashMap<String, String> getSpuAttributes(Long spuId) {
        List<SpuAttrValueDO> spuAttributes = spuAttrValueMapper.getAttrValueBySpuId(spuId);
        HashMap<String, String> hashMap = new HashMap<>();
        if (spuAttributes != null) {
            // TODO 待优化
            spuAttributes.forEach(spuAttrValue -> {
                // 查询属性名称
                AttributeDO attribute = attributeService.getById(spuAttrValue.getAttrId());
                hashMap.put(attribute.getName(), spuAttrValue.getAttrValue());
            });
        }
        return hashMap;
    }
}
