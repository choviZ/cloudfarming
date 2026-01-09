package com.vv.cloudfarming.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.constant.UserRoleConstant;
import com.vv.cloudfarming.shop.dto.req.SpuCreateOrUpdateReqDTO;
import com.vv.cloudfarming.shop.dto.req.SpuPageQueryReqDTO;
import com.vv.cloudfarming.shop.dto.resp.SpuRespDTO;
import com.vv.cloudfarming.shop.service.SpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * SPU控制层
 */
@Tag(name = "SPU操作")
@RestController
@RequiredArgsConstructor
public class SpuController {

    private final SpuService spuService;

    @Operation(summary = "创建或修改SPU")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/v1/spu/save-or-update")
    public void saveOrUpdateSpu(@Validated @RequestBody SpuCreateOrUpdateReqDTO requestParam) {
        spuService.saveOrUpdateSpu(requestParam);
    }

    @Operation(summary = "根据id删除SPU")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @DeleteMapping("/v1/spu/{id}")
    public void deleteSpuById(@PathVariable("id") Long id) {
        spuService.deleteSpuById(id);
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
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PutMapping("/v1/spu/status")
    public void updateSpuStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        spuService.updateSpuStatus(id, status);
    }

}
