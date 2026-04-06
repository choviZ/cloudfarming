package com.vv.cloudfarming.product.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.product.dto.req.AdoptLogCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptLogPageReqDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptLogRespDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptWeightPointRespDTO;
import com.vv.cloudfarming.product.service.AdoptLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Tag(name = "养殖日志控制层")
@RestController
@RequiredArgsConstructor
public class AdoptLogController {

    private final AdoptLogService adoptLogService;

    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "上传养殖日志")
    @PostMapping("/api/adopt/log/v1/create")
    public Result<Void> createAdoptLog(@RequestBody @Valid AdoptLogCreateReqDTO requestParam) {
        adoptLogService.createAdoptLog(requestParam);
        return Results.success();
    }

    @Operation(summary = "分页查询养殖日志")
    @PostMapping("/api/adopt/log/v1/page")
    public Result<IPage<AdoptLogRespDTO>> pageAdoptLogs(@RequestBody AdoptLogPageReqDTO requestParam) {
        return Results.success(adoptLogService.pageAdoptLogs(requestParam));
    }

    @Operation(summary = "查询体重变化趋势")
    @GetMapping("/api/adopt/log/v1/weight-trend")
    public Result<List<AdoptWeightPointRespDTO>> listWeightTrend(@RequestParam @NotNull Long instanceId) {
        return Results.success(adoptLogService.listWeightTrend(instanceId));
    }
}
