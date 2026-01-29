package com.vv.cloudfarming.order.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderCreateRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageRespDTO;
import com.vv.cloudfarming.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单控制层")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping("/v1/order")
    public Result<OrderCreateRespDTO> createOrder(@RequestBody @Valid OrderCreateReqDTO requestParam) {
        long userId = StpUtil.getLoginIdAsLong();
        return Results.success(orderService.createOrder(userId, requestParam));
    }

    @Operation(summary = "查询订单列表")
    @PostMapping("/v1/order/list")
    public Result<IPage<OrderPageRespDTO>> listOrder(@RequestBody OrderPageReqDTO requestParam) {
        return Results.success(orderService.listOrder(requestParam));
    }
}
