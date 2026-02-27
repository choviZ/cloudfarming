package com.vv.cloudfarming.product.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.product.dao.mapper.AdoptItemMapper;
import com.vv.cloudfarming.product.dto.req.AdoptItemCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptItemPageReqDTO;
import com.vv.cloudfarming.product.dto.req.AdoptItemUpdateReqDTO;
import com.vv.cloudfarming.product.dto.req.LockStockReqDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptItemRespDTO;
import com.vv.cloudfarming.product.service.AdoptItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "认养项目控制层")
@RestController
@RequiredArgsConstructor
public class AdoptItemController {

    private final AdoptItemService adoptItemService;
    private final AdoptItemMapper adoptItemMapper;

    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "创建认养项目")
    @PostMapping("/api/adopt/item/v1/create")
    public Result<Long> createAdoptItem(@RequestBody AdoptItemCreateReqDTO reqDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Results.success(adoptItemService.createAdoptItem(userId, reqDTO));
    }

    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "修改认养项目基础信息")
    @PutMapping("/api/adopt/item/v1/update")
    public Result<Void> updateAdoptItem(@RequestBody AdoptItemUpdateReqDTO reqDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        adoptItemService.updateAdoptItem(userId, reqDTO);
        return Results.success();
    }

    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "上架认养项目")
    @PutMapping("/api/adopt/item/v1/{adoptItemId}/on-shelf")
    public Result<Void> onShelfAdoptItem(@PathVariable Long adoptItemId) {
        Long userId = StpUtil.getLoginIdAsLong();
        adoptItemService.updateAdoptItemStatus(userId, adoptItemId, 1);
        return Results.success();
    }

    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "下架认养项目")
    @PutMapping("/api/adopt/item/v1/{adoptItemId}/off-shelf")
    public Result<Void> offShelfAdoptItem(@PathVariable Long adoptItemId) {
        Long userId = StpUtil.getLoginIdAsLong();
        adoptItemService.updateAdoptItemStatus(userId, adoptItemId, 0);
        return Results.success();
    }

    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "删除认养项目")
    @DeleteMapping("/api/adopt/item/v1/{adoptItemId}")
    public Result<Void> deleteAdoptItem(@PathVariable Long adoptItemId) {
        Long userId = StpUtil.getLoginIdAsLong();
        adoptItemService.deleteAdoptItem(userId, adoptItemId);
        return Results.success();
    }

    @Operation(summary = "查询单个认养项目详情")
    @GetMapping("/api/adopt/item/v1/{adoptItemId}")
    public Result<AdoptItemRespDTO> getAdoptItemDetail(@PathVariable Long adoptItemId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Results.success(adoptItemService.getAdoptItemDetail(userId, adoptItemId));
    }

    @Operation(summary = "批量查询认养项目详情")
    @PostMapping("/api/adopt/item/v1/batch")
    public Result<List<AdoptItemRespDTO>> batchAdoptItemByIds(@RequestBody List<Long> ids){
        if (ids == null || ids.isEmpty()) {
            return Results.success(new java.util.ArrayList<>());
        }
        List<AdoptItemRespDTO> result = adoptItemService.getBaseMapper().selectBatchIds(ids).stream()
                .map(each -> BeanUtil.toBean(each, AdoptItemRespDTO.class))
                .collect(Collectors.toList());
        return Results.success(result);
    }

    @Operation(summary = "分页查询认养项目")
    @PostMapping("/api/adopt/item/v1/page")
    public Result<IPage<AdoptItemRespDTO>> pageAdoptItems(@RequestBody AdoptItemPageReqDTO reqDTO) {
        return Results.success(adoptItemService.pageAdoptItems(reqDTO));
    }

    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "查询我的发布")
    @PostMapping("/api/adopt/item/v1/my/page")
    public Result<IPage<AdoptItemRespDTO>> pageMyAdoptItems(@RequestBody AdoptItemPageReqDTO reqDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        // 设置用户ID，查询我的发布
        reqDTO.setUserId(userId);
        return Results.success(adoptItemService.pageAdoptItems(reqDTO));
    }

    @Operation(summary = "锁定库存")
    @PostMapping("/api/adopt/item/v1/stock/lock")
    public Result<Integer> lockStock(@RequestBody LockStockReqDTO requestParam) {
        int updated = adoptItemMapper.lockStock(requestParam.getQuantity(), requestParam.getId());
        return Results.success(updated);
    }

    @Operation(summary = "释放锁定库存")
    @PostMapping("/api/adopt/item/v1/stock/unlock")
    public Result<Integer> unlockStock(@RequestBody LockStockReqDTO requestParam) {
        int updated = adoptItemMapper.unlockStock(requestParam.getQuantity(), requestParam.getId());
        return Results.success(updated);
    }

    @Operation(summary = "扣减库存")
    @PostMapping("/api/adopt/item/v1/stock/deduct")
    public Result<Integer> deductStock(@RequestBody LockStockReqDTO requestParam) {
        int updated = adoptItemMapper.deductStock(requestParam.getQuantity(), requestParam.getId());
        return Results.success(updated);
    }
}
