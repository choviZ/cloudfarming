package com.vv.cloudfarming.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.user.dao.entity.FarmerDO;
import com.vv.cloudfarming.user.dto.req.FarmerApplyReqDO;
import com.vv.cloudfarming.user.dto.req.UpdateReviewStatusReqDTO;
import com.vv.cloudfarming.user.dto.resp.FarmerReviewRespDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 农户操作服务层
 */
public interface FarmerService extends IService<FarmerDO> {

    /**
     * 提交农户入住申请
     *
     * @param requestParam 请求参数
     */
    void submitApply(FarmerApplyReqDO requestParam, HttpServletRequest request);

    /**
     * 获取审核状态
     *
     * @param request http请求
     * @return 审核状态
     */
    FarmerReviewRespDTO getReviewStatus(HttpServletRequest request);

    /**
     * 修改审核状态
     * @param requestParam 请求参数
     * @param request http请求
     */
    void updateReviewState(UpdateReviewStatusReqDTO requestParam, HttpServletRequest request);
}
