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
import com.vv.cloudfarming.product.dao.entity.SpuAttrValueDO;
import com.vv.cloudfarming.product.dao.entity.SpuDO;
import com.vv.cloudfarming.product.dao.mapper.ShopMapper;
import com.vv.cloudfarming.product.dao.mapper.SkuMapper;
import com.vv.cloudfarming.product.dao.mapper.SpuAttrValueMapper;
import com.vv.cloudfarming.product.dao.mapper.SpuMapper;
import com.vv.cloudfarming.product.dto.req.AuditSubmitReqDTO;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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

        spuDO.setTitle(requestParam.getTitle());
        spuDO.setCategoryId(categoryId);
        spuDO.setImages(requestParam.getImages());
        spuDO.setStatus(ShelfStatusEnum.OFFLINE.getCode());
        spuDO.setAuditStatus(AuditStatusEnum.PENDING.getCode());
        boolean updated = this.updateById(spuDO);
        if (!updated) {
            throw new ServiceException("Failed to update SPU.");
        }

        syncSkuStatus(id, ShelfStatusEnum.OFFLINE.getCode());

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
                    spuResp.setAttributes(JSONUtil.toJsonStr(baseMapper.querySpuAttr(spuDO.getId())));

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

    private void clearProductCache(Long spuId) {
        stringRedisTemplate.delete(CacheKeyConstant.PRODUCT_SKU_DETAIL_CACHE_KEY + spuId);
    }
}
