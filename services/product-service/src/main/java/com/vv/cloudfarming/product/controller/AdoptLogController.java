package com.vv.cloudfarming.product.controller;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.product.dto.req.AdoptLogCreateReqDTO;
import com.vv.cloudfarming.product.service.AdoptLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "养殖日志控制层")
@RestController
@RequiredArgsConstructor
public class AdoptLogController {

    private final AdoptLogService adoptLogService;

    @Operation(summary = "上传养殖日志")
    @PostMapping("/api/adopt/log/v1/create")
    public Result<Void> createAdoptLog(@RequestBody AdoptLogCreateReqDTO requestParam) {
        adoptLogService.createAdoptLog(requestParam);
        return Results.success();
    }
}
