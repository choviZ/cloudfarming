package com.vv.cloudfarming.controller.user;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.constant.UserRoleConstant;
import com.vv.cloudfarming.shop.dto.req.AdoptItemCreateReqDTO;
import com.vv.cloudfarming.shop.dto.req.AdoptItemPageReqDTO;
import com.vv.cloudfarming.shop.dto.req.AdoptItemUpdateReqDTO;
import com.vv.cloudfarming.shop.dto.resp.AdoptItemRespDTO;
import com.vv.cloudfarming.shop.service.AdoptItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认养项目控制器
 */
@Tag(name = "认养项目操作")
@RestController
@RequiredArgsConstructor
public class AdoptItemController {

    private final AdoptItemService adoptItemService;

    /**
     * 创建认养项目
     *
     * @param reqDTO 创建认养项目请求DTO
     * @return 认养项目ID
     */
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "创建认养项目")
    @PostMapping("/v1/adopt-item")
    public Result<Long> createAdoptItem(@RequestBody AdoptItemCreateReqDTO reqDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Results.success(adoptItemService.createAdoptItem(userId, reqDTO));
    }

    /**
     * 修改认养项目基础信息
     *
     * @param reqDTO 更新认养项目请求DTO
     * @return 操作结果
     */
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "修改认养项目基础信息")
    @PutMapping("/v1/adopt-item")
    public Result<Void> updateAdoptItem(@RequestBody AdoptItemUpdateReqDTO reqDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        adoptItemService.updateAdoptItem(userId, reqDTO);
        return Results.success();
    }

    /**
     * 上架认养项目
     *
     * @param adoptItemId 认养项目ID
     * @return 操作结果
     */
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "上架认养项目")
    @PutMapping("/v1/adopt-item/{adoptItemId}/on-shelf")
    public Result<Void> onShelfAdoptItem(@PathVariable Long adoptItemId) {
        Long userId = StpUtil.getLoginIdAsLong();
        adoptItemService.updateAdoptItemStatus(userId, adoptItemId, 1);
        return Results.success();
    }

    /**
     * 下架认养项目
     *
     * @param adoptItemId 认养项目ID
     * @return 操作结果
     */
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "下架认养项目")
    @PutMapping("/v1/adopt-item/{adoptItemId}/off-shelf")
    public Result<Void> offShelfAdoptItem(@PathVariable Long adoptItemId) {
        Long userId = StpUtil.getLoginIdAsLong();
        adoptItemService.updateAdoptItemStatus(userId, adoptItemId, 0);
        return Results.success();
    }

    /**
     * 删除认养项目
     *
     * @param adoptItemId 认养项目ID
     * @return 操作结果
     */
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "删除认养项目")
    @DeleteMapping("/v1/adopt-item/{adoptItemId}")
    public Result<Void> deleteAdoptItem(@PathVariable Long adoptItemId) {
        Long userId = StpUtil.getLoginIdAsLong();
        adoptItemService.deleteAdoptItem(userId, adoptItemId);
        return Results.success();
    }

    /**
     * 查询单个认养项目详情
     *
     * @param adoptItemId 认养项目ID
     * @return 认养项目详情
     */
    @Operation(summary = "查询单个认养项目详情")
    @GetMapping("/v1/adopt-item/{adoptItemId}")
    public Result<AdoptItemRespDTO> getAdoptItemDetail(@PathVariable Long adoptItemId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Results.success(adoptItemService.getAdoptItemDetail(userId, adoptItemId));
    }

    /**
     * 分页查询认养项目
     *
     * @param reqDTO 分页查询请求DTO
     * @return 分页查询结果
     */
    @Operation(summary = "分页查询认养项目")
    @PostMapping("/v1/adopt-item/page")
    public Result<IPage<AdoptItemRespDTO>> pageAdoptItems(@RequestBody AdoptItemPageReqDTO reqDTO) {
        return Results.success(adoptItemService.pageAdoptItems(reqDTO));
    }

    /**
     * 查询我的发布（分页）
     *
     * @param reqDTO 分页查询请求DTO
     * @return 分页查询结果
     */
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @Operation(summary = "查询我的发布")
    @PostMapping("/v1/adopt-item/my/page")
    public Result<IPage<AdoptItemRespDTO>> pageMyAdoptItems(@RequestBody AdoptItemPageReqDTO reqDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        // 设置用户ID，查询我的发布
        reqDTO.setUserId(userId);
        return Results.success(adoptItemService.pageAdoptItems(reqDTO));
    }
}
