package com.vv.cloudfarming.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.user.dto.req.FarmerAdminPageReqDTO;
import com.vv.cloudfarming.user.dto.req.FarmerApplyReqDO;
import com.vv.cloudfarming.user.dto.req.FarmerFeatureUpdateReqDTO;
import com.vv.cloudfarming.user.dto.req.FarmerShowcasePageReqDTO;
import com.vv.cloudfarming.user.dto.req.FarmerShowcaseUpdateReqDTO;
import com.vv.cloudfarming.user.dto.req.UpdateReviewStatusReqDTO;
import com.vv.cloudfarming.user.dto.resp.FarmerAdminPageRespDTO;
import com.vv.cloudfarming.user.dto.resp.FarmerMyShowcaseRespDTO;
import com.vv.cloudfarming.user.dto.resp.FarmerReviewRespDTO;
import com.vv.cloudfarming.user.dto.resp.FarmerShowcaseRespDTO;
import com.vv.cloudfarming.user.service.FarmerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Farmer controller.
 */
@Tag(name = "农户控制器")
@Validated
@RestController
@RequiredArgsConstructor
public class FarmerController {

    private final FarmerService farmerService;

    @Operation(summary = "农户入驻申请")
    @SaCheckLogin
    @PostMapping("/api/farmer/apply")
    public Result<Void> submitApply(@RequestBody FarmerApplyReqDO requestParam) {
        farmerService.submitApply(requestParam);
        return Results.success();
    }

    @Operation(summary = "获取农户审核状态")
    @SaCheckLogin
    @GetMapping("/api/farmer/status")
    public Result<FarmerReviewRespDTO> getReviewStatus() {
        return Results.success(farmerService.getReviewStatus());
    }

    @Operation(summary = "修改审核状态")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/farmer/review-status")
    public Result<Void> updateReviewState(@Valid @RequestBody UpdateReviewStatusReqDTO requestParam) {
        farmerService.updateReviewState(requestParam);
        return Results.success();
    }

    @Operation(summary = "分页查询优质农户")
    @PostMapping("/api/farmer/showcase/page")
    public Result<IPage<FarmerShowcaseRespDTO>> pageShowcase(@Valid @RequestBody FarmerShowcasePageReqDTO requestParam) {
        return Results.success(farmerService.pageShowcase(requestParam));
    }

    @Operation(summary = "查询优质农户详情")
    @GetMapping("/api/farmer/showcase/detail")
    public Result<FarmerShowcaseRespDTO> getShowcaseDetail(@RequestParam("id") Long id) {
        return Results.success(farmerService.getShowcaseDetail(id));
    }

    @Operation(summary = "获取我的展示资料")
    @SaCheckLogin
    @GetMapping("/api/farmer/showcase/my")
    public Result<FarmerMyShowcaseRespDTO> getMyShowcase() {
        return Results.success(farmerService.getMyShowcase());
    }

    @Operation(summary = "更新我的展示资料")
    @SaCheckLogin
    @PostMapping("/api/farmer/showcase/update")
    public Result<Boolean> updateMyShowcase(@Valid @RequestBody FarmerShowcaseUpdateReqDTO requestParam) {
        return Results.success(farmerService.updateMyShowcase(requestParam));
    }

    @Operation(summary = "管理端分页查询农户")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/farmer/admin/page")
    public Result<IPage<FarmerAdminPageRespDTO>> pageAdminFarmers(@Valid @RequestBody FarmerAdminPageReqDTO requestParam) {
        return Results.success(farmerService.pageAdminFarmers(requestParam));
    }

    @Operation(summary = "管理端切换精选状态")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/farmer/admin/feature")
    public Result<Boolean> updateFeaturedFlag(@Valid @RequestBody FarmerFeatureUpdateReqDTO requestParam) {
        return Results.success(farmerService.updateFeaturedFlag(requestParam));
    }
}
