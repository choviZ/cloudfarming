package com.vv.cloudfarming.product.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckOr;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.product.dto.req.ShopUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.ShopRespDTO;
import com.vv.cloudfarming.product.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "店铺控制层")
@RestController
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @Operation(summary = "根据id查询店铺信息")
    @SaCheckLogin
    @GetMapping("/api/shop/get")
    public Result<ShopRespDTO> getShopInfoById(@RequestParam @NotNull Long shopId) {
        return Results.success(shopService.getShopInfo(shopId));
    }

    @Operation(summary = "更新店铺信息")
    @SaCheckOr(
            role = {@SaCheckRole(UserRoleConstant.FARMER_DESC), @SaCheckRole(UserRoleConstant.ADMIN_DESC)}
    )
    @PostMapping("/api/shop/update")
    public Result<Void> updateShop(@Validated @RequestBody ShopUpdateReqDTO requestParam) {
        shopService.updateShop(requestParam);
        return Results.success();
    }
}
