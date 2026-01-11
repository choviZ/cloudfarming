package com.vv.cloudfarming.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.constant.UserRoleConstant;
import com.vv.cloudfarming.shop.dto.req.SkuCreateReqDTO;
import com.vv.cloudfarming.shop.service.SkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * SKU控制层
 */
@Tag(name = "SKU操作")
@RestController
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;

    @Operation(summary = "创建SKU")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/v1/sku")
    public Result<Void> createSku(@RequestBody SkuCreateReqDTO requestParam){
        skuService.createSku(requestParam);
        return Results.success();
    }
}
