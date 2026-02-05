package com.vv.cloudfarming.user.controller;

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
 * 收货地址控制层
 */
@Tag(name = "用户收货地址管理")
@RestController
@RequiredArgsConstructor
public class ReceiveAddressController {

    private final ReceiveAddressService receiveAddressService;

    @Operation(summary = "添加收货地址")
    @PostMapping("/api/receive-address/add")
    public Result<Boolean> addReceiveAddress(@RequestBody ReceiveAddressAddReqDTO requestParam) {
        return Results.success(receiveAddressService.addReceiveAddress(requestParam));
    }

    @Operation(summary = "修改收货地址")
    @PostMapping("/api/receive-address/update")
    public Result<Boolean> updateReceiveAddress(@RequestBody ReceiveAddressUpdateReqDTO requestParam) {
        return Results.success(receiveAddressService.updateReceiveAddress(requestParam));
    }

    @Operation(summary = "设置默认收货地址")
    @PostMapping("/api/receive-address/default")
    public Result<Boolean> setDefaultReceiveAddress(@RequestBody ReceiveAddressSetDefaultReqDTO requestParam) {
        return Results.success(receiveAddressService.setDefaultReceiveAddress(requestParam));
    }

    @Operation(summary = "删除收货地址")
    @PostMapping("/api/receive-address/delete")
    public Result<Boolean> deleteReceiveAddress(@RequestParam Long id) {
        return Results.success(receiveAddressService.deleteReceiveAddress(id));
    }

    @Operation(summary = "根据ID获取收货地址详情")
    @GetMapping("/api/receive-address/get")
    public Result<ReceiveAddressRespDTO> getReceiveAddressById(@RequestParam Long id) {
        return Results.success(receiveAddressService.getReceiveAddressById(id));
    }

    @Operation(summary = "获取当前登录用户的所有收货地址")
    @GetMapping("/api/receive-address/list")
    public Result<List<ReceiveAddressRespDTO>> getCurrentUserReceiveAddresses() {
        return Results.success(receiveAddressService.getCurrentUserReceiveAddresses());
    }

    @Operation(summary = "获取当前登录用户的默认收货地址")
    @GetMapping("/api/receive-address/default")
    public Result<ReceiveAddressRespDTO> getCurrentUserDefaultReceiveAddress() {
        return Results.success(receiveAddressService.getCurrentUserDefaultReceiveAddress());
    }
}