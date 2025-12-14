package com.vv.cloudfarming.controller.admin;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.user.dto.req.UpdateReviewStatusReqDTO;
import com.vv.cloudfarming.user.service.FarmerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 农户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class FarmerAdminController {

    private final FarmerService farmerService;

    /**
     * 修改审核状态
     */
    @PostMapping("admin/v1/review-status")
    public Result<Void> updateReviewState(@RequestBody UpdateReviewStatusReqDTO requestParam, HttpServletRequest request){
        farmerService.updateReviewState(requestParam,request);
        return Results.success();
    }
}
