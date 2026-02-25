package com.vv.cloudfarming.product.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.product.dao.mapper.SkuMapper;
import com.vv.cloudfarming.product.dto.req.SkuCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.LockStockReqDTO;
import com.vv.cloudfarming.product.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.product.service.SkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品SKU控制层")
@RestController
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;
    private final SkuMapper skuMapper;

    @Operation(summary = "创建SKU")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @PostMapping("/api/sku/create")
    public Result<Void> createSku(@RequestBody SkuCreateReqDTO requestParam){
        skuService.createSku(requestParam);
        return Results.success();
    }

    @Operation(summary = "更新SKU状态")
    @PostMapping("/api/sku/status")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    public Result<Void> updateSkuStatus(@RequestParam Long id,@RequestParam Integer status){
        skuService.updateSkuStatus(id, status);
        return Results.success();
    }

    @Operation(summary = "根据id集合获取sku详情列表")
    @PostMapping("/api/sku/list")
    public Result<List<SkuRespDTO>> listSkuDetailsByIds(@RequestBody List<Long> ids){
        return Results.success(skuService.listSkuDetailsByIds(ids));
    }

    @Operation(summary = "扣减库存")
    @PostMapping("/api/sku/stock/lock")
    public Result<Integer> decreaseStock(@RequestBody LockStockReqDTO requestParam){
        int updated = skuMapper.lockSkuStock(requestParam.getQuantity(), requestParam.getId());
        return Results.success(updated);
    }
}
