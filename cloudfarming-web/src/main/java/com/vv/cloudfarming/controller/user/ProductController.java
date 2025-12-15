package com.vv.cloudfarming.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.constant.UserRoleConstant;
import com.vv.cloudfarming.shop.dto.req.*;
import com.vv.cloudfarming.shop.dto.resp.ProductRespDTO;
import com.vv.cloudfarming.shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制层
 */
@Tag(name = "商品操作")
@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 创建商品
     */
    @Operation(summary = "创建商品")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @PostMapping("/v1/product")
    public Result<Void> createProduct(@RequestBody ProductCreateReqDTO requestParam) {
        productService.createProduct(requestParam);
        return Results.success();
    }

    /**
     * 根据id查询商品
     */
    @Operation(summary = "根据id查询商品")
    @SaCheckLogin
    @GetMapping("/v1/product")
    public Result<ProductRespDTO> getProductById(@RequestParam @NotNull Long id) {
        return Results.success(productService.getProductById(id));
    }

    /**
     * 修改商品信息
     */
    @Operation(summary = "修改商品信息")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @PutMapping("/v1/product")
    public Result<Boolean> updateProduct(@RequestBody ProductUpdateReqDTO requestParam) {
        return Results.success(productService.updateProduct(requestParam));
    }

    /**
     * 删除商品
     */
    @Operation(summary = "删除商品")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @DeleteMapping("/v1/product")
    public Result<Boolean> deleteProduct(@RequestParam @NotNull Long id) {
        return Results.success(productService.deleteProduct(id));
    }

    /**
     * 分页查询商品列表
     */
    @Operation(summary = "分页查询商品列表")
    @PostMapping("/v1/product/page")
    public Result<IPage<ProductRespDTO>> pageProductByShopId(@RequestBody ProductPageQueryReqDTO requestParam) {
        return Results.success(productService.PageProductByShopId(requestParam));
    }

    /**
     * 修改商品上/下架状态
     */
    @Operation(summary = "修改商品上/下架状态")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @PutMapping("/v1/product/shelf-status")
    public Result<Void> updateShelfStatus(@RequestBody ProductUpdateShelfStatusRequestDTO requestParam) {
        productService.updateShelfStatus(requestParam);
        return Results.success();
    }
}