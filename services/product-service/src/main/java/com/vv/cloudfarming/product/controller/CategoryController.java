package com.vv.cloudfarming.product.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.product.dto.req.CategoryCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.CategoryUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.CategoryRespDTO;
import com.vv.cloudfarming.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制层
 */
@Tag(name = "商品分类控制层")
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 创建商品分类
     */
    @Operation(summary = "创建商品分类")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/category/create")
    public Result<Void> createCategory(@RequestBody CategoryCreateReqDTO requestParam) {
        categoryService.createCategory(requestParam);
        return Results.success();
    }

    /**
     * 更新商品分类
     */
    @Operation(summary = "更新商品分类")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/category/update")
    public Result<Boolean> updateCategory(@RequestBody CategoryUpdateReqDTO requestParam) {
        return Results.success(categoryService.updateCategory(requestParam));
    }

    /**
     * 删除商品分类
     */
    @Operation(summary = "删除商品分类")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @GetMapping("/api/category/delete")
    public Result<Boolean> deleteCategory(@RequestParam @NotNull Long id) {
        return Results.success(categoryService.deleteCategory(id));
    }

    /**
     * 根据ID查询分类详情
     */
    @Operation(summary = "根据ID查询分类详情")
    @GetMapping("/api/category/get")
    public Result<CategoryRespDTO> getCategoryById(@RequestParam @NotNull Long id) {
        return Results.success(categoryService.getCategoryById(id));
    }

    /**
     * 获取分类树（支持多级分类）
     */
    @Operation(summary = "获取分类树")
    @GetMapping("/api/category/tree")
    public Result<List<CategoryRespDTO>> getCategoryTree() {
        return Results.success(categoryService.getCategoryTree());
    }

    /**
     * 获取所有顶级分类
     */
    @Operation(summary = "获取所有顶级分类")
    @GetMapping("/api/category/top-level")
    public Result<List<CategoryRespDTO>> getTopLevelCategories() {
        return Results.success(categoryService.getTopLevelCategories());
    }

    /**
     * 根据父级ID查询子分类
     */
    @Operation(summary = "根据父级ID查询子分类")
    @GetMapping("/api/category/children")
    public Result<List<CategoryRespDTO>> getChildrenByParentId(@RequestParam @NotNull Long parentId) {
        return Results.success(categoryService.getChildrenByParentId(parentId));
    }
}
