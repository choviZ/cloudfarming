package com.vv.cloudfarming.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.constant.UserRoleConstant;
import com.vv.cloudfarming.shop.dto.resp.ShopInfoRespDTO;
import com.vv.cloudfarming.shop.service.ShopService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺控制层
 */
@RestController
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    /**
     * 查询店铺信息
     */
    @SaCheckLogin
    @GetMapping("/v1/shop")
    public Result<ShopInfoRespDTO> getShopInfo(@RequestParam @NotNull Long shopId) {
        return Results.success(shopService.getShopInfo(shopId));
    }

    /**
     * 查询我的店铺
     */
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @GetMapping("/v1/shop/my")
    public Result<ShopInfoRespDTO> getMyShopInfo() {
        return Results.success(shopService.getMyShopInfo());
    }
}
