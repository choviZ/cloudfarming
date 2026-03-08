package com.vv.cloudfarming.product.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.product.dao.entity.SeckillActivityDO;
import com.vv.cloudfarming.product.dao.entity.SkuDO;
import com.vv.cloudfarming.product.dao.entity.SpuDO;
import com.vv.cloudfarming.product.dao.mapper.SeckillActivityMapper;
import com.vv.cloudfarming.product.dao.mapper.SkuMapper;
import com.vv.cloudfarming.product.dao.mapper.SpuMapper;
import com.vv.cloudfarming.product.dto.req.SeckillActivityCreateReqDTO;
import com.vv.cloudfarming.product.enums.AuditStatusEnum;
import com.vv.cloudfarming.product.service.SeckillActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀活动服务实现
 */
@Service
@RequiredArgsConstructor
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivityDO>
        implements SeckillActivityService {

    private static final String SECKILL_CACHE_KEY = "cloudfarming:seckill";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSeckillActivity(SeckillActivityCreateReqDTO requestParam) {
        validateRequest(requestParam);

        SkuDO skuDO = skuMapper.selectById(requestParam.getSkuId());
        if (skuDO == null) {
            throw new ClientException("SKU不存在");
        }

        SpuDO spuDO = spuMapper.selectById(skuDO.getSpuId());
        if (spuDO == null) {
            throw new ServiceException("SKU关联的SPU不存在");
        }

        BigDecimal originalPrice = requestParam.getOriginalPrice() != null
                ? requestParam.getOriginalPrice()
                : skuDO.getPrice();
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ClientException("原价配置非法");
        }
        if (requestParam.getSeckillPrice().compareTo(originalPrice) > 0) {
            throw new ClientException("秒杀价不能高于原价");
        }

        if (existsOverlapActivity(requestParam.getSkuId(), requestParam.getStartTime(), requestParam.getEndTime())) {
            throw new ClientException("当前SKU存在时间重叠的秒杀活动");
        }

        SeckillActivityDO activityDO = SeckillActivityDO.builder()
                .activityName(requestParam.getActivityName().trim())
                .spuId(spuDO.getId())
                .skuId(skuDO.getId())
                .shopId(spuDO.getShopId())
                .originalPrice(originalPrice)
                .seckillPrice(requestParam.getSeckillPrice())
                .totalStock(requestParam.getTotalStock())
                .stock(requestParam.getTotalStock())
                .lockStock(0)
                .limitPerUser(requestParam.getLimitPerUser())
                .startTime(requestParam.getStartTime())
                .endTime(requestParam.getEndTime())
                .status(resolveStatus(requestParam.getStartTime(), requestParam.getEndTime()))
                .auditStatus(AuditStatusEnum.APPROVED.getCode())
                .build();

        boolean saved = this.save(activityDO);
        if (!saved) {
            throw new ServiceException("创建秒杀商品失败");
        }

        preloadSeckillCache(activityDO);
        return activityDO.getId();
    }

    private void validateRequest(SeckillActivityCreateReqDTO requestParam) {
        if (requestParam == null) {
            throw new ClientException("请求参数不能为空");
        }
        if (StrUtil.isBlank(requestParam.getActivityName())) {
            throw new ClientException("活动名称不能为空");
        }
        LocalDateTime startTime = requestParam.getStartTime();
        LocalDateTime endTime = requestParam.getEndTime();
        if (startTime == null || endTime == null) {
            throw new ClientException("活动时间不能为空");
        }
        if (!endTime.isAfter(startTime)) {
            throw new ClientException("活动结束时间必须晚于开始时间");
        }
    }

    private boolean existsOverlapActivity(Long skuId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SeckillActivityDO> queryWrapper = Wrappers.lambdaQuery(SeckillActivityDO.class)
                .eq(SeckillActivityDO::getSkuId, skuId)
                .lt(SeckillActivityDO::getStartTime, endTime)
                .gt(SeckillActivityDO::getEndTime, startTime);
        Long count = baseMapper.selectCount(queryWrapper);
        return count != null && count > 0;
    }

    private Integer resolveStatus(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return 0;
        }
        if (now.isAfter(endTime)) {
            return 2;
        }
        return 1;
    }

    private void preloadSeckillCache(SeckillActivityDO activityDO) {
        String seckillCacheKey = SECKILL_CACHE_KEY + activityDO.getId();
        Map<String, String> cacheData = new HashMap<>(16);
        cacheData.put("id", String.valueOf(activityDO.getId()));
        cacheData.put("activityName", activityDO.getActivityName());
        cacheData.put("spuId", String.valueOf(activityDO.getSpuId()));
        cacheData.put("skuId", String.valueOf(activityDO.getSkuId()));
        cacheData.put("shopId", String.valueOf(activityDO.getShopId()));
        cacheData.put("originalPrice", activityDO.getOriginalPrice().toPlainString());
        cacheData.put("seckillPrice", activityDO.getSeckillPrice().toPlainString());
        cacheData.put("totalStock", String.valueOf(activityDO.getTotalStock()));
        cacheData.put("stock", String.valueOf(activityDO.getStock()));
        cacheData.put("lockStock", String.valueOf(activityDO.getLockStock()));
        cacheData.put("limitPerUser", String.valueOf(activityDO.getLimitPerUser()));
        cacheData.put("startTime", activityDO.getStartTime().format(DATE_TIME_FORMATTER));
        cacheData.put("endTime", activityDO.getEndTime().format(DATE_TIME_FORMATTER));
        cacheData.put("status", String.valueOf(activityDO.getStatus()));
        cacheData.put("auditStatus", String.valueOf(activityDO.getAuditStatus()));
        try {
            stringRedisTemplate.opsForHash().putAll(seckillCacheKey, cacheData);
            long ttlSeconds = Duration.between(LocalDateTime.now(), activityDO.getEndTime().plusDays(1)).getSeconds();
            if (ttlSeconds > 0) {
                stringRedisTemplate.expire(seckillCacheKey, ttlSeconds, TimeUnit.SECONDS);
            }
        } catch (Exception ex) {
            throw new ServiceException("秒杀活动缓存预热失败");
        }
    }
}
