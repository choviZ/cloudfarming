package com.vv.cloudfarming.product.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.vv.cloudfarming.product.dto.req.CartItemAddReqDTO;
import com.vv.cloudfarming.product.dto.req.CartItemUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.CartRespDTO;
import com.vv.cloudfarming.product.service.CartService;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "购物车控制层")
@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "添加商品到购物车")
    @SaCheckLogin
    @PostMapping("/items")
    public Result<Boolean> addToCart(@Valid @RequestBody CartItemAddReqDTO requestParam) {
        return Results.success(cartService.addToCart(requestParam));
    }

    @Operation(summary = "更新购物车商品")
    @SaCheckLogin
    @PutMapping("/items")
    public Result<Boolean> updateCartItem(@Valid @RequestBody CartItemUpdateReqDTO requestParam) {
        return Results.success(cartService.updateCartItem(requestParam));
    }

    @Operation(summary = "删除购物车商品")
    @SaCheckLogin
    @DeleteMapping("/items/{skuId}")
    public Result<Boolean> removeFromCart(@PathVariable @NotNull String skuId) {
        return Results.success(cartService.removeFromCart(skuId));
    }

    @Operation(summary = "批量删除购物车商品")
    @SaCheckLogin
    @DeleteMapping("/items")
    public Result<Boolean> batchRemoveFromCart(@RequestBody @NotEmpty List<String> skuIds) {
        return Results.success(cartService.batchRemoveFromCart(skuIds));
    }

    @Operation(summary = "清空购物车")
    @SaCheckLogin
    @DeleteMapping("/clear")
    public Result<Boolean> clearCart() {
        return Results.success(cartService.clearCart());
    }

    @Operation(summary = "获取购物车")
    @SaCheckLogin
    @GetMapping("")
    public Result<CartRespDTO> getCart() {
        return Results.success(cartService.getCart());
    }

    @Operation(summary = "选择/取消选择购物车商品")
    @SaCheckLogin
    @PutMapping("/items/{skuId}/select")
    public Result<Boolean> selectCartItem(
            @PathVariable @NotNull String skuId,
            @RequestParam Boolean selected) {
        return Results.success(cartService.selectCartItem(skuId, selected));
    }

    @Operation(summary = "全选/取消全选购物车商品")
    @SaCheckLogin
    @PutMapping("/items/select-all")
    public Result<Boolean> selectAllCartItems(@Parameter(description = "是否全选") @RequestParam Boolean selected) {
        return Results.success(cartService.selectAllCartItems(selected));
    }
}