package com.vv.cloudfarming.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.user.dto.req.FeedbackPageQueryReqDTO;
import com.vv.cloudfarming.user.dto.req.FeedbackProcessReqDTO;
import com.vv.cloudfarming.user.dto.req.FeedbackSubmitReqDTO;
import com.vv.cloudfarming.user.dto.resp.FeedbackRespDTO;
import com.vv.cloudfarming.user.dto.resp.FeedbackTypeRespDTO;
import com.vv.cloudfarming.user.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "意见反馈控制层")
@Validated
@RestController
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "获取意见反馈类型")
    @SaCheckLogin
    @GetMapping("/api/feedback/types")
    public Result<List<FeedbackTypeRespDTO>> listFeedbackTypes() {
        return Results.success(feedbackService.listFeedbackTypes());
    }

    @Operation(summary = "提交意见反馈")
    @SaCheckLogin
    @PostMapping("/api/feedback/submit")
    public Result<Boolean> submitFeedback(@Valid @RequestBody FeedbackSubmitReqDTO requestParam) {
        return Results.success(feedbackService.submitFeedback(requestParam));
    }

    @Operation(summary = "管理员分页查询意见反馈")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/feedback/page/admin")
    public Result<IPage<FeedbackRespDTO>> pageAdminFeedback(@Valid @RequestBody FeedbackPageQueryReqDTO requestParam) {
        return Results.success(feedbackService.pageAdminFeedback(requestParam));
    }

    @Operation(summary = "管理员处理意见反馈")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/feedback/process")
    public Result<Boolean> processFeedback(@Valid @RequestBody FeedbackProcessReqDTO requestParam) {
        return Results.success(feedbackService.processFeedback(requestParam));
    }
}
