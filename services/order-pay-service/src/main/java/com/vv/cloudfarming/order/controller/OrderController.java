package com.vv.cloudfarming.order.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.order.config.AdoptAgreementProperties;
import com.vv.cloudfarming.order.dto.req.OrderAssignAdoptReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderFulfillAdoptReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderPageReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderReceiveReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderShipReqDTO;
import com.vv.cloudfarming.order.dto.req.SeckillCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.AdoptAgreementRespDTO;
import com.vv.cloudfarming.order.dto.resp.AdoptOrderDetailRespDTO;
import com.vv.cloudfarming.order.dto.resp.FarmerOrderStatisticsRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderCreateRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageWithProductInfoRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderSimpleRespDTO;
import com.vv.cloudfarming.order.dto.resp.SkuOrderDetailRespDTO;
import com.vv.cloudfarming.order.service.OrderService;
import com.vv.cloudfarming.starter.idempotent.NoDuplicateSubmit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "订单控制层")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final AdoptAgreementProperties adoptAgreementProperties;
    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @NoDuplicateSubmit(message = "请勿重复提交订单")
    @PostMapping("/api/order/v1/create")
    public Result<OrderCreateRespDTO> createOrder(@RequestBody @Valid OrderCreateReqDTO requestParam) {
        long userId = StpUtil.getLoginIdAsLong();
        return Results.success(orderService.createOrderV2(userId, requestParam));
    }

    @Operation(summary = "秒杀下单")
    @NoDuplicateSubmit(message = "请勿重复提交秒杀订单")
    @PostMapping("/api/order/v1/seckill/create")
    public Result<String> createSeckillOrder(@RequestBody @Valid SeckillCreateReqDTO requestParam) {
        return Results.success(orderService.createSeckillOrder(requestParam));
    }

    @Operation(summary = "获取当前认养协议")
    @GetMapping("/api/order/v1/agreement/adopt/current")
    public Result<AdoptAgreementRespDTO> getCurrentAdoptAgreement() {
        return Results.success(AdoptAgreementRespDTO.builder()
            .version(adoptAgreementProperties.getVersion())
            .title(adoptAgreementProperties.getTitle())
            .content(adoptAgreementProperties.getContent())
            .build());
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

    @Operation(summary = "查询农户订单列表")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @PostMapping("/api/order/v1/farmer/list")
    public Result<IPage<OrderPageRespDTO>> listCurrentFarmerOrders(@RequestBody OrderPageReqDTO requestParam) {
        return Results.success(orderService.listCurrentFarmerOrders(requestParam));
    }

    @Operation(summary = "农户订单发货")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @PostMapping("/api/order/v1/farmer/ship")
    public Result<Void> shipCurrentFarmerOrder(@RequestBody @Valid OrderShipReqDTO requestParam) {
        orderService.shipCurrentFarmerOrder(requestParam);
        return Results.success();
    }

    @Operation(summary = "用户确认收货")
    @PostMapping("/api/order/v1/receive")
    public Result<Void> receiveCurrentUserOrder(@RequestBody @Valid OrderReceiveReqDTO requestParam) {
        orderService.receiveCurrentUserOrder(requestParam);
        return Results.success();
    }

    @Operation(summary = "农户分配认养牲畜")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @PostMapping("/api/order/v1/farmer/adopt/assign")
    public Result<Void> assignCurrentFarmerAdoptOrder(@RequestBody @Valid OrderAssignAdoptReqDTO requestParam) {
        orderService.assignCurrentFarmerAdoptOrder(requestParam);
        return Results.success();
    }

    @Operation(summary = "农户完成认养履约")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @PostMapping("/api/order/v1/farmer/adopt/fulfill")
    public Result<Void> fulfillCurrentFarmerAdoptInstance(@RequestBody @Valid OrderFulfillAdoptReqDTO requestParam) {
        orderService.fulfillCurrentFarmerAdoptInstance(requestParam);
        return Results.success();
    }

    @Operation(summary = "查询订单详情（委托养殖）")
    @GetMapping("/api/order/v1/detail/adopt")
    public Result<List<AdoptOrderDetailRespDTO>> getAdoptOrderDetail(@RequestParam @NotNull String orderNo) {
        return Results.success(orderService.getAdoptOrderDetail(orderNo));
    }

    @Operation(summary = "查询订单详情（普通商品）")
    @GetMapping("/api/order/v1/detail/sku")
    public Result<List<SkuOrderDetailRespDTO>> getSkuOrderDetail(@RequestParam @NotNull String orderNo) {
        return Results.success(orderService.getSkuOrderDetail(orderNo));
    }

    @Operation(summary = "按订单ID批量查询订单简要信息")
    @PostMapping("/api/order/v1/inner/simple/list")
    public Result<List<OrderSimpleRespDTO>> listSimpleOrdersByIds(@RequestBody List<Long> orderIds) {
        return Results.success(orderService.listSimpleOrdersByIds(orderIds));
    }

    @Operation(summary = "查询农户订单统计")
    @SaCheckRole(UserRoleConstant.FARMER_DESC)
    @GetMapping("/api/order/v1/farmer/statistics")
    public Result<FarmerOrderStatisticsRespDTO> getFarmerOrderStatistics() {
        return Results.success(orderService.getFarmerOrderStatistics());
    }
}
