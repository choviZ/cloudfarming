package com.vv.cloudfarming.product.controller;

import cn.dev33.satoken.annotation.SaCheckOr;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.product.dto.req.SpuAttrValueCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.SpuAttrValueUpdateReqDTO;
import com.vv.cloudfarming.product.dto.req.SpuCreateOrUpdateReqDTO;
import com.vv.cloudfarming.product.dto.req.SpuPageQueryReqDTO;
import com.vv.cloudfarming.product.dto.resp.SpuAttrValueRespDTO;
import com.vv.cloudfarming.product.dto.resp.SpuDetailRespDTO;
import com.vv.cloudfarming.product.dto.resp.SpuRespDTO;
import com.vv.cloudfarming.product.service.SpuService;
import com.vv.cloudfarming.user.dto.req.UpdateReviewStatusReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品SPU控制层")
@RestController
@RequiredArgsConstructor
public class SpuController {

    private final SpuService spuService;

    // spu相关接口
    @Operation(summary = "创建")
    @SaCheckOr(
            role = {@SaCheckRole(UserRoleConstant.FARMER_DESC), @SaCheckRole(UserRoleConstant.ADMIN_DESC),}
    )
    @PostMapping("/api/spu/create")
    public Result<Long> createSpu(@Validated @RequestBody SpuCreateOrUpdateReqDTO requestParam) {
        Long spuId = spuService.saveSpu(requestParam);
        return Results.success(spuId);
    }

    @Operation(summary = "根据id删除SPU")
    @SaCheckOr(
            role = {@SaCheckRole(UserRoleConstant.FARMER_DESC), @SaCheckRole(UserRoleConstant.ADMIN_DESC),}
    )
    @PostMapping("/api/spu/delete")
    public Result<Void> deleteSpuById(@RequestParam("id") Long id) {
        spuService.deleteSpuById(id);
        return Results.success();
    }

    @Operation(summary = "根据id获取单个SPU详情")
    @GetMapping("/api/spu/get")
    public Result<SpuDetailRespDTO> getSpuById(@RequestParam("id") Long id) {
        SpuDetailRespDTO spu = spuService.getSpuDetail(id);
        return Results.success(spu);
    }

    @Operation(summary = "分页查询SPU列表")
    @PostMapping("/api/spu/page")
    public Result<IPage<SpuRespDTO>> listSpuByPage(@RequestBody SpuPageQueryReqDTO queryParam) {
        IPage<SpuRespDTO> pageResult = spuService.listSpuByPage(queryParam);
        return Results.success(pageResult);
    }

    @Operation(summary = "更新SPU状态")
    @SaCheckOr(
            role = {@SaCheckRole(UserRoleConstant.FARMER_DESC), @SaCheckRole(UserRoleConstant.ADMIN_DESC),}
    )
    @PostMapping("/api/spu/status")
    public Result<Void> updateSpuStatus(@RequestBody UpdateReviewStatusReqDTO requestParam) {
        spuService.updateSpuStatus(requestParam.getId(), requestParam.getStatus());
        return Results.success();
    }

    // spu属性相关接口

    @Operation(summary = "创建SPU属性值")
    @SaCheckOr(
            role = {@SaCheckRole(UserRoleConstant.FARMER_DESC), @SaCheckRole(UserRoleConstant.ADMIN_DESC),}
    )
    @PostMapping("/api/spu/attr/create")
    public Result<Void> createSpuAttrValue(@Validated @RequestBody SpuAttrValueCreateReqDTO requestParam) {
        spuService.createSpuAttrValue(requestParam);
        return Results.success();
    }

    @Operation(summary = "批量创建SPU属性值")
    @SaCheckOr(
            role = {@SaCheckRole(UserRoleConstant.FARMER_DESC), @SaCheckRole(UserRoleConstant.ADMIN_DESC),}
    )
    @PostMapping("/api/spu/attr/create/batch")
    public Result<Void> batchCreateSpuAttrValues(@Validated @RequestBody List<SpuAttrValueCreateReqDTO> requestParams) {
        spuService.batchCreateSpuAttrValues(requestParams);
        return Results.success();
    }

    @Operation(summary = "更新SPU属性值")
    @SaCheckOr(
            role = {@SaCheckRole(UserRoleConstant.FARMER_DESC), @SaCheckRole(UserRoleConstant.ADMIN_DESC),}
    )
    @PostMapping("/api/spu/attr/update")
    public Result<Boolean> updateSpuAttrValue(@Validated @RequestBody SpuAttrValueUpdateReqDTO requestParam) {
        return Results.success(spuService.updateSpuAttrValue(requestParam));
    }

    @Operation(summary = "删除SPU属性值")
    @SaCheckOr(
            role = {@SaCheckRole(UserRoleConstant.FARMER_DESC), @SaCheckRole(UserRoleConstant.ADMIN_DESC),}
    )
    @PostMapping("/api/spu/attr/delete")
    public Result<Boolean> deleteSpuAttrValue(@RequestParam("id") Long id) {
        return Results.success(spuService.deleteSpuAttrValue(id));
    }

    @Operation(summary = "批量删除SPU属性值")
    @SaCheckOr(
            role = {@SaCheckRole(UserRoleConstant.FARMER_DESC), @SaCheckRole(UserRoleConstant.ADMIN_DESC),}
    )
    @PostMapping("/api/spu/attr/delete/batch")
    public Result<Boolean> batchDeleteSpuAttrValues(@RequestBody List<Long> ids) {
        return Results.success(spuService.batchDeleteSpuAttrValues(ids));
    }

    @Operation(summary = "根据SPU ID删除所有属性值")
    @SaCheckOr(
            role = {@SaCheckRole(UserRoleConstant.FARMER_DESC), @SaCheckRole(UserRoleConstant.ADMIN_DESC),}
    )
    @PostMapping("/api/spu/attr/delete/all")
    public Result<Boolean> deleteSpuAttrValuesBySpuId(@RequestParam("spuId") Long spuId) {
        return Results.success(spuService.deleteSpuAttrValuesBySpuId(spuId));
    }

    @Operation(summary = "根据ID查询SPU属性值")
    @GetMapping("/api/spu/attr/get")
    public Result<SpuAttrValueRespDTO> getSpuAttrValueById(@RequestParam("id") Long id) {
        return Results.success(spuService.getSpuAttrValueById(id));
    }

    @Operation(summary = "根据SPU ID查询属性值列表")
    @GetMapping("/v1/spu/attr/list")
    public Result<List<SpuAttrValueRespDTO>> listSpuAttrValuesBySpuId(@RequestParam("spuId") Long spuId) {
        return Results.success(spuService.listSpuAttrValuesBySpuId(spuId));
    }

    @Operation(summary = "根据SPU ID和属性ID查询属性值")
    @GetMapping("/api/spu/attr/id")
    public Result<SpuAttrValueRespDTO> getSpuAttrValueBySpuIdAndAttrId(
            @RequestParam("spuId") Long spuId,
            @RequestParam("attrId") Long attrId) {
        return Results.success(spuService.getSpuAttrValueBySpuIdAndAttrId(spuId, attrId));
    }
}
