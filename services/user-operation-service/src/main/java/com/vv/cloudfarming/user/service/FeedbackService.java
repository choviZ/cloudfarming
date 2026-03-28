package com.vv.cloudfarming.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.user.dao.entity.FeedbackDO;
import com.vv.cloudfarming.user.dto.req.FeedbackPageQueryReqDTO;
import com.vv.cloudfarming.user.dto.req.FeedbackProcessReqDTO;
import com.vv.cloudfarming.user.dto.req.FeedbackSubmitReqDTO;
import com.vv.cloudfarming.user.dto.resp.FeedbackRespDTO;
import com.vv.cloudfarming.user.dto.resp.FeedbackTypeRespDTO;

import java.util.List;

public interface FeedbackService extends IService<FeedbackDO> {

    /**
     * 获取反馈类型选项
     */
    List<FeedbackTypeRespDTO> listFeedbackTypes();

    /**
     * 提交意见反馈
     */
    Boolean submitFeedback(FeedbackSubmitReqDTO requestParam);

    /**
     * 管理员分页查询意见反馈
     */
    IPage<FeedbackRespDTO> pageAdminFeedback(FeedbackPageQueryReqDTO requestParam);

    /**
     * 管理员处理意见反馈
     */
    Boolean processFeedback(FeedbackProcessReqDTO requestParam);
}
