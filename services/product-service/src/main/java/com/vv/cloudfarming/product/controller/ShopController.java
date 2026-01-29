package com.vv.cloudfarming.product.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.product.dto.resp.ShopInfoRespDTO;
import com.vv.cloudfarming.product.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "店铺控制层")
@RestController
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @Operation(summary = "根据id查询店铺信息")
    @SaCheckLogin
    @GetMapping("/v1/shop")
    public Result<ShopInfoRespDTO> getShopInfoById(@RequestParam @NotNull Long shopId) {
        return Results.success(shopService.getShopInfo(shopId));
    }
}
