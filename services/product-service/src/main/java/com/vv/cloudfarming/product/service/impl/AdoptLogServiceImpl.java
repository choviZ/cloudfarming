package com.vv.cloudfarming.product.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.product.dao.entity.AdoptInstanceDO;
import com.vv.cloudfarming.product.dao.entity.AdoptLogDO;
import com.vv.cloudfarming.product.dao.mapper.AdoptInstanceMapper;
import com.vv.cloudfarming.product.dao.mapper.AdoptLogMapper;
import com.vv.cloudfarming.product.dto.req.AdoptLogCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptLogPageReqDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptLogRespDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptWeightPointRespDTO;
import com.vv.cloudfarming.product.enums.AdoptLogTypeEnum;
import com.vv.cloudfarming.product.enums.LivestockStatusEnum;
import com.vv.cloudfarming.product.service.AdoptLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdoptLogServiceImpl extends ServiceImpl<AdoptLogMapper, AdoptLogDO> implements AdoptLogService {

    private final AdoptInstanceMapper adoptInstanceMapper;

    @Override
    public void createAdoptLog(AdoptLogCreateReqDTO requestParam) {
        long farmerId = StpUtil.getLoginIdAsLong();
        AdoptInstanceDO instance = getAndCheckReadableInstance(requestParam.getInstanceId(), null, true);
        if (!Objects.equals(instance.getStatus(), LivestockStatusEnum.ADOPTED.getCode())) {
            throw new ClientException("当前养殖实例已结束，不能继续更新生长日记");
        }
        Integer logType = requestParam.getLogType();
        AdoptLogTypeEnum typeEnum = AdoptLogTypeEnum.getByCode(logType);
        if (typeEnum == null) {
            throw new ClientException("日志类型不存在");
        }
        if (AdoptLogTypeEnum.WEIGHT_RECORD.getCode().equals(logType)) {
            if (requestParam.getWeight() == null || requestParam.getWeight() <= 0) {
                throw new ClientException("体重记录必须填写大于0的体重");
            }
        }
        boolean hasContent = StrUtil.isNotBlank(requestParam.getContent())
            || CollUtil.isNotEmpty(requestParam.getImageUrls())
            || StrUtil.isNotBlank(requestParam.getVideoUrl())
            || requestParam.getWeight() != null;
        if (!hasContent) {
            throw new ClientException("请至少填写文字、图片、视频或体重中的一种内容");
        }

        AdoptLogDO adoptLogDO = new AdoptLogDO();
        adoptLogDO.setInstanceId(Long.valueOf(instance.getId()));
        adoptLogDO.setLogType(logType);
        adoptLogDO.setContent(StrUtil.trimToNull(requestParam.getContent()));
        adoptLogDO.setImageUrls(joinImageUrls(requestParam.getImageUrls()));
        adoptLogDO.setVideoUrl(StrUtil.trimToNull(requestParam.getVideoUrl()));
        adoptLogDO.setWeight(requestParam.getWeight());
        adoptLogDO.setOperatorId(farmerId);
        int result = baseMapper.insert(adoptLogDO);
        if (result < 1) {
            throw new ClientException("创建养殖日志失败");
        }
    }

    @Override
    public IPage<AdoptLogRespDTO> pageAdoptLogs(AdoptLogPageReqDTO requestParam) {
        AdoptInstanceDO instance = getAndCheckReadableInstance(requestParam.getInstanceId(), requestParam.getViewType(), false);
        LambdaQueryWrapper<AdoptLogDO> queryWrapper = Wrappers.lambdaQuery(AdoptLogDO.class)
            .eq(AdoptLogDO::getInstanceId, Long.valueOf(instance.getId()))
            .eq(Objects.nonNull(requestParam.getLogType()), AdoptLogDO::getLogType, requestParam.getLogType())
            .orderByDesc(AdoptLogDO::getCreateTime)
            .orderByDesc(AdoptLogDO::getId);
        return page(requestParam, queryWrapper).convert(this::buildLogResp);
    }

    @Override
    public List<AdoptWeightPointRespDTO> listWeightTrend(Long instanceId, String viewType) {
        AdoptInstanceDO instance = getAndCheckReadableInstance(instanceId, viewType, false);
        List<AdoptLogDO> records = baseMapper.selectList(Wrappers.lambdaQuery(AdoptLogDO.class)
            .eq(AdoptLogDO::getInstanceId, Long.valueOf(instance.getId()))
            .isNotNull(AdoptLogDO::getWeight)
            .orderByAsc(AdoptLogDO::getCreateTime)
            .orderByAsc(AdoptLogDO::getId));
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }
        return records.stream()
            .map(item -> AdoptWeightPointRespDTO.builder()
                .recordTime(item.getCreateTime())
                .weight(item.getWeight())
                .build())
            .toList();
    }

    private AdoptLogRespDTO buildLogResp(AdoptLogDO logDO) {
        AdoptLogTypeEnum logTypeEnum = AdoptLogTypeEnum.getByCode(logDO.getLogType());
        return AdoptLogRespDTO.builder()
            .id(logDO.getId())
            .instanceId(logDO.getInstanceId())
            .logType(logDO.getLogType())
            .logTypeDesc(logTypeEnum == null ? "未知类型" : logTypeEnum.getDesc())
            .content(logDO.getContent())
            .imageUrls(splitImageUrls(logDO.getImageUrls()))
            .videoUrl(logDO.getVideoUrl())
            .weight(logDO.getWeight())
            .createTime(logDO.getCreateTime())
            .build();
    }

    private AdoptInstanceDO getAndCheckReadableInstance(Long instanceId, String viewType, boolean farmerOnly) {
        if (instanceId == null || instanceId <= 0) {
            throw new ClientException("养殖实例不存在");
        }
        AdoptInstanceDO instance = adoptInstanceMapper.selectOne(Wrappers.lambdaQuery(AdoptInstanceDO.class)
            .eq(AdoptInstanceDO::getId, instanceId)
            .eq(AdoptInstanceDO::getDelFlag, 0));
        if (instance == null) {
            throw new ClientException("养殖实例不存在");
        }
        long userId = StpUtil.getLoginIdAsLong();
        boolean hasFarmerRole = StpUtil.hasRole(UserRoleConstant.FARMER_DESC);
        if (farmerOnly && !hasFarmerRole) {
            throw new ClientException("只有农户可以发布养殖日志");
        }
        boolean isFarmer = farmerOnly || resolveFarmerView(viewType, hasFarmerRole);
        if (isFarmer) {
            if (!Objects.equals(instance.getFarmerId(), userId)) {
                throw new ClientException("无权查看该养殖实例");
            }
            return instance;
        }
        if (!Objects.equals(instance.getOwnerUserId(), userId)) {
            throw new ClientException("无权查看该养殖实例");
        }
        return instance;
    }

    private boolean resolveFarmerView(String viewType, boolean hasFarmerRole) {
        viewType = StrUtil.trimToNull(viewType);
        if (viewType == null) {
            return hasFarmerRole;
        }
        if ("FARMER".equalsIgnoreCase(viewType)) {
            if (!hasFarmerRole) {
                throw new ClientException("当前账号无权按农户视角查询");
            }
            return true;
        }
        if ("USER".equalsIgnoreCase(viewType)) {
            return false;
        }
        throw new ClientException("查询视角不正确");
    }

    private String joinImageUrls(List<String> imageUrls) {
        if (CollUtil.isEmpty(imageUrls)) {
            return null;
        }
        return imageUrls.stream()
            .map(StrUtil::trim)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .collect(java.util.stream.Collectors.joining(","));
    }

    private List<String> splitImageUrls(String imageUrls) {
        if (StrUtil.isBlank(imageUrls)) {
            return Collections.emptyList();
        }
        return StrUtil.splitTrim(imageUrls, ',');
    }
}
