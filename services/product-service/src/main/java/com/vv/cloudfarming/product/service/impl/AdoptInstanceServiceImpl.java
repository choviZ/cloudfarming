package com.vv.cloudfarming.product.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.product.dao.entity.AdoptInstanceDO;
import com.vv.cloudfarming.product.dao.mapper.AdoptInstanceMapper;
import com.vv.cloudfarming.product.dto.req.AdoptInstanceAssignReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptInstanceCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptInstancePageReqDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptInstanceRespDTO;
import com.vv.cloudfarming.product.enums.LivestockStatusEnum;
import com.vv.cloudfarming.product.service.AdoptInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdoptInstanceServiceImpl extends ServiceImpl<AdoptInstanceMapper, AdoptInstanceDO> implements AdoptInstanceService {

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
        return pageResult.convert(item -> BeanUtil.toBean(item, AdoptInstanceRespDTO.class));
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
}
