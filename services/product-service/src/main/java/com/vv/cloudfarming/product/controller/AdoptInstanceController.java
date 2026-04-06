package com.vv.cloudfarming.product.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.product.dto.req.AdoptInstanceAssignReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptInstancePageReqDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptInstanceRespDTO;
import com.vv.cloudfarming.product.service.AdoptInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "养殖实例控制层")
@RestController
@RequiredArgsConstructor
public class AdoptInstanceController {

    private final AdoptInstanceService adoptInstanceService;

    @Operation(summary = "查询我的养殖实例列表")
    @PostMapping("/api/adopt/instance/v1/my")
    public Result<IPage<AdoptInstanceRespDTO>> queryMyInstance(@RequestBody AdoptInstancePageReqDTO requestParam) {
        return Results.success(adoptInstanceService.queryMyAdoptInstances(requestParam));
    }

    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "为认养订单分配牲畜")
    @PostMapping("/api/adopt/instance/v1/assign")
    public Result<Integer> assignAdoptInstances(@RequestBody @Valid AdoptInstanceAssignReqDTO requestParam) {
        Long farmerId = StpUtil.getLoginIdAsLong();
        return Results.success(adoptInstanceService.assignAdoptInstances(farmerId, requestParam));
    }
}
