package com.vv.cloudfarming.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.product.dao.entity.AuditRecordDO;
import com.vv.cloudfarming.product.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.product.dao.entity.SpuDO;
import com.vv.cloudfarming.product.dao.mapper.AuditRecordMapper;
import com.vv.cloudfarming.product.dao.mapper.AdoptItemMapper;
import com.vv.cloudfarming.product.dao.mapper.SpuMapper;
import com.vv.cloudfarming.product.dto.req.AuditApproveReqDTO;
import com.vv.cloudfarming.product.dto.req.AuditPageQueryReqDTO;
import com.vv.cloudfarming.product.dto.req.AuditRejectReqDTO;
import com.vv.cloudfarming.product.dto.req.AuditSubmitReqDTO;
import com.vv.cloudfarming.product.dto.resp.AuditRecordRespDTO;
import com.vv.cloudfarming.product.enums.AuditStatusEnum;
import com.vv.cloudfarming.product.enums.ProductTypeEnum;
import com.vv.cloudfarming.product.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 审核服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl extends ServiceImpl<AuditRecordMapper, AuditRecordDO> implements AuditService {

    private final SpuMapper spuMapper;
    private final AdoptItemMapper adoptItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitAudit(Long submitterId, AuditSubmitReqDTO requestParam) {
        // 校验业务类型
        ProductTypeEnum bizType = ProductTypeEnum.of(requestParam.getBizType());
        if (bizType == null) {
            throw new ClientException("业务类型不存在");
        }

        // 查询商品是否存在
        validateBizExists(bizType, requestParam.getBizId());

        // 检查是否存在待审核的记录
        AuditRecordDO pendingRecord = getPendingRecord(bizType.getCode(), requestParam.getBizId());
        if (pendingRecord != null) {
            throw new ClientException("该商品正在审核中，请勿重复提交");
        }

        // 创建审核记录
        AuditRecordDO auditRecord = AuditRecordDO.builder()
                .bizType(bizType.getCode())
                .bizId(requestParam.getBizId())
                .auditStatus(AuditStatusEnum.PENDING.getCode())
                .submitterId(submitterId)
                .build();

        // 保存审核记录
        this.save(auditRecord);

        log.info("提交审核成功，auditId={}, bizType={}, bizId={}, submitterId={}",
                auditRecord.getId(), bizType.getCode(), requestParam.getBizId(), submitterId);
        return auditRecord.getId();
    }

    @Override
    public IPage<AuditRecordRespDTO> pageAuditRecords(AuditPageQueryReqDTO requestParam) {
        // 构建查询条件
        LambdaQueryWrapper<AuditRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(requestParam.getBizType() != null, AuditRecordDO::getBizType, requestParam.getBizType())
                .eq(requestParam.getAuditStatus() != null, AuditRecordDO::getAuditStatus, requestParam.getAuditStatus())
                .eq(requestParam.getSubmitterId() != null, AuditRecordDO::getSubmitterId, requestParam.getSubmitterId())
                .orderByDesc(AuditRecordDO::getCreateTime);

        // 分页查询
        Page<AuditRecordDO> page = new Page<>(requestParam.getCurrent(), requestParam.getSize());
        IPage<AuditRecordDO> recordPage = this.page(page, queryWrapper);

        // 转换为响应DTO
        IPage<AuditRecordRespDTO> result = recordPage.convert(record -> {
            return BeanUtil.toBean(record, AuditRecordRespDTO.class);
        });

        // 批量获取商品标题
        fillBizTitle(result.getRecords());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveAudit(Long auditorId, AuditApproveReqDTO requestParam) {
        // 查询审核记录
        AuditRecordDO auditRecord = this.getById(requestParam.getAuditId());
        if (auditRecord == null) {
            throw new ClientException("审核记录不存在");
        }

        // 校验审核状态
        if (!AuditStatusEnum.PENDING.getCode().equals(auditRecord.getAuditStatus())) {
            throw new ClientException("该审核记录不是待审核状态，无法操作");
        }

        // 更新审核记录
        auditRecord.setAuditStatus(AuditStatusEnum.APPROVED.getCode());
        auditRecord.setAuditorId(auditorId);
        auditRecord.setAuditTime(new Date());
        this.updateById(auditRecord);

        // 更新业务表的审核状态
        updateBizAuditStatus(auditRecord.getBizType(), auditRecord.getBizId(), AuditStatusEnum.APPROVED.getCode());

        log.info("审核通过，auditId={}, bizType={}, bizId={}, auditorId={}",
                auditRecord.getId(), auditRecord.getBizType(), auditRecord.getBizId(), auditorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectAudit(Long auditorId, AuditRejectReqDTO requestParam) {
        // 查询审核记录
        AuditRecordDO auditRecord = this.getById(requestParam.getAuditId());
        if (auditRecord == null) {
            throw new ClientException("审核记录不存在");
        }

        // 校验审核状态
        if (!AuditStatusEnum.PENDING.getCode().equals(auditRecord.getAuditStatus())) {
            throw new ClientException("该审核记录不是待审核状态，无法操作");
        }

        // 更新审核记录
        auditRecord.setAuditStatus(AuditStatusEnum.REJECTED.getCode());
        auditRecord.setAuditorId(auditorId);
        auditRecord.setAuditTime(new Date());
        auditRecord.setRejectReason(requestParam.getRejectReason());
        this.updateById(auditRecord);

        // 更新业务表的审核状态
        updateBizAuditStatus(auditRecord.getBizType(), auditRecord.getBizId(), AuditStatusEnum.REJECTED.getCode());

        log.info("审核拒绝，auditId={}, bizType={}, bizId={}, auditorId={}, reason={}",
                auditRecord.getId(), auditRecord.getBizType(), auditRecord.getBizId(), auditorId, requestParam.getRejectReason());
    }

    @Override
    public AuditRecordDO getLatestAuditRecord(Integer bizType, Long bizId) {
        LambdaQueryWrapper<AuditRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AuditRecordDO::getBizType, bizType)
                .eq(AuditRecordDO::getBizId, bizId)
                .orderByDesc(AuditRecordDO::getCreateTime)
                .last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public void updateBizAuditStatus(Integer bizType, Long bizId, Integer auditStatus) {
        ProductTypeEnum bizTypeEnum = ProductTypeEnum.of(bizType);
        if (bizTypeEnum == null) {
            throw new ClientException("业务类型不存在");
        }

        switch (bizTypeEnum) {
            case ADOPT:
                // 更新认养项目表的审核状态
                LambdaUpdateWrapper<AdoptItemDO> adoptUpdateWrapper = new LambdaUpdateWrapper<>();
                adoptUpdateWrapper.eq(AdoptItemDO::getId, bizId)
                        .set(AdoptItemDO::getAuditStatus, auditStatus);
                adoptItemMapper.update(adoptUpdateWrapper);
                break;

            case SPU:
                // 更新农产品表的审核状态
                LambdaUpdateWrapper<SpuDO> spuUpdateWrapper = new LambdaUpdateWrapper<>();
                spuUpdateWrapper.eq(SpuDO::getId, bizId)
                        .set(SpuDO::getAuditStatus, auditStatus);
                spuMapper.update(spuUpdateWrapper);
                break;
            default:
                throw new ClientException("不支持的业务类型");
        }
    }

    /**
     * 校验业务数据是否存在
     */
    private void validateBizExists(ProductTypeEnum bizType, Long bizId) {
        boolean exists = false;
        switch (bizType) {
            case ADOPT:
                exists = adoptItemMapper.selectById(bizId) != null;
                break;
            case SPU:
                exists = spuMapper.selectById(bizId) != null;
                break;
        }

        if (!exists) {
            throw new ClientException("商品不存在");
        }
    }

    /**
     * 获取待审核记录
     */
    private AuditRecordDO getPendingRecord(Integer bizType, Long bizId) {
        LambdaQueryWrapper<AuditRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AuditRecordDO::getBizType, bizType)
                .eq(AuditRecordDO::getBizId, bizId)
                .eq(AuditRecordDO::getAuditStatus, AuditStatusEnum.PENDING.getCode())
                .last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 填充商品标题
     */
    private void fillBizTitle(List<AuditRecordRespDTO> records) {
        if (CollUtil.isEmpty(records)) {
            return;
        }

        // 按业务类型分组
        Map<Integer, List<AuditRecordRespDTO>> bizTypeMap = records.stream()
                .collect(Collectors.groupingBy(AuditRecordRespDTO::getBizType));

        // 处理认养项目
        List<AuditRecordRespDTO> adoptRecords = bizTypeMap.get(ProductTypeEnum.ADOPT.getCode());
        if (CollUtil.isNotEmpty(adoptRecords)) {
            Set<Long> adoptIds = adoptRecords.stream()
                    .map(AuditRecordRespDTO::getBizId)
                    .collect(Collectors.toSet());
            List<AdoptItemDO> adoptItems = adoptItemMapper.selectBatchIds(adoptIds);
            Map<Long, String> adoptTitleMap = adoptItems.stream()
                    .collect(Collectors.toMap(AdoptItemDO::getId, AdoptItemDO::getTitle));
            adoptRecords.forEach(r -> r.setBizTitle(adoptTitleMap.get(r.getBizId())));
        }

        // 处理农产品
        List<AuditRecordRespDTO> spuRecords = bizTypeMap.get(ProductTypeEnum.SPU.getCode());
        if (CollUtil.isNotEmpty(spuRecords)) {
            Set<Long> spuIds = spuRecords.stream()
                    .map(AuditRecordRespDTO::getBizId)
                    .collect(Collectors.toSet());
            List<SpuDO> spus = spuMapper.selectBatchIds(spuIds);
            Map<Long, String> spuTitleMap = spus.stream()
                    .collect(Collectors.toMap(SpuDO::getId, SpuDO::getTitle));
            spuRecords.forEach(r -> r.setBizTitle(spuTitleMap.get(r.getBizId())));
        }
    }
}
