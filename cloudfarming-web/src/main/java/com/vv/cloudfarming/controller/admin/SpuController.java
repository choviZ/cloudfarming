package com.vv.cloudfarming.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.constant.UserRoleConstant;
import com.vv.cloudfarming.shop.dto.req.SpuAttrValueCreateReqDTO;
import com.vv.cloudfarming.shop.dto.req.SpuAttrValueUpdateReqDTO;
import com.vv.cloudfarming.shop.dto.req.SpuCreateOrUpdateReqDTO;
import com.vv.cloudfarming.shop.dto.req.SpuPageQueryReqDTO;
import com.vv.cloudfarming.shop.dto.resp.SpuAttrValueRespDTO;
import com.vv.cloudfarming.shop.dto.resp.SpuRespDTO;
import com.vv.cloudfarming.shop.service.SpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SPU控制层
 */
@Tag(name = "SPU操作")
@RestController
@RequiredArgsConstructor
public class SpuController {

    private final SpuService spuService;

    // spu相关接口
    @Operation(summary = "创建或修改SPU")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @PostMapping("/v1/spu/save-or-update")
    public Result<Void> saveOrUpdateSpu(@Validated @RequestBody SpuCreateOrUpdateReqDTO requestParam) {
        spuService.saveOrUpdateSpu(requestParam);
        return Results.success();
    }

    @Operation(summary = "根据id删除SPU")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @DeleteMapping("/v1/spu/{id}")
    public Result<Void> deleteSpuById(@PathVariable("id") Long id) {
        spuService.deleteSpuById(id);
        return Results.success();
    }

    @Operation(summary = "根据id获取单个SPU详情")
    @GetMapping("/v1/spu/{id}")
    public Result<SpuRespDTO> getSpuById(@PathVariable("id") Long id) {
        SpuRespDTO spu = spuService.getSpuById(id);
        return Results.success(spu);
    }

    @Operation(summary = "分页查询SPU列表")
    @PostMapping("/v1/spu/page")
    public Result<IPage<SpuRespDTO>> listSpuByPage(@RequestBody SpuPageQueryReqDTO queryParam) {
        IPage<SpuRespDTO> pageResult = spuService.listSpuByPage(queryParam);
        return Results.success(pageResult);
    }

    @Operation(summary = "更新SPU状态")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @PutMapping("/v1/spu/status")
    public Result<Void> updateSpuStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        spuService.updateSpuStatus(id, status);
        return Results.success();
    }

    // spu属性相关接口

    @Operation(summary = "创建SPU属性值")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @PostMapping("/v1/spu/attr")
    public Result<Void> createSpuAttrValue(@Validated @RequestBody SpuAttrValueCreateReqDTO requestParam) {
        spuService.createSpuAttrValue(requestParam);
        return Results.success();
    }

    @Operation(summary = "批量创建SPU属性值")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @PostMapping("/v1/spu/attr/batch")
    public Result<Void> batchCreateSpuAttrValues(@Validated @RequestBody List<SpuAttrValueCreateReqDTO> requestParams) {
        spuService.batchCreateSpuAttrValues(requestParams);
        return Results.success();
    }

    @Operation(summary = "更新SPU属性值")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @PutMapping("/v1/spu/attr")
    public Result<Boolean> updateSpuAttrValue(@Validated @RequestBody SpuAttrValueUpdateReqDTO requestParam) {
        return Results.success(spuService.updateSpuAttrValue(requestParam));
    }

    @Operation(summary = "删除SPU属性值")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @DeleteMapping("/v1/spu/attr")
    public Result<Boolean> deleteSpuAttrValue(@PathVariable("id") Long id) {
        return Results.success(spuService.deleteSpuAttrValue(id));
    }

    @Operation(summary = "批量删除SPU属性值")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @DeleteMapping("/v1/spu/attr/batch")
    public Result<Boolean> batchDeleteSpuAttrValues(@RequestBody List<Long> ids) {
        return Results.success(spuService.batchDeleteSpuAttrValues(ids));
    }

    @Operation(summary = "根据SPU ID删除所有属性值")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @DeleteMapping("/v1/spu/attr/all")
    public Result<Boolean> deleteSpuAttrValuesBySpuId(@RequestParam("spuId") Long spuId) {
        return Results.success(spuService.deleteSpuAttrValuesBySpuId(spuId));
    }

    @Operation(summary = "根据ID查询SPU属性值")
    @GetMapping("/v1/spu/attr")
    public Result<SpuAttrValueRespDTO> getSpuAttrValueById(@PathVariable("id") Long id) {
        return Results.success(spuService.getSpuAttrValueById(id));
    }

    @Operation(summary = "根据SPU ID查询属性值列表")
    @GetMapping("/v1/spu/attr/list")
    public Result<List<SpuAttrValueRespDTO>> listSpuAttrValuesBySpuId(@RequestParam("spuId") Long spuId) {
        return Results.success(spuService.listSpuAttrValuesBySpuId(spuId));
    }

    @Operation(summary = "根据SPU ID和属性ID查询属性值")
    @GetMapping("/v1/spu/attr/id")
    public Result<SpuAttrValueRespDTO> getSpuAttrValueBySpuIdAndAttrId(
            @RequestParam("spuId") Long spuId,
            @RequestParam("attrId") Long attrId) {
        return Results.success(spuService.getSpuAttrValueBySpuIdAndAttrId(spuId, attrId));
    }
}
