package com.vv.cloudfarming.controller.user;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderCreateRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderInfoRespDTO;
import com.vv.cloudfarming.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单业务控制层
 */
@Tag(name = "订单操作")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping("/v1/order")
    public Result<Void> createOrder(@RequestBody @Valid OrderCreateReqDTO requestParam) {
        orderService.createOrder(requestParam);
        return Results.success();
    }

    @Operation(summary = "创建订单(统一入口)")
    @PostMapping("/v1/order/create")
    public Result<OrderCreateRespDTO> createUnifiedOrder(@RequestBody @Valid OrderCreateReqDTO requestParam) {
        return Results.success(orderService.createOrder(requestParam));
    }

    @Operation(summary = "根据id查询订单")
    @GetMapping("/v1/order/")
    public Result<OrderInfoRespDTO> getOrder(@RequestParam Long id){
         return Results.success(orderService.queryOrderById(id));
    }
}
