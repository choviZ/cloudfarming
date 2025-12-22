package com.vv.cloudfarming.controller.user;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.user.dto.req.ReceiveAddressAddReqDTO;
import com.vv.cloudfarming.user.dto.req.ReceiveAddressSetDefaultReqDTO;
import com.vv.cloudfarming.user.dto.req.ReceiveAddressUpdateReqDTO;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import com.vv.cloudfarming.user.service.ReceiveAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户收货地址控制层
 */
@Tag(name = "用户收货地址管理")
@RestController
@RequiredArgsConstructor
public class ReceiveAddressController {

    private final ReceiveAddressService receiveAddressService;

    /**
     * 添加收货地址
     */
    @Operation(summary = "添加收货地址")
    @PostMapping("/v1/user/receive-address")
    public Result<Boolean> addReceiveAddress(@RequestBody ReceiveAddressAddReqDTO requestParam) {
        return Results.success(receiveAddressService.addReceiveAddress(requestParam));
    }

    /**
     * 修改收货地址
     */
    @Operation(summary = "修改收货地址")
    @PutMapping("/v1/user/receive-address")
    public Result<Boolean> updateReceiveAddress(@RequestBody ReceiveAddressUpdateReqDTO requestParam) {
        return Results.success(receiveAddressService.updateReceiveAddress(requestParam));
    }

    /**
     * 设置默认收货地址
     */
    @Operation(summary = "设置默认收货地址")
    @PutMapping("/v1/user/receive-address/default")
    public Result<Boolean> setDefaultReceiveAddress(@RequestBody ReceiveAddressSetDefaultReqDTO requestParam) {
        return Results.success(receiveAddressService.setDefaultReceiveAddress(requestParam));
    }

    /**
     * 删除收货地址
     */
    @Operation(summary = "删除收货地址")
    @DeleteMapping("/v1/user/receive-address/{id}")
    public Result<Boolean> deleteReceiveAddress(@PathVariable Long id) {
        return Results.success(receiveAddressService.deleteReceiveAddress(id));
    }

    /**
     * 根据ID获取收货地址详情
     */
    @Operation(summary = "根据ID获取收货地址详情")
    @GetMapping("/v1/user/receive-address/{id}")
    public Result<ReceiveAddressRespDTO> getReceiveAddressById(@PathVariable Long id) {
        return Results.success(receiveAddressService.getReceiveAddressById(id));
    }

    /**
     * 获取当前登录用户的所有收货地址
     */
    @Operation(summary = "获取当前登录用户的所有收货地址")
    @GetMapping("/v1/user/receive-address")
    public Result<List<ReceiveAddressRespDTO>> getCurrentUserReceiveAddresses() {
        return Results.success(receiveAddressService.getCurrentUserReceiveAddresses());
    }

    /**
     * 获取当前登录用户的默认收货地址
     */
    @Operation(summary = "获取当前登录用户的默认收货地址")
    @GetMapping("/v1/user/receive-address/default")
    public Result<ReceiveAddressRespDTO> getCurrentUserDefaultReceiveAddress() {
        return Results.success(receiveAddressService.getCurrentUserDefaultReceiveAddress());
    }
}