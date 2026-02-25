package com.vv.cloudfarming.order.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.*;
import com.vv.cloudfarming.order.service.OrderService;
import com.vv.cloudfarming.starter.idempotent.NoDuplicateSubmit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单控制层")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @NoDuplicateSubmit(message = "请勿重复提交订单")
    @PostMapping("/api/order/v1/create")
    public Result<OrderCreateRespDTO> createOrder(@RequestBody @Valid OrderCreateReqDTO requestParam) {
        long userId = StpUtil.getLoginIdAsLong();
        return Results.success(orderService.createOrderV2(userId, requestParam));
    }

    @Operation(summary = "查询订单列表")
    @PostMapping("/api/order/v1/list")
    public Result<IPage<OrderPageWithProductInfoRespDTO>> listOrder(@RequestBody OrderPageReqDTO requestParam) {
        return Results.success(orderService.listOrderWithProductInfo(requestParam));
    }

    @Operation(summary = "查询订单列表（管理员用）")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/order/v1/list/admin")
    public Result<IPage<OrderPageRespDTO>> listOrders(@RequestBody OrderPageReqDTO requestParam) {
        return Results.success(orderService.listOrders(requestParam));
    }

    @Operation(summary = "查询订单详情（委托养殖）")
    @GetMapping("/api/order/v1/detail/adopt")
    public Result<List<AdoptOrderDetailRespDTO>> getAdoptOrderDetail(@RequestParam @NotNull String orderNo){
        return Results.success(orderService.getAdoptOrderDetail(orderNo));
    }

    @Operation(summary = "查询订单详情（普通商品）")
    @GetMapping("/api/order/v1/detail/sku")
    public Result<List<SkuOrderDetailRespDTO>> getSkuOrderDetail(@RequestParam @NotNull String orderNo){
        return Results.success(orderService.getSkuOrderDetail(orderNo));
    }
}
