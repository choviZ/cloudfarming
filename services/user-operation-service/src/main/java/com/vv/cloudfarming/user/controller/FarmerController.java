package com.vv.cloudfarming.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.user.dto.req.FarmerApplyReqDO;
import com.vv.cloudfarming.user.dto.req.UpdateReviewStatusReqDTO;
import com.vv.cloudfarming.user.dto.resp.FarmerReviewRespDTO;
import com.vv.cloudfarming.user.service.FarmerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 农户控制层
 */
@Tag(name = "农户（商家）控制层")
@RestController
@RequiredArgsConstructor
public class FarmerController {

    private final FarmerService farmerService;

    @Operation(summary = "农户入驻申请")
    @SaCheckLogin
    @PostMapping("/v1/farmer")
    public Result<Void> submitApply(@RequestBody FarmerApplyReqDO requestParam) {
        farmerService.submitApply(requestParam);
        return Results.success();
    }

    @Operation(summary = "获取审核状态")
    @SaCheckLogin
    @GetMapping("/v1/farmer/status")
    public Result<FarmerReviewRespDTO> getReviewStatus(){
       return Results.success(farmerService.getReviewStatus());
    }

    @Operation(summary = "修改审核状态")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/admin/v1/review-status")
    public Result<Void> updateReviewState(@RequestBody UpdateReviewStatusReqDTO requestParam){
        farmerService.updateReviewState(requestParam);
        return Results.success();
    }
}
