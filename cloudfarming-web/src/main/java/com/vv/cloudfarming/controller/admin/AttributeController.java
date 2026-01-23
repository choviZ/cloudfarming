package com.vv.cloudfarming.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.shop.dto.req.AttributeCreateReqDTO;
import com.vv.cloudfarming.shop.dto.req.AttributeUpdateReqDTO;
import com.vv.cloudfarming.shop.dto.resp.AttributeRespDTO;
import com.vv.cloudfarming.shop.service.AttributeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 属性控制层
 */
@Tag(name = "属性操作")
@RestController
@RequiredArgsConstructor
public class AttributeController {

    private final AttributeService attributeService;

    /**
     * 创建属性
     */
    @Operation(summary = "创建属性")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/v1/attribute")
    public Result<Void> createAttribute(@RequestBody AttributeCreateReqDTO requestParam) {
        attributeService.createAttribute(requestParam);
        return Results.success();
    }

    /**
     * 更新属性
     */
    @Operation(summary = "更新属性")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PutMapping("/v1/attribute")
    public Result<Boolean> updateAttribute(@RequestBody AttributeUpdateReqDTO requestParam) {
        return Results.success(attributeService.updateAttribute(requestParam));
    }

    /**
     * 删除属性
     */
    @Operation(summary = "删除属性")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @DeleteMapping("/v1/attribute")
    public Result<Boolean> deleteAttribute(@RequestParam @NotNull Long id) {
        return Results.success(attributeService.deleteAttribute(id));
    }

    /**
     * 批量删除属性
     */
    @Operation(summary = "批量删除属性")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @DeleteMapping("/v1/attribute/batch")
    public Result<Boolean> batchDeleteAttributes(@RequestBody List<Long> ids) {
        return Results.success(attributeService.batchDeleteAttributes(ids));
    }

    /**
     * 根据ID查询属性详情
     */
    @Operation(summary = "根据ID查询属性详情")
    @GetMapping("/v1/attribute")
    public Result<AttributeRespDTO> getAttributeById(@RequestParam @NotNull Long id) {
        return Results.success(attributeService.getAttributeById(id));
    }

    /**
     * 根据分类ID查询属性列表
     */
    @Operation(summary = "根据分类ID查询属性列表")
    @GetMapping("/v1/attribute/by-category")
    public Result<List<AttributeRespDTO>> getAttributesByCategoryId(@RequestParam @NotNull Long categoryId) {
        return Results.success(attributeService.getAttributesByCategoryId(categoryId));
    }

    /**
     * 根据分类ID和属性类型查询属性列表
     */
    @Operation(summary = "根据分类ID和属性类型查询属性列表")
    @GetMapping("/v1/attribute/by-category-and-type")
    public Result<List<AttributeRespDTO>> getAttributesByCategoryIdAndType(
            @RequestParam @NotNull Long categoryId,
            @RequestParam @NotNull Integer attrType) {
        return Results.success(attributeService.getAttributesByCategoryIdAndType(categoryId, attrType));
    }
}
