package com.vv.cloudfarming.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.shop.dto.req.CategoryCreateReqDTO;
import com.vv.cloudfarming.shop.dto.req.CategoryUpdateReqDTO;
import com.vv.cloudfarming.shop.dto.resp.CategoryRespDTO;
import com.vv.cloudfarming.shop.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制层
 */
@Tag(name = "商品分类操作")
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 创建商品分类
     */
    @Operation(summary = "创建商品分类")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/v1/category")
    public Result<Void> createCategory(@RequestBody CategoryCreateReqDTO requestParam) {
        categoryService.createCategory(requestParam);
        return Results.success();
    }

    /**
     * 更新商品分类
     */
    @Operation(summary = "更新商品分类")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PutMapping("/v1/category")
    public Result<Boolean> updateCategory(@RequestBody CategoryUpdateReqDTO requestParam) {
        return Results.success(categoryService.updateCategory(requestParam));
    }

    /**
     * 删除商品分类
     */
    @Operation(summary = "删除商品分类")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @DeleteMapping("/v1/category")
    public Result<Boolean> deleteCategory(@RequestParam @NotNull Long id) {
        return Results.success(categoryService.deleteCategory(id));
    }

    /**
     * 根据ID查询分类详情
     */
    @Operation(summary = "根据ID查询分类详情")
    @GetMapping("/v1/category")
    public Result<CategoryRespDTO> getCategoryById(@RequestParam @NotNull Long id) {
        return Results.success(categoryService.getCategoryById(id));
    }

    /**
     * 获取分类树（支持多级分类）
     */
    @Operation(summary = "获取分类树")
    @GetMapping("/v1/category/tree")
    public Result<List<CategoryRespDTO>> getCategoryTree() {
        return Results.success(categoryService.getCategoryTree());
    }

    /**
     * 获取所有顶级分类
     */
    @Operation(summary = "获取所有顶级分类")
    @GetMapping("/v1/category/top-level")
    public Result<List<CategoryRespDTO>> getTopLevelCategories() {
        return Results.success(categoryService.getTopLevelCategories());
    }

    /**
     * 根据父级ID查询子分类
     */
    @Operation(summary = "根据父级ID查询子分类")
    @GetMapping("/v1/category/children")
    public Result<List<CategoryRespDTO>> getChildrenByParentId(@RequestParam @NotNull Long parentId) {
        return Results.success(categoryService.getChildrenByParentId(parentId));
    }
}
