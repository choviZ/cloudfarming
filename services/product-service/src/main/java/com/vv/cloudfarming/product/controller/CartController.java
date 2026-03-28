package com.vv.cloudfarming.product.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.product.dto.req.CartItemAddReqDTO;
import com.vv.cloudfarming.product.dto.req.CartItemSelectedReqDTO;
import com.vv.cloudfarming.product.dto.req.CartItemUpdateReqDTO;
import com.vv.cloudfarming.product.dto.req.CartSelectAllReqDTO;
import com.vv.cloudfarming.product.dto.resp.CartCheckoutPreviewRespDTO;
import com.vv.cloudfarming.product.dto.resp.CartRespDTO;
import com.vv.cloudfarming.product.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "购物车控制层")
@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "添加商品到购物车")
    @SaCheckLogin
    @PostMapping("/api/cart/items")
    public Result<Boolean> addToCart(@Valid @RequestBody CartItemAddReqDTO requestParam) {
        return Results.success(cartService.addToCart(requestParam));
    }

    @Operation(summary = "修改购物车商品数量")
    @SaCheckLogin
    @PutMapping("/api/cart/items/{skuId}")
    public Result<Boolean> updateCartItem(@PathVariable @NotNull Long skuId,
                                          @Valid @RequestBody CartItemUpdateReqDTO requestParam) {
        return Results.success(cartService.updateCartItem(skuId, requestParam));
    }

    @Operation(summary = "删除购物车商品")
    @SaCheckLogin
    @DeleteMapping("/api/cart/items/{skuId}")
    public Result<Boolean> removeFromCart(@PathVariable @NotNull Long skuId) {
        return Results.success(cartService.removeFromCart(skuId));
    }

    @Operation(summary = "批量删除购物车商品")
    @SaCheckLogin
    @PostMapping("/api/cart/items/delete-batch")
    public Result<Boolean> batchRemoveFromCart(@RequestBody @NotEmpty List<Long> skuIds) {
        return Results.success(cartService.batchRemoveFromCart(skuIds));
    }

    @Operation(summary = "清空购物车")
    @SaCheckLogin
    @DeleteMapping("/api/cart")
    public Result<Boolean> clearCart() {
        return Results.success(cartService.clearCart());
    }

    @Operation(summary = "获取购物车")
    @SaCheckLogin
    @GetMapping("/api/cart")
    public Result<CartRespDTO> getCart() {
        return Results.success(cartService.getCart());
    }

    @Operation(summary = "选择或取消选择购物车商品")
    @SaCheckLogin
    @PostMapping("/api/cart/items/{skuId}/selected")
    public Result<Boolean> selectCartItem(@PathVariable @NotNull Long skuId,
                                          @Valid @RequestBody CartItemSelectedReqDTO requestParam) {
        return Results.success(cartService.selectCartItem(skuId, requestParam.getSelected()));
    }

    @Operation(summary = "全选或取消全选购物车商品")
    @SaCheckLogin
    @PostMapping("/api/cart/select-all")
    public Result<Boolean> selectAllCartItems(@Valid @RequestBody CartSelectAllReqDTO requestParam) {
        return Results.success(cartService.selectAllCartItems(requestParam.getSelected()));
    }

    @Operation(summary = "获取购物车结算预览")
    @SaCheckLogin
    @GetMapping("/api/cart/checkout-preview")
    public Result<CartCheckoutPreviewRespDTO> getCheckoutPreview() {
        return Results.success(cartService.getCheckoutPreview());
    }
}
