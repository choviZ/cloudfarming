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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.enums.ShelfStatusEnum;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.product.constant.CacheKeyConstant;
import com.vv.cloudfarming.product.dao.entity.AttributeDO;
import com.vv.cloudfarming.product.dao.entity.SkuAttrValueDO;
import com.vv.cloudfarming.product.dao.entity.SkuDO;
import com.vv.cloudfarming.product.dao.entity.SpuAttrValueDO;
import com.vv.cloudfarming.product.dao.entity.SpuDO;
import com.vv.cloudfarming.product.dao.mapper.ShopMapper;
import com.vv.cloudfarming.product.dao.mapper.SkuAttrValueMapper;
import com.vv.cloudfarming.product.dao.mapper.SkuMapper;
import com.vv.cloudfarming.product.dao.mapper.SpuAttrValueMapper;
import com.vv.cloudfarming.product.dao.mapper.SpuMapper;
import com.vv.cloudfarming.product.dto.domain.SaleAttrDTO;
import com.vv.cloudfarming.product.dto.domain.SkuItemDTO;
import com.vv.cloudfarming.product.dto.domain.SpuAttrItemDTO;
import com.vv.cloudfarming.product.dto.req.AuditSubmitReqDTO;
import com.vv.cloudfarming.product.dto.req.SkuCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.SpuAttrValueCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.SpuAttrValueUpdateReqDTO;
import com.vv.cloudfarming.product.dto.req.SpuCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.SpuPageQueryReqDTO;
import com.vv.cloudfarming.product.dto.req.SpuUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.CategoryRespDTO;
import com.vv.cloudfarming.product.dto.resp.ProductRespDTO;
import com.vv.cloudfarming.product.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.product.dto.resp.SpuAttrValueRespDTO;
import com.vv.cloudfarming.product.dto.resp.SpuRespDTO;
import com.vv.cloudfarming.product.enums.AuditStatusEnum;
import com.vv.cloudfarming.product.enums.ProductTypeEnum;
import com.vv.cloudfarming.product.service.AttributeService;
import com.vv.cloudfarming.product.service.AuditService;
import com.vv.cloudfarming.product.service.CategoryService;
import com.vv.cloudfarming.product.service.SkuService;
import com.vv.cloudfarming.product.service.SpuService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * SPU service implementation.
 */
@Service
@RequiredArgsConstructor
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuDO> implements SpuService {

    private static final String NULL_VALUE = "NULL_VALUE";

    private final CategoryService categoryService;
    private final SpuAttrValueMapper spuAttrValueMapper;
    private final AttributeService attributeService;
    private final SkuService skuService;
    private final AuditService auditService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final SkuAttrValueMapper skuAttrValueMapper;
    private final SkuMapper skuMapper;
    private final ShopMapper shopMapper;

    @Override
    @Transactional
    public Long saveSpu(SpuCreateReqDTO requestParam) {
        if (requestParam == null) {
            throw new ClientException("Request body cannot be null.");
        }
        Long categoryId = requestParam.getCategoryId();
        CategoryRespDTO category = categoryService.getCategoryById(categoryId);
        if (category == null) {
            throw new ClientException("Category does not exist.");
        }

        SpuDO spu = BeanUtil.toBean(requestParam, SpuDO.class);
        spu.setShopId(resolveOperateShopId(requestParam.getShopId()));
        spu.setStatus(ShelfStatusEnum.OFFLINE.getCode());
        spu.setAuditStatus(AuditStatusEnum.PENDING.getCode());
        boolean result = this.save(spu);
        if (!result) {
            throw new ServiceException("Failed to create SPU.");
        }

        AuditSubmitReqDTO auditSubmitReqDTO = new AuditSubmitReqDTO();
        auditSubmitReqDTO.setBizId(spu.getId());
        auditSubmitReqDTO.setBizType(ProductTypeEnum.SPU.getCode());
        auditService.submitAudit(StpUtil.getLoginIdAsLong(), auditSubmitReqDTO);
        return spu.getId();
    }

    @Override
    @Transactional
    public void updateSpu(SpuUpdateReqDTO requestParam) {
        if (requestParam == null) {
            throw new ClientException("Request body cannot be null.");
        }
        Long id = requestParam.getId();
        if (id == null || id <= 0) {
            throw new ClientException("Invalid SPU id.");
        }
        Long categoryId = requestParam.getCategoryId();
        CategoryRespDTO category = categoryService.getCategoryById(categoryId);
        if (category == null) {
            throw new ClientException("Category does not exist.");
        }

        SpuDO spuDO = this.getById(id);
        if (spuDO == null) {
            throw new ClientException("SPU does not exist.");
        }
        validateOperatePermission(spuDO);

        List<SpuAttrItemDTO> attrItems = normalizeSpuAttrItems(requestParam.getAttrItems());
        List<SaleAttrDTO> saleAttrs = normalizeSaleAttrs(requestParam.getSaleAttrs());
        List<SkuItemDTO> skuItems = normalizeSkuItems(requestParam.getSkuItems());
        boolean shouldSyncAttrItems = requestParam.getAttrItems() != null;
        boolean shouldSyncSkuItems = requestParam.getSaleAttrs() != null || requestParam.getSkuItems() != null;
        if (shouldSyncSkuItems) {
            validateSkuPayload(saleAttrs, skuItems);
        }

        spuDO.setTitle(requestParam.getTitle());
        spuDO.setCategoryId(categoryId);
        spuDO.setImages(requestParam.getImages());
        spuDO.setStatus(ShelfStatusEnum.OFFLINE.getCode());
        spuDO.setAuditStatus(AuditStatusEnum.PENDING.getCode());
        boolean updated = this.updateById(spuDO);
        if (!updated) {
            throw new ServiceException("Failed to update SPU.");
        }

        if (shouldSyncAttrItems) {
            rebuildSpuAttrValues(id, attrItems);
        }

        if (shouldSyncSkuItems) {
            syncSpuSkus(id, saleAttrs, skuItems);
        } else {
            syncSkuStatus(id, ShelfStatusEnum.OFFLINE.getCode());
        }

        AuditSubmitReqDTO auditSubmitReqDTO = new AuditSubmitReqDTO();
        auditSubmitReqDTO.setBizId(spuDO.getId());
        auditSubmitReqDTO.setBizType(ProductTypeEnum.SPU.getCode());
        auditService.submitAudit(StpUtil.getLoginIdAsLong(), auditSubmitReqDTO);
        clearProductCache(id);
    }

    @Override
    public void deleteSpuById(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("Invalid SPU id.");
        }
        SpuDO spuDO = this.getById(id);
        if (spuDO == null) {
            throw new ClientException("SPU does not exist.");
        }
        validateOperatePermission(spuDO);
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new ServiceException("Failed to delete SPU.");
        }
    }

    @Override
    public SpuRespDTO getSpuById(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("Invalid SPU id.");
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
            throw new ClientException("Invalid SPU id.");
        }

        String cacheKey = CacheKeyConstant.PRODUCT_SKU_DETAIL_CACHE_KEY + id;
        String cache = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isBlank(cache)) {
            RLock lock = redissonClient.getLock(CacheKeyConstant.LOCK_PRODUCT_SKU_KEY + id);
            lock.lock();
            try {
                cache = stringRedisTemplate.opsForValue().get(cacheKey);
                if (StrUtil.isBlank(cache)) {
                    SpuDO spuDO = this.getById(id);
                    if (spuDO == null) {
                        stringRedisTemplate.opsForValue().setIfAbsent(cacheKey, NULL_VALUE, 5, TimeUnit.MINUTES);
                        return null;
                    }

                    SpuRespDTO spuResp = BeanUtil.toBean(spuDO, SpuRespDTO.class);
                    ProductRespDTO result = new ProductRespDTO();
                    result.setProductSpu(spuResp);
                    result.setProductSkus(skuService.getSkusBySpuId(id));
                    spuResp.setAttributes(JSONUtil.toJsonStr(getSpuAttributes(spuDO.getId())));

                    cache = JSONUtil.toJsonStr(result);
                    stringRedisTemplate.opsForValue().set(cacheKey, cache, 30, TimeUnit.MINUTES);
                }
            } finally {
                lock.unlock();
            }
        }
        if (NULL_VALUE.equals(cache)) {
            return null;
        }
        return JSONUtil.toBean(cache, ProductRespDTO.class);
    }

    @Override
    public IPage<SpuRespDTO> listSpuByPage(SpuPageQueryReqDTO queryParam) {
        if (queryParam == null) {
            throw new ClientException("Request body cannot be null.");
        }

        Long id = queryParam.getId();
        String spuName = queryParam.getSpuName();
        Long categoryId = queryParam.getCategoryId();
        Long shopId = queryParam.getShopId();
        Integer status = queryParam.getStatus();
        Integer auditStatus = queryParam.getAuditStatus();
        List<Long> categoryIds = CollUtil.isNotEmpty(queryParam.getCategoryIds())
                ? queryParam.getCategoryIds()
                : resolveCategoryIds(categoryId);

        LambdaQueryWrapper<SpuDO> queryWrapper = Wrappers.lambdaQuery(SpuDO.class)
                .eq(Objects.nonNull(id), SpuDO::getId, id)
                .eq(Objects.nonNull(shopId), SpuDO::getShopId, shopId)
                .like(StrUtil.isNotBlank(spuName), SpuDO::getTitle, spuName)
                .in(CollUtil.isNotEmpty(categoryIds), SpuDO::getCategoryId, categoryIds)
                .eq(status != null, SpuDO::getStatus, status)
                .eq(auditStatus != null, SpuDO::getAuditStatus, auditStatus)
                .orderByDesc(SpuDO::getCreateTime);

        Page<SpuDO> pageRequest = new Page<>(queryParam.getCurrent(), queryParam.getSize());
        IPage<SpuDO> spuDOPage = baseMapper.selectPage(pageRequest, queryWrapper);
        return spuDOPage.convert(spuDO -> {
            SpuRespDTO spuRespDTO = BeanUtil.toBean(spuDO, SpuRespDTO.class);
            spuRespDTO.setAttributes(JSONUtil.toJsonStr(getSpuAttributes(spuDO.getId())));
            spuRespDTO.setMinPrice(skuMapper.queryLowestPrice(spuDO.getId()));
            return spuRespDTO;
        });
    }

    @Override
    public IPage<SpuRespDTO> listMySpuByPage(Long userId, SpuPageQueryReqDTO queryParam) {
        if (userId == null || userId <= 0) {
            throw new ClientException("Invalid user id.");
        }
        if (queryParam == null) {
            queryParam = new SpuPageQueryReqDTO();
        }

        Long shopId = shopMapper.getIdByFarmerId(userId);
        if (shopId == null) {
            return new Page<>(queryParam.getCurrent(), queryParam.getSize());
        }

        queryParam.setShopId(shopId);
        return listSpuByPage(queryParam);
    }

    @Override
    public void updateSpuStatus(Long id, Integer status) {
        if (id == null || id <= 0) {
            throw new ClientException("Invalid SPU id.");
        }
        ShelfStatusEnum newStatus = ShelfStatusEnum.of(status);
        if (newStatus == null) {
            throw new ClientException("Invalid shelf status.");
        }

        SpuDO spuDO = baseMapper.selectById(id);
        if (spuDO == null) {
            throw new ClientException("SPU does not exist.");
        }
        validateOperatePermission(spuDO);

        if (ShelfStatusEnum.ONLINE == newStatus
                && !AuditStatusEnum.APPROVED.getCode().equals(spuDO.getAuditStatus())) {
            throw new ClientException("Only approved SPU can be put online.");
        }
        if (Objects.equals(spuDO.getStatus(), status)) {
            throw new ClientException("SPU already has the target status.");
        }

        LambdaUpdateWrapper<SpuDO> updateWrapper = Wrappers.lambdaUpdate(SpuDO.class)
                .eq(SpuDO::getId, id)
                .set(SpuDO::getStatus, status);
        boolean updated = this.update(updateWrapper);
        if (!updated) {
            throw new ServiceException("Failed to update SPU status.");
        }

        syncSkuStatus(id, status);
        clearProductCache(id);
    }

    @Override
    public void createSpuAttrValue(SpuAttrValueCreateReqDTO requestParam) {
        if (requestParam == null) {
            throw new ClientException("Request body cannot be null.");
        }
        Long spuId = requestParam.getSpuId();
        Long attrId = requestParam.getAttrId();
        String attrValue = requestParam.getAttrValue();

        if (spuId == null || spuId <= 0) {
            throw new ClientException("Invalid SPU id.");
        }
        if (attrId == null || attrId <= 0) {
            throw new ClientException("Invalid attribute id.");
        }
        if (StrUtil.isBlank(attrValue)) {
            throw new ClientException("Attribute value cannot be blank.");
        }

        SpuDO spu = this.getById(spuId);
        if (spu == null) {
            throw new ClientException("SPU does not exist.");
        }
        if (spuAttrValueMapper.checkAttributeExists(attrId) == 0) {
            throw new ClientException("Attribute does not exist.");
        }

        LambdaQueryWrapper<SpuAttrValueDO> wrapper = Wrappers.lambdaQuery(SpuAttrValueDO.class)
                .eq(SpuAttrValueDO::getSpuId, spuId)
                .eq(SpuAttrValueDO::getAttrId, attrId);
        SpuAttrValueDO existing = spuAttrValueMapper.selectOne(wrapper);
        if (existing != null) {
            throw new ClientException("SPU attribute value already exists.");
        }

        SpuAttrValueDO spuAttrValue = BeanUtil.toBean(requestParam, SpuAttrValueDO.class);
        int inserted = spuAttrValueMapper.insert(spuAttrValue);
        if (inserted < 1) {
            throw new ServiceException("Failed to create SPU attribute value.");
        }
    }

    @Override
    public boolean updateSpuAttrValue(SpuAttrValueUpdateReqDTO requestParam) {
        if (requestParam == null) {
            throw new ClientException("Request body cannot be null.");
        }
        Long id = requestParam.getId();
        Long spuId = requestParam.getSpuId();
        Long attrId = requestParam.getAttrId();
        String attrValue = requestParam.getAttrValue();

        if (id == null || id <= 0) {
            throw new ClientException("Invalid id.");
        }

        SpuAttrValueDO existing = spuAttrValueMapper.selectById(id);
        if (existing == null) {
            throw new ClientException("SPU attribute value does not exist.");
        }

        if (ObjectUtil.isNotNull(spuId) && this.getById(spuId) == null) {
            throw new ClientException("SPU does not exist.");
        }
        if (ObjectUtil.isNotNull(attrId) && spuAttrValueMapper.checkAttributeExists(attrId) == 0) {
            throw new ClientException("Attribute does not exist.");
        }

        LambdaUpdateWrapper<SpuAttrValueDO> wrapper = Wrappers.lambdaUpdate(SpuAttrValueDO.class)
                .eq(SpuAttrValueDO::getId, id)
                .set(ObjectUtil.isNotNull(spuId), SpuAttrValueDO::getSpuId, spuId)
                .set(ObjectUtil.isNotNull(attrId), SpuAttrValueDO::getAttrId, attrId)
                .set(StrUtil.isNotBlank(attrValue), SpuAttrValueDO::getAttrValue, attrValue);
        int updated = spuAttrValueMapper.update(null, wrapper);
        if (updated < 1) {
            throw new ServiceException("Failed to update SPU attribute value.");
        }
        return true;
    }

    @Override
    public boolean deleteSpuAttrValue(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("Invalid id.");
        }
        SpuAttrValueDO spuAttrValue = spuAttrValueMapper.selectById(id);
        if (spuAttrValue == null) {
            throw new ClientException("SPU attribute value does not exist.");
        }

        int deleted = spuAttrValueMapper.deleteById(id);
        if (deleted < 1) {
            throw new ServiceException("Failed to delete SPU attribute value.");
        }
        return true;
    }

    @Override
    public SpuAttrValueRespDTO getSpuAttrValueById(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("Invalid id.");
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
            throw new ClientException("Invalid SPU id.");
        }
        if (this.getById(spuId) == null) {
            throw new ClientException("SPU does not exist.");
        }

        LambdaQueryWrapper<SpuAttrValueDO> wrapper = Wrappers.lambdaQuery(SpuAttrValueDO.class)
                .eq(SpuAttrValueDO::getSpuId, spuId);
        List<SpuAttrValueDO> spuAttrValues = spuAttrValueMapper.selectList(wrapper);
        return spuAttrValues.stream()
                .map(item -> BeanUtil.toBean(item, SpuAttrValueRespDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public SpuAttrValueRespDTO getSpuAttrValueBySpuIdAndAttrId(Long spuId, Long attrId) {
        if (spuId == null || spuId <= 0) {
            throw new ClientException("Invalid SPU id.");
        }
        if (attrId == null || attrId <= 0) {
            throw new ClientException("Invalid attribute id.");
        }
        if (this.getById(spuId) == null) {
            throw new ClientException("SPU does not exist.");
        }
        if (spuAttrValueMapper.checkAttributeExists(attrId) == 0) {
            throw new ClientException("Attribute does not exist.");
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
        if (CollUtil.isEmpty(requestParams)) {
            throw new ClientException("Request list cannot be empty.");
        }
        for (SpuAttrValueCreateReqDTO requestParam : requestParams) {
            createSpuAttrValue(requestParam);
        }
    }

    @Override
    public boolean batchDeleteSpuAttrValues(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new ClientException("Id list cannot be empty.");
        }
        int deleted = spuAttrValueMapper.deleteBatchIds(ids);
        if (deleted < 1) {
            throw new ServiceException("Failed to batch delete SPU attribute values.");
        }
        return true;
    }

    @Override
    public boolean deleteSpuAttrValuesBySpuId(Long spuId) {
        if (spuId == null || spuId <= 0) {
            throw new ClientException("Invalid SPU id.");
        }
        if (this.getById(spuId) == null) {
            throw new ClientException("SPU does not exist.");
        }

        LambdaQueryWrapper<SpuAttrValueDO> wrapper = Wrappers.lambdaQuery(SpuAttrValueDO.class)
                .eq(SpuAttrValueDO::getSpuId, spuId);
        int deleted = spuAttrValueMapper.delete(wrapper);
        if (deleted < 1) {
            throw new ServiceException("Failed to delete SPU attribute values.");
        }
        return true;
    }

    private List<Long> resolveCategoryIds(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        CategoryRespDTO category = categoryService.getCategoryById(categoryId);
        if (category == null) {
            throw new ClientException("Category does not exist.");
        }

        List<Long> categoryIds = new ArrayList<>();
        collectCategoryIds(categoryId, categoryIds);
        return categoryIds;
    }

    private void collectCategoryIds(Long categoryId, List<Long> categoryIds) {
        categoryIds.add(categoryId);
        List<CategoryRespDTO> children = categoryService.getChildrenByParentId(categoryId);
        if (CollUtil.isEmpty(children)) {
            return;
        }
        children.forEach(child -> collectCategoryIds(child.getId(), categoryIds));
    }

    /**
     * Build base attribute map for the current SPU.
     */
    private HashMap<String, String> getSpuAttributes(Long spuId) {
        List<SpuAttrValueDO> spuAttributes = spuAttrValueMapper.getAttrValueBySpuId(spuId);
        HashMap<String, String> result = new HashMap<>();
        if (spuAttributes == null) {
            return result;
        }

        for (SpuAttrValueDO spuAttrValue : spuAttributes) {
            AttributeDO attribute = attributeService.getById(spuAttrValue.getAttrId());
            if (attribute != null) {
                result.put(attribute.getName(), spuAttrValue.getAttrValue());
            }
        }
        return result;
    }

    private Long resolveOperateShopId(Long requestShopId) {
        if (StpUtil.hasRole(UserRoleConstant.ADMIN_DESC)) {
            if (requestShopId == null || requestShopId <= 0) {
                throw new ClientException("shopId is required for admin operation.");
            }
            return requestShopId;
        }

        Long shopId = shopMapper.getIdByFarmerId(StpUtil.getLoginIdAsLong());
        if (shopId == null) {
            throw new ClientException("Current farmer does not have an associated shop.");
        }
        return shopId;
    }

    private void validateOperatePermission(SpuDO spuDO) {
        if (spuDO == null || StpUtil.hasRole(UserRoleConstant.ADMIN_DESC)) {
            return;
        }

        Long currentShopId = resolveOperateShopId(null);
        if (!Objects.equals(currentShopId, spuDO.getShopId())) {
            throw new ClientException("No permission to operate this SPU.");
        }
    }

    private void syncSkuStatus(Long spuId, Integer status) {
        List<Long> skuIds = skuService.getSkusBySpuId(spuId).stream()
                .map(SkuRespDTO::getId)
                .collect(Collectors.toList());
        for (Long skuId : skuIds) {
            skuService.updateSkuStatus(skuId, status);
        }
    }

    private List<SpuAttrItemDTO> normalizeSpuAttrItems(List<SpuAttrItemDTO> attrItems) {
        if (attrItems == null) {
            return null;
        }
        List<SpuAttrItemDTO> result = new ArrayList<>();
        for (SpuAttrItemDTO attrItem : attrItems) {
            if (attrItem == null) {
                continue;
            }
            SpuAttrItemDTO normalized = new SpuAttrItemDTO();
            normalized.setAttrId(attrItem.getAttrId());
            normalized.setAttrValue(StrUtil.trim(attrItem.getAttrValue()));
            result.add(normalized);
        }
        return result;
    }

    private List<SaleAttrDTO> normalizeSaleAttrs(List<SaleAttrDTO> saleAttrs) {
        if (saleAttrs == null) {
            return null;
        }
        List<SaleAttrDTO> result = new ArrayList<>();
        for (SaleAttrDTO saleAttr : saleAttrs) {
            if (saleAttr == null) {
                continue;
            }
            SaleAttrDTO normalized = new SaleAttrDTO();
            normalized.setAttrId(saleAttr.getAttrId());
            List<String> values = saleAttr.getValues() == null
                    ? Collections.emptyList()
                    : saleAttr.getValues().stream()
                    .map(StrUtil::trim)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            normalized.setValues(values);
            result.add(normalized);
        }
        return result;
    }

    private List<SkuItemDTO> normalizeSkuItems(List<SkuItemDTO> skuItems) {
        if (skuItems == null) {
            return null;
        }
        List<SkuItemDTO> result = new ArrayList<>();
        for (SkuItemDTO skuItem : skuItems) {
            if (skuItem == null) {
                continue;
            }
            SkuItemDTO normalized = new SkuItemDTO();
            normalized.setAttrValues(normalizeSkuAttrValues(skuItem.getAttrValues()));
            normalized.setPrice(skuItem.getPrice());
            normalized.setStock(skuItem.getStock());
            normalized.setImage(StrUtil.trim(skuItem.getImage()));
            result.add(normalized);
        }
        return result;
    }

    private LinkedHashMap<Long, String> normalizeSkuAttrValues(Map<Long, String> attrValues) {
        LinkedHashMap<Long, String> result = new LinkedHashMap<>();
        if (attrValues == null || attrValues.isEmpty()) {
            return result;
        }
        List<Long> attrIds = attrValues.keySet().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        for (Long attrId : attrIds) {
            String attrValue = StrUtil.trim(attrValues.get(attrId));
            result.put(attrId, attrValue);
        }
        return result;
    }

    private void validateSkuPayload(List<SaleAttrDTO> saleAttrs, List<SkuItemDTO> skuItems) {
        if (CollUtil.isEmpty(saleAttrs)) {
            throw new ClientException("销售属性不能为空。");
        }
        if (CollUtil.isEmpty(skuItems)) {
            throw new ClientException("SKU 列表不能为空。");
        }

        Set<Long> allowedAttrIds = new HashSet<>();
        for (SaleAttrDTO saleAttr : saleAttrs) {
            if (saleAttr.getAttrId() == null || saleAttr.getAttrId() <= 0) {
                throw new ClientException("销售属性配置不完整。");
            }
            if (!allowedAttrIds.add(saleAttr.getAttrId())) {
                throw new ClientException("销售属性不能重复。");
            }
            if (spuAttrValueMapper.checkAttributeExists(saleAttr.getAttrId()) == 0) {
                throw new ClientException("销售属性不存在。");
            }
            if (CollUtil.isEmpty(saleAttr.getValues())) {
                throw new ClientException("销售属性值不能为空。");
            }
        }

        Set<String> skuKeys = new HashSet<>();
        for (SkuItemDTO skuItem : skuItems) {
            Map<Long, String> attrValues = normalizeSkuAttrValues(skuItem.getAttrValues());
            if (CollUtil.isEmpty(attrValues)) {
                throw new ClientException("SKU 规格不能为空。");
            }
            if (attrValues.size() != allowedAttrIds.size() || !allowedAttrIds.containsAll(attrValues.keySet())) {
                throw new ClientException("SKU 规格与销售属性不匹配。");
            }
            if (attrValues.values().stream().anyMatch(StrUtil::isBlank)) {
                throw new ClientException("SKU 规格值不能为空。");
            }
            if (skuItem.getPrice() == null || skuItem.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ClientException("SKU 价格必须大于 0。");
            }
            if (skuItem.getStock() == null || skuItem.getStock() < 0) {
                throw new ClientException("SKU 库存不能小于 0。");
            }
            String skuKey = buildSkuKey(attrValues);
            if (!skuKeys.add(skuKey)) {
                throw new ClientException("SKU 规格组合不能重复。");
            }
        }
    }

    private void rebuildSpuAttrValues(Long spuId, List<SpuAttrItemDTO> attrItems) {
        spuAttrValueMapper.delete(Wrappers.lambdaQuery(SpuAttrValueDO.class)
                .eq(SpuAttrValueDO::getSpuId, spuId));
        if (CollUtil.isEmpty(attrItems)) {
            return;
        }

        Set<Long> attrIds = new HashSet<>();
        for (SpuAttrItemDTO attrItem : attrItems) {
            if (attrItem.getAttrId() == null || attrItem.getAttrId() <= 0) {
                throw new ClientException("基础属性配置不完整。");
            }
            if (!attrIds.add(attrItem.getAttrId())) {
                throw new ClientException("基础属性不能重复。");
            }
            if (spuAttrValueMapper.checkAttributeExists(attrItem.getAttrId()) == 0) {
                throw new ClientException("基础属性不存在。");
            }
            if (StrUtil.isBlank(attrItem.getAttrValue())) {
                throw new ClientException("基础属性值不能为空。");
            }

            SpuAttrValueDO spuAttrValueDO = new SpuAttrValueDO();
            spuAttrValueDO.setSpuId(spuId);
            spuAttrValueDO.setAttrId(attrItem.getAttrId());
            spuAttrValueDO.setAttrValue(attrItem.getAttrValue());
            if (spuAttrValueMapper.insert(spuAttrValueDO) < 1) {
                throw new ServiceException("Failed to update SPU attributes.");
            }
        }
    }

    private void syncSpuSkus(Long spuId, List<SaleAttrDTO> saleAttrs, List<SkuItemDTO> skuItems) {
        List<SkuDO> existingSkus = skuService.list(Wrappers.lambdaQuery(SkuDO.class)
                .eq(SkuDO::getSpuId, spuId));
        Map<String, SkuDO> existingSkuMap = new HashMap<>();
        if (CollUtil.isNotEmpty(existingSkus)) {
            List<Long> skuIds = existingSkus.stream().map(SkuDO::getId).collect(Collectors.toList());
            Map<Long, List<SkuAttrValueDO>> existingSkuAttrMap = skuAttrValueMapper.selectList(
                            Wrappers.lambdaQuery(SkuAttrValueDO.class).in(SkuAttrValueDO::getSkuId, skuIds))
                    .stream()
                    .collect(Collectors.groupingBy(SkuAttrValueDO::getSkuId));

            for (SkuDO existingSku : existingSkus) {
                List<SkuAttrValueDO> attrValues = existingSkuAttrMap.get(existingSku.getId());
                if (CollUtil.isEmpty(attrValues)) {
                    continue;
                }
                Map<Long, String> existingAttrValues = new HashMap<>();
                for (SkuAttrValueDO attrValue : attrValues) {
                    existingAttrValues.put(attrValue.getAttrId(), attrValue.getAttrValue());
                }
                existingSkuMap.put(buildSkuKey(normalizeSkuAttrValues(existingAttrValues)), existingSku);
            }
        }

        Set<String> retainedSkuKeys = new HashSet<>();
        List<SkuItemDTO> skuItemsToCreate = new ArrayList<>();
        for (SkuItemDTO skuItem : skuItems) {
            Map<Long, String> attrValues = normalizeSkuAttrValues(skuItem.getAttrValues());
            String skuKey = buildSkuKey(attrValues);
            retainedSkuKeys.add(skuKey);

            SkuDO existingSku = existingSkuMap.get(skuKey);
            if (existingSku == null) {
                skuItemsToCreate.add(skuItem);
                continue;
            }

            SkuDO updateSku = new SkuDO();
            updateSku.setId(existingSku.getId());
            updateSku.setPrice(skuItem.getPrice());
            updateSku.setStock(skuItem.getStock());
            updateSku.setSkuImage(skuItem.getImage());
            updateSku.setStatus(ShelfStatusEnum.OFFLINE.getCode());
            if (skuMapper.updateById(updateSku) < 1) {
                throw new ServiceException("Failed to update SKU.");
            }
        }

        List<Long> removedSkuIds = existingSkuMap.entrySet().stream()
                .filter(entry -> !retainedSkuKeys.contains(entry.getKey()))
                .map(entry -> entry.getValue().getId())
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(removedSkuIds)) {
            skuAttrValueMapper.delete(Wrappers.lambdaQuery(SkuAttrValueDO.class)
                    .in(SkuAttrValueDO::getSkuId, removedSkuIds));
            if (!skuService.removeByIds(removedSkuIds)) {
                throw new ServiceException("Failed to remove outdated SKU.");
            }
        }

        if (CollUtil.isNotEmpty(skuItemsToCreate)) {
            SkuCreateReqDTO createReqDTO = new SkuCreateReqDTO();
            createReqDTO.setSpuId(spuId);
            createReqDTO.setSaleAttrs(saleAttrs);
            createReqDTO.setSkuItems(skuItemsToCreate);
            skuService.createSku(createReqDTO);
        }

        syncSkuStatus(spuId, ShelfStatusEnum.OFFLINE.getCode());
    }

    private String buildSkuKey(Map<Long, String> attrValues) {
        return attrValues.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + ":" + StrUtil.trim(entry.getValue()))
                .collect(Collectors.joining("|"));
    }

    private void clearProductCache(Long spuId) {
        stringRedisTemplate.delete(CacheKeyConstant.PRODUCT_SKU_DETAIL_CACHE_KEY + spuId);
    }
}
