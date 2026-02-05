package com.vv.cloudfarming.user.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.user.dto.req.AdvertCreateReqDTO;
import com.vv.cloudfarming.user.dto.req.AdvertPageQueryReqDTO;
import com.vv.cloudfarming.user.dto.req.AdvertUpdateReqDTO;
import com.vv.cloudfarming.user.dto.resp.AdvertRespDTO;
import com.vv.cloudfarming.user.service.AdvertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "广告管理控制层")
@RestController
@RequiredArgsConstructor
public class AdvertController {

    private final AdvertService advertService;

    @Operation(summary = "查询展示的广告")
    @GetMapping("/api/advert/show")
    public Result<List<AdvertRespDTO>> getShowAdverts(){
        return Results.success(advertService.getShowAdverts());
    }

    @Operation(summary = "创建广告")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/advert/create")
    public Result<Void> createAdvert(@RequestBody AdvertCreateReqDTO requestParam) {
        advertService.createAdvert(requestParam);
        return Results.success();
    }

    @Operation(summary = "根据id查询广告")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @GetMapping("/api/advert/get")
    public Result<AdvertRespDTO> getAdvertById(@RequestParam @NotNull Integer id) {
        return Results.success(advertService.getAdvertById(id));
    }

    @Operation(summary = "修改广告信息")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/advert/update")
    public Result<Boolean> updateAdvert(@RequestBody AdvertUpdateReqDTO requestParam) {
        return Results.success(advertService.updateAdvert(requestParam));
    }

    @Operation(summary = "删除广告")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/advert/delete")
    public Result<Boolean> deleteAdvert(@RequestParam @NotNull Integer id) {
        return Results.success(advertService.deleteAdvert(id));
    }

    @Operation(summary = "分页查询广告列表")
    @PostMapping("/api/advert/page")
    public Result<IPage<AdvertRespDTO>> pageAdvert(@RequestBody AdvertPageQueryReqDTO requestParam) {
        return Results.success(advertService.pageAdvert(requestParam));
    }
}