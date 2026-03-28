package com.vv.cloudfarming.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.enums.UserRoleEnum;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.user.dao.entity.FeedbackDO;
import com.vv.cloudfarming.user.dao.entity.UserDO;
import com.vv.cloudfarming.user.dao.mapper.FeedbackMapper;
import com.vv.cloudfarming.user.dao.mapper.UserMapper;
import com.vv.cloudfarming.user.dto.req.FeedbackPageQueryReqDTO;
import com.vv.cloudfarming.user.dto.req.FeedbackProcessReqDTO;
import com.vv.cloudfarming.user.dto.req.FeedbackSubmitReqDTO;
import com.vv.cloudfarming.user.dto.resp.FeedbackRespDTO;
import com.vv.cloudfarming.user.dto.resp.FeedbackTypeRespDTO;
import com.vv.cloudfarming.user.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, FeedbackDO> implements FeedbackService {

    private static final Integer STATUS_PENDING = 0;
    private static final Integer STATUS_HANDLED = 1;

    private static final LinkedHashMap<String, String> FEEDBACK_TYPE_MAP = new LinkedHashMap<>();
    private static final LinkedHashMap<Integer, String> FEEDBACK_STATUS_MAP = new LinkedHashMap<>();

    static {
        FEEDBACK_TYPE_MAP.put("PRODUCT", "商品问题");
        FEEDBACK_TYPE_MAP.put("ADOPT", "认养项目问题");
        FEEDBACK_TYPE_MAP.put("ORDER", "订单/支付");
        FEEDBACK_TYPE_MAP.put("LOGISTICS", "物流/售后");
        FEEDBACK_TYPE_MAP.put("SHOP_SERVICE", "店铺/服务");
        FEEDBACK_TYPE_MAP.put("ACCOUNT", "账号/登录");
        FEEDBACK_TYPE_MAP.put("COMPLAINT", "投诉举报");
        FEEDBACK_TYPE_MAP.put("SUGGESTION", "功能建议");
        FEEDBACK_TYPE_MAP.put("OTHER", "其他");

        FEEDBACK_STATUS_MAP.put(STATUS_PENDING, "待处理");
        FEEDBACK_STATUS_MAP.put(STATUS_HANDLED, "已处理");
    }

    private final UserMapper userMapper;

    @Override
    public List<FeedbackTypeRespDTO> listFeedbackTypes() {
        List<FeedbackTypeRespDTO> result = new ArrayList<>();
        FEEDBACK_TYPE_MAP.forEach((code, name) -> result.add(new FeedbackTypeRespDTO(code, name)));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitFeedback(FeedbackSubmitReqDTO requestParam) {
        validateFeedbackType(requestParam.getFeedbackType());

        Long userId = StpUtil.getLoginIdAsLong();
        UserDO loginUser = userMapper.selectById(userId);
        if (loginUser == null) {
            throw new ClientException("请先登录");
        }
        if (ObjectUtil.equals(loginUser.getUserType(), UserRoleConstant.ADMIN_CODE)) {
            throw new ClientException("管理员账号无需提交意见反馈");
        }

        FeedbackDO feedbackDO = BeanUtil.toBean(requestParam, FeedbackDO.class);
        feedbackDO.setUserId(userId);
        feedbackDO.setSubmitterType(loginUser.getUserType());
        feedbackDO.setStatus(STATUS_PENDING);

        int inserted = baseMapper.insert(feedbackDO);
        if (inserted < 1) {
            throw new ServiceException("提交意见反馈失败");
        }
        return true;
    }

    @Override
    public IPage<FeedbackRespDTO> pageAdminFeedback(FeedbackPageQueryReqDTO requestParam) {
        if (StrUtil.isNotBlank(requestParam.getFeedbackType())) {
            validateFeedbackType(requestParam.getFeedbackType());
        }

        LambdaQueryWrapper<FeedbackDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ObjectUtil.isNotNull(requestParam.getId()), FeedbackDO::getId, requestParam.getId())
                .eq(ObjectUtil.isNotNull(requestParam.getSubmitterType()), FeedbackDO::getSubmitterType, requestParam.getSubmitterType())
                .eq(StrUtil.isNotBlank(requestParam.getFeedbackType()), FeedbackDO::getFeedbackType, requestParam.getFeedbackType())
                .eq(ObjectUtil.isNotNull(requestParam.getStatus()), FeedbackDO::getStatus, requestParam.getStatus())
                .like(StrUtil.isNotBlank(requestParam.getKeyword()), FeedbackDO::getContent, requestParam.getKeyword())
                .like(StrUtil.isNotBlank(requestParam.getContactPhone()), FeedbackDO::getContactPhone, requestParam.getContactPhone())
                .orderByAsc(FeedbackDO::getStatus)
                .orderByDesc(FeedbackDO::getCreateTime);

        IPage<FeedbackDO> pageResult = baseMapper.selectPage(requestParam, wrapper);
        return convertPage(pageResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean processFeedback(FeedbackProcessReqDTO requestParam) {
        FeedbackDO feedbackDO = baseMapper.selectById(requestParam.getId());
        if (feedbackDO == null) {
            throw new ClientException("反馈记录不存在");
        }

        FeedbackDO updateDO = new FeedbackDO();
        updateDO.setId(requestParam.getId());
        updateDO.setStatus(STATUS_HANDLED);
        updateDO.setReplyContent(StrUtil.trim(requestParam.getReplyContent()));
        updateDO.setHandlerId(StpUtil.getLoginIdAsLong());
        updateDO.setHandleTime(new Date());

        int updated = baseMapper.updateById(updateDO);
        if (updated < 1) {
            throw new ServiceException("处理意见反馈失败");
        }
        return true;
    }

    private IPage<FeedbackRespDTO> convertPage(IPage<FeedbackDO> pageResult) {
        List<FeedbackDO> records = pageResult.getRecords();
        if (CollUtil.isEmpty(records)) {
            return pageResult.convert(each -> BeanUtil.toBean(each, FeedbackRespDTO.class));
        }

        Set<Long> relatedUserIds = records.stream()
                .flatMap(each -> Stream.of(each.getUserId(), each.getHandlerId()))
                .filter(ObjectUtil::isNotNull)
                .collect(Collectors.toSet());

        Map<Long, UserDO> userMap = relatedUserIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(relatedUserIds).stream()
                .collect(Collectors.toMap(UserDO::getId, Function.identity(), (left, right) -> left));

        return pageResult.convert(each -> convertRecord(each, userMap));
    }

    private FeedbackRespDTO convertRecord(FeedbackDO feedbackDO, Map<Long, UserDO> userMap) {
        FeedbackRespDTO respDTO = BeanUtil.toBean(feedbackDO, FeedbackRespDTO.class);
        respDTO.setFeedbackTypeName(FEEDBACK_TYPE_MAP.getOrDefault(feedbackDO.getFeedbackType(), feedbackDO.getFeedbackType()));
        respDTO.setStatusName(FEEDBACK_STATUS_MAP.getOrDefault(feedbackDO.getStatus(), "未知状态"));

        UserRoleEnum submitterRole = UserRoleEnum.fromCode(feedbackDO.getSubmitterType());
        if (submitterRole != null) {
            respDTO.setSubmitterTypeName(submitterRole.getDescription());
        }

        UserDO submitter = userMap.get(feedbackDO.getUserId());
        if (submitter != null) {
            respDTO.setUsername(submitter.getUsername());
        }

        UserDO handler = userMap.get(feedbackDO.getHandlerId());
        if (handler != null) {
            respDTO.setHandlerName(handler.getUsername());
        }

        return respDTO;
    }

    private void validateFeedbackType(String feedbackType) {
        if (!FEEDBACK_TYPE_MAP.containsKey(feedbackType)) {
            throw new ClientException("问题类型错误");
        }
    }
}
