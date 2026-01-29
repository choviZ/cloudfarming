package com.vv.cloudfarming.product.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.product.dto.req.SkuCreateReqDTO;
import com.vv.cloudfarming.product.service.SkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商品SKU控制层")
@RestController
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;

    @Operation(summary = "创建SKU")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @PostMapping("/v1/sku")
    public Result<Void> createSku(@RequestBody SkuCreateReqDTO requestParam){
        skuService.createSku(requestParam);
        return Results.success();
    }
}
