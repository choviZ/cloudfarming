package com.vv.cloudfarming.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.user.dto.req.FarmerApplyReqDO;
import com.vv.cloudfarming.user.dto.resp.FarmerReviewRespDTO;
import com.vv.cloudfarming.user.service.FarmerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 农户控制层
 */
@RestController
@RequiredArgsConstructor
public class FarmerController {

    private final FarmerService farmerService;

    /**
     * 农户入驻申请
     */
    @SaCheckLogin
    @PostMapping("v1/farmer")
    public Result<Void> submitApply(@RequestBody FarmerApplyReqDO requestParam) {
        farmerService.submitApply(requestParam);
        return Results.success();
    }

    /**
     * 获取审核状态
     */
    @SaCheckLogin
    @GetMapping("v1/farmer/status")
    public Result<FarmerReviewRespDTO> getReviewStatus(){
       return Results.success(farmerService.getReviewStatus());
    }
}
