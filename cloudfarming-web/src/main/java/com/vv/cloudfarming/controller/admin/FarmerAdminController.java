package com.vv.cloudfarming.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.user.dto.req.UpdateReviewStatusReqDTO;
import com.vv.cloudfarming.user.service.FarmerService;
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
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("admin/v1/review-status")
    public Result<Void> updateReviewState(@RequestBody UpdateReviewStatusReqDTO requestParam){
        farmerService.updateReviewState(requestParam);
        return Results.success();
    }
}
