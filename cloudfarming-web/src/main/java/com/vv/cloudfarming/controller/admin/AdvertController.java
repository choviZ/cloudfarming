package com.vv.cloudfarming.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.operation.dto.req.AdvertCreateReqDTO;
import com.vv.cloudfarming.operation.dto.req.AdvertPageQueryReqDTO;
import com.vv.cloudfarming.operation.dto.req.AdvertUpdateReqDTO;
import com.vv.cloudfarming.operation.dto.resp.AdvertRespDTO;
import com.vv.cloudfarming.operation.service.AdvertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 广告控制层
 */
@Tag(name = "广告管理")
@RestController
@RequiredArgsConstructor
public class AdvertController {

    private final AdvertService advertService;

    /**
     * 查询展示的广告
     */
    @Operation(summary = "查询展示的广告")
    @GetMapping("/v1/show/adverts")
    public Result<List<AdvertRespDTO>> getShowAdverts(){
        return Results.success(advertService.getShowAdverts());
    }

    /**
     * 创建广告
     */
    @Operation(summary = "创建广告")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/v1/advert")
    public Result<Void> createAdvert(@RequestBody AdvertCreateReqDTO requestParam) {
        advertService.createAdvert(requestParam);
        return Results.success();
    }

    /**
     * 根据id查询广告
     */
    @Operation(summary = "根据id查询广告")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @GetMapping("/v1/advert")
    public Result<AdvertRespDTO> getAdvertById(@RequestParam @NotNull Integer id) {
        return Results.success(advertService.getAdvertById(id));
    }

    /**
     * 修改广告信息
     */
    @Operation(summary = "修改广告信息")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PutMapping("/v1/advert")
    public Result<Boolean> updateAdvert(@RequestBody AdvertUpdateReqDTO requestParam) {
        return Results.success(advertService.updateAdvert(requestParam));
    }

    /**
     * 删除广告
     */
    @Operation(summary = "删除广告")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @DeleteMapping("/v1/advert")
    public Result<Boolean> deleteAdvert(@RequestParam @NotNull Integer id) {
        return Results.success(advertService.deleteAdvert(id));
    }

    /**
     * 分页查询广告列表
     */
    @Operation(summary = "分页查询广告列表")
    @PostMapping("/v1/advert/page")
    public Result<IPage<AdvertRespDTO>> pageAdvert(@RequestBody AdvertPageQueryReqDTO requestParam) {
        return Results.success(advertService.pageAdvert(requestParam));
    }
}