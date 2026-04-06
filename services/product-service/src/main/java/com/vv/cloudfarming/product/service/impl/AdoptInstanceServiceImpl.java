package com.vv.cloudfarming.product.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.product.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.product.dao.entity.AdoptInstanceDO;
import com.vv.cloudfarming.product.dao.entity.AdoptLogDO;
import com.vv.cloudfarming.product.dao.mapper.AdoptItemMapper;
import com.vv.cloudfarming.product.dao.mapper.AdoptInstanceMapper;
import com.vv.cloudfarming.product.dao.mapper.AdoptLogMapper;
import com.vv.cloudfarming.product.dto.req.AdoptInstanceAssignReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptInstanceCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptInstanceMarkDeadReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptInstancePageReqDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptInstanceDetailRespDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptInstanceFulfillRespDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptInstanceRespDTO;
import com.vv.cloudfarming.product.dto.resp.OrderSimpleRespDTO;
import com.vv.cloudfarming.product.enums.LivestockStatusEnum;
import com.vv.cloudfarming.product.remote.OrderRemoteService;
import com.vv.cloudfarming.product.service.AdoptInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdoptInstanceServiceImpl extends ServiceImpl<AdoptInstanceMapper, AdoptInstanceDO> implements AdoptInstanceService {

    private final AdoptItemMapper adoptItemMapper;
    private final AdoptLogMapper adoptLogMapper;
    private final OrderRemoteService orderRemoteService;

    @Override
    public IPage<AdoptInstanceRespDTO> queryMyAdoptInstances(AdoptInstancePageReqDTO reqDTO) {
        long userId = StpUtil.getLoginIdAsLong();
        boolean isFarmer = StpUtil.hasRole(UserRoleConstant.FARMER_DESC);
        LambdaQueryWrapper<AdoptInstanceDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdoptInstanceDO::getDelFlag, 0);
        if (reqDTO.getStatus() != null) {
            queryWrapper.eq(AdoptInstanceDO::getStatus, reqDTO.getStatus());
        }
        if (reqDTO.getItemId() != null) {
            queryWrapper.eq(AdoptInstanceDO::getItemId, reqDTO.getItemId());
        }
        if (reqDTO.getOwnerOrderId() != null) {
            queryWrapper.eq(AdoptInstanceDO::getOwnerOrderId, reqDTO.getOwnerOrderId());
        }
        if (isFarmer) {
            queryWrapper.eq(AdoptInstanceDO::getFarmerId, userId);
            queryWrapper.orderByAsc(AdoptInstanceDO::getItemId)
                .orderByDesc(AdoptInstanceDO::getCreateTime);
        } else {
            queryWrapper.eq(AdoptInstanceDO::getOwnerUserId, userId);
            queryWrapper.orderByDesc(AdoptInstanceDO::getCreateTime);
        }
        IPage<AdoptInstanceDO> pageResult = this.page(reqDTO, queryWrapper);
        List<AdoptInstanceDO> records = pageResult.getRecords();
        Map<Long, AdoptItemDO> adoptItemMap = buildAdoptItemMap(records);
        Map<Long, Date> latestLogTimeMap = buildLatestLogTimeMap(records);
        Map<Long, OrderSimpleRespDTO> orderSimpleMap = buildOrderSimpleMap(records);
        return pageResult.convert(item -> buildInstanceResp(item, adoptItemMap, latestLogTimeMap, orderSimpleMap));
    }

    @Override
    public AdoptInstanceDetailRespDTO getAdoptInstanceDetail(Long instanceId) {
        AdoptInstanceDO instance = getAndCheckReadableInstance(instanceId);
        AdoptItemDO adoptItem = instance.getItemId() == null ? null : adoptItemMapper.selectById(instance.getItemId());
        Date latestLogTime = getLatestLogTime(Long.valueOf(instance.getId()));
        String orderNo = resolveOrderNo(instance.getOwnerOrderId(), buildOrderSimpleMap(List.of(instance)));
        return AdoptInstanceDetailRespDTO.builder()
            .id(instance.getId())
            .itemId(instance.getItemId())
            .itemTitle(adoptItem == null ? "认养项目" : adoptItem.getTitle())
            .itemCoverImage(adoptItem == null ? null : adoptItem.getCoverImage())
            .earTagNo(instance.getEarTagNo())
            .image(instance.getImage())
            .status(instance.getStatus())
            .statusDesc(resolveStatusDesc(instance.getStatus()))
            .ownerOrderId(instance.getOwnerOrderId())
            .orderNo(orderNo)
            .latestLogTime(latestLogTime)
            .deathTime(instance.getDeathTime())
            .deathReason(instance.getDeathReason())
            .createTime(instance.getCreateTime())
            .updateTime(instance.getUpdateTime())
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer assignAdoptInstances(Long currentFarmerId, AdoptInstanceAssignReqDTO reqDTO) {
        if (!Objects.equals(currentFarmerId, reqDTO.getFarmerId())) {
            throw new ClientException("无权分配该订单牲畜");
        }

        List<AdoptInstanceCreateReqDTO> instanceReqList = reqDTO.getInstances();
        if (CollUtil.isEmpty(instanceReqList)) {
            throw new ClientException("养殖实例不能为空");
        }

        validateInstanceRequest(instanceReqList);

        List<AdoptInstanceDO> existingInstances = baseMapper.selectList(
            new LambdaQueryWrapper<AdoptInstanceDO>()
                .eq(AdoptInstanceDO::getOwnerOrderId, reqDTO.getOwnerOrderId())
                .eq(AdoptInstanceDO::getDelFlag, 0)
        );
        if (CollUtil.isNotEmpty(existingInstances)) {
            return validateExistingAssignment(existingInstances, reqDTO);
        }

        Set<Long> earTagNos = instanceReqList.stream()
            .map(AdoptInstanceCreateReqDTO::getEarTagNo)
            .collect(Collectors.toSet());
        List<AdoptInstanceDO> duplicatedEarTags = baseMapper.selectList(
            new LambdaQueryWrapper<AdoptInstanceDO>()
                .in(AdoptInstanceDO::getEarTagNo, earTagNos)
                .eq(AdoptInstanceDO::getDelFlag, 0)
        );
        if (CollUtil.isNotEmpty(duplicatedEarTags)) {
            throw new ClientException("耳标号已存在，请检查后重试");
        }

        List<AdoptInstanceDO> entities = instanceReqList.stream()
            .map(each -> AdoptInstanceDO.builder()
                .itemId(each.getItemId())
                .earTagNo(each.getEarTagNo())
                .status(LivestockStatusEnum.ADOPTED.getCode())
                .farmerId(reqDTO.getFarmerId())
                .ownerUserId(reqDTO.getOwnerUserId())
                .ownerOrderId(reqDTO.getOwnerOrderId())
                .build())
            .toList();
        if (!saveBatch(entities)) {
            throw new ServiceException("创建养殖实例失败");
        }
        return entities.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdoptInstanceFulfillRespDTO fulfillAdoptInstance(Long currentFarmerId, Long instanceId) {
        AdoptInstanceDO instance = getAndCheckFarmerOwnedInstance(currentFarmerId, instanceId);
        Long ownerOrderId = instance.getOwnerOrderId();
        if (ownerOrderId == null || ownerOrderId <= 0) {
            throw new ClientException("当前养殖实例未绑定认养订单");
        }
        if (Objects.equals(instance.getStatus(), LivestockStatusEnum.FULFILLED.getCode())) {
            return buildFulfillResp(ownerOrderId);
        }
        if (!Objects.equals(instance.getStatus(), LivestockStatusEnum.ADOPTED.getCode())) {
            throw new ClientException("当前养殖实例状态不支持完成履约");
        }

        int updated = baseMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AdoptInstanceDO>()
            .eq(AdoptInstanceDO::getId, instance.getId())
            .eq(AdoptInstanceDO::getDelFlag, 0)
            .eq(AdoptInstanceDO::getStatus, LivestockStatusEnum.ADOPTED.getCode())
            .set(AdoptInstanceDO::getStatus, LivestockStatusEnum.FULFILLED.getCode()));
        if (!SqlHelper.retBool(updated)) {
            AdoptInstanceDO latestInstance = getInstanceById(instanceId);
            if (Objects.equals(latestInstance.getStatus(), LivestockStatusEnum.FULFILLED.getCode())) {
                return buildFulfillResp(ownerOrderId);
            }
            throw new ServiceException("更新养殖实例状态失败，请刷新后重试");
        }
        return buildFulfillResp(ownerOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAdoptInstanceDead(Long currentFarmerId, AdoptInstanceMarkDeadReqDTO reqDTO) {
        AdoptInstanceDO instance = getAndCheckFarmerOwnedInstance(currentFarmerId, reqDTO.getInstanceId());
        if (Objects.equals(instance.getStatus(), LivestockStatusEnum.FULFILLED.getCode())) {
            throw new ClientException("已履约完成的养殖实例不支持再修改为异常状态");
        }
        if (!Objects.equals(instance.getStatus(), LivestockStatusEnum.ADOPTED.getCode())
            && !Objects.equals(instance.getStatus(), LivestockStatusEnum.DEAD.getCode())) {
            throw new ClientException("当前养殖实例状态不支持异常处理");
        }

        Date deathTime = reqDTO.getDeathTime() == null ? new Date() : reqDTO.getDeathTime();
        String deathReason = StrUtil.trim(reqDTO.getDeathReason());
        if (StrUtil.isBlank(deathReason)) {
            throw new ClientException("死亡原因不能为空");
        }

        int updated = baseMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AdoptInstanceDO>()
            .eq(AdoptInstanceDO::getId, instance.getId())
            .eq(AdoptInstanceDO::getDelFlag, 0)
            .set(AdoptInstanceDO::getStatus, LivestockStatusEnum.DEAD.getCode())
            .set(AdoptInstanceDO::getDeathTime, deathTime)
            .set(AdoptInstanceDO::getDeathReason, deathReason));
        if (!SqlHelper.retBool(updated)) {
            throw new ServiceException("更新养殖实例异常状态失败，请刷新后重试");
        }
    }

    private void validateInstanceRequest(List<AdoptInstanceCreateReqDTO> instanceReqList) {
        Set<Long> uniqueEarTags = new HashSet<>();
        for (AdoptInstanceCreateReqDTO each : instanceReqList) {
            if (each.getItemId() == null || each.getItemId() <= 0) {
                throw new ClientException("认养项目不能为空");
            }
            if (each.getEarTagNo() == null || each.getEarTagNo() <= 0) {
                throw new ClientException("耳标号格式不正确");
            }
            if (!uniqueEarTags.add(each.getEarTagNo())) {
                throw new ClientException("耳标号不能重复");
            }
        }
    }

    private Integer validateExistingAssignment(List<AdoptInstanceDO> existingInstances, AdoptInstanceAssignReqDTO reqDTO) {
        if (existingInstances.size() != reqDTO.getInstances().size()) {
            throw new ClientException("当前订单已分配牲畜，请勿重复操作");
        }
        boolean ownerMatched = existingInstances.stream().allMatch(each ->
            Objects.equals(each.getFarmerId(), reqDTO.getFarmerId())
                && Objects.equals(each.getOwnerUserId(), reqDTO.getOwnerUserId())
                && Objects.equals(each.getOwnerOrderId(), reqDTO.getOwnerOrderId())
        );
        Set<String> existingKeySet = existingInstances.stream()
            .map(each -> buildInstanceKey(each.getItemId(), each.getEarTagNo()))
            .collect(Collectors.toSet());
        Set<String> requestKeySet = reqDTO.getInstances().stream()
            .map(each -> buildInstanceKey(each.getItemId(), each.getEarTagNo()))
            .collect(Collectors.toSet());
        if (ownerMatched && existingKeySet.equals(requestKeySet)) {
            return existingInstances.size();
        }
        throw new ClientException("当前订单已分配牲畜，请勿重复操作");
    }

    private String buildInstanceKey(Long itemId, Long earTagNo) {
        return itemId + "_" + earTagNo;
    }

    private AdoptInstanceRespDTO buildInstanceResp(
        AdoptInstanceDO instance,
        Map<Long, AdoptItemDO> adoptItemMap,
        Map<Long, Date> latestLogTimeMap,
        Map<Long, OrderSimpleRespDTO> orderSimpleMap
    ) {
        AdoptItemDO adoptItem = adoptItemMap.get(instance.getItemId());
        AdoptInstanceRespDTO response = AdoptInstanceRespDTO.builder()
            .id(instance.getId())
            .itemId(instance.getItemId())
            .earTagNo(instance.getEarTagNo())
            .image(instance.getImage())
            .status(instance.getStatus())
            .ownerOrderId(instance.getOwnerOrderId())
            .orderNo(resolveOrderNo(instance.getOwnerOrderId(), orderSimpleMap))
            .itemTitle(adoptItem == null ? "认养项目" : adoptItem.getTitle())
            .itemCoverImage(adoptItem == null ? null : adoptItem.getCoverImage())
            .statusDesc(resolveStatusDesc(instance.getStatus()))
            .latestLogTime(latestLogTimeMap.get(Long.valueOf(instance.getId())))
            .deathTime(instance.getDeathTime())
            .deathReason(instance.getDeathReason())
            .build();
        response.setCreateTime(instance.getCreateTime());
        response.setUpdateTime(instance.getUpdateTime());
        return response;
    }

    private Map<Long, OrderSimpleRespDTO> buildOrderSimpleMap(List<AdoptInstanceDO> records) {
        List<Long> orderIds = records.stream()
            .map(AdoptInstanceDO::getOwnerOrderId)
            .filter(Objects::nonNull)
            .filter(each -> each > 0)
            .distinct()
            .toList();
        if (CollUtil.isEmpty(orderIds)) {
            return Map.of();
        }
        var result = orderRemoteService.listSimpleOrdersByIds(orderIds);
        if (result == null || !result.isSuccess() || CollUtil.isEmpty(result.getData())) {
            return Map.of();
        }
        return result.getData().stream()
            .filter(Objects::nonNull)
            .filter(each -> each.getId() != null)
            .collect(Collectors.toMap(OrderSimpleRespDTO::getId, each -> each, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, AdoptItemDO> buildAdoptItemMap(List<AdoptInstanceDO> records) {
        List<Long> itemIds = records.stream()
            .map(AdoptInstanceDO::getItemId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (CollUtil.isEmpty(itemIds)) {
            return Map.of();
        }
        return adoptItemMapper.selectBatchIds(itemIds).stream()
            .collect(Collectors.toMap(AdoptItemDO::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, Date> buildLatestLogTimeMap(List<AdoptInstanceDO> records) {
        List<Long> instanceIds = records.stream()
            .map(AdoptInstanceDO::getId)
            .filter(Objects::nonNull)
            .map(Long::valueOf)
            .distinct()
            .toList();
        if (CollUtil.isEmpty(instanceIds)) {
            return Map.of();
        }
        List<AdoptLogDO> logs = adoptLogMapper.selectList(new QueryWrapper<AdoptLogDO>()
            .lambda()
            .in(AdoptLogDO::getInstanceId, instanceIds)
            .eq(AdoptLogDO::getDelFlag, 0));
        if (CollUtil.isEmpty(logs)) {
            return Map.of();
        }
        return logs.stream()
            .filter(item -> item.getCreateTime() != null)
            .collect(Collectors.toMap(
                AdoptLogDO::getInstanceId,
                AdoptLogDO::getCreateTime,
                (left, right) -> left.after(right) ? left : right
            ));
    }

    private Date getLatestLogTime(Long instanceId) {
        AdoptLogDO latestLog = adoptLogMapper.selectOne(new QueryWrapper<AdoptLogDO>()
            .lambda()
            .eq(AdoptLogDO::getInstanceId, instanceId)
            .eq(AdoptLogDO::getDelFlag, 0)
            .orderByDesc(AdoptLogDO::getCreateTime)
            .last("limit 1"));
        return latestLog == null ? null : latestLog.getCreateTime();
    }

    private String resolveOrderNo(Long ownerOrderId, Map<Long, OrderSimpleRespDTO> orderSimpleMap) {
        if (ownerOrderId == null || ownerOrderId <= 0 || orderSimpleMap == null || orderSimpleMap.isEmpty()) {
            return null;
        }
        OrderSimpleRespDTO orderSimple = orderSimpleMap.get(ownerOrderId);
        return orderSimple == null ? null : orderSimple.getOrderNo();
    }

    private AdoptInstanceFulfillRespDTO buildFulfillResp(Long ownerOrderId) {
        Long unfulfilledCount = baseMapper.selectCount(new LambdaQueryWrapper<AdoptInstanceDO>()
            .eq(AdoptInstanceDO::getOwnerOrderId, ownerOrderId)
            .eq(AdoptInstanceDO::getDelFlag, 0)
            .ne(AdoptInstanceDO::getStatus, LivestockStatusEnum.FULFILLED.getCode()));
        return AdoptInstanceFulfillRespDTO.builder()
            .ownerOrderId(ownerOrderId)
            .allFulfilled(unfulfilledCount == null || unfulfilledCount == 0)
            .build();
    }

    private AdoptInstanceDO getAndCheckReadableInstance(Long instanceId) {
        AdoptInstanceDO instance = getInstanceById(instanceId);
        long userId = StpUtil.getLoginIdAsLong();
        if (StpUtil.hasRole(UserRoleConstant.FARMER_DESC)) {
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

    private AdoptInstanceDO getAndCheckFarmerOwnedInstance(Long currentFarmerId, Long instanceId) {
        AdoptInstanceDO instance = getInstanceById(instanceId);
        if (!Objects.equals(instance.getFarmerId(), currentFarmerId)) {
            throw new ClientException("无权操作该养殖实例");
        }
        return instance;
    }

    private AdoptInstanceDO getInstanceById(Long instanceId) {
        if (instanceId == null || instanceId <= 0) {
            throw new ClientException("养殖实例不存在");
        }
        AdoptInstanceDO instance = baseMapper.selectOne(new LambdaQueryWrapper<AdoptInstanceDO>()
            .eq(AdoptInstanceDO::getId, instanceId)
            .eq(AdoptInstanceDO::getDelFlag, 0));
        if (instance == null) {
            throw new ClientException("养殖实例不存在");
        }
        return instance;
    }

    private String resolveStatusDesc(Integer status) {
        LivestockStatusEnum livestockStatusEnum = LivestockStatusEnum.getByCode(status);
        return livestockStatusEnum == null ? "未知状态" : livestockStatusEnum.getDesc();
    }
}
