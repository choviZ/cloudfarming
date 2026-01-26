package com.vv.cloudfarming.controller.user;

import cn.dev33.satoken.stp.StpUtil;
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
        long userId = StpUtil.getLoginIdAsLong();
        orderService.createOrder(userId, requestParam);
        return Results.success();
    }

}
