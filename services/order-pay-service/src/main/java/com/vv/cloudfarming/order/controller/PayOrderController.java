package com.vv.cloudfarming.order.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.order.dto.req.PayOrderCancelReqDTO;
import com.vv.cloudfarming.order.dto.req.PayOrderPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.PayOrderRespDTO;
import com.vv.cloudfarming.order.service.OrderExpireService;
import com.vv.cloudfarming.order.service.PayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "支付单控制层")
@RestController
@RequiredArgsConstructor
public class PayOrderController {

    private final PayService payService;
    private final OrderExpireService orderExpireService;

    @Operation(summary = "分页查询支付单")
    @PostMapping("/api/pay/list")
    public Result<IPage<PayOrderRespDTO>> listPayOrder(@RequestBody PayOrderPageReqDTO requestParam) {
        IPage<PayOrderRespDTO> result = payService.listPayOrder(requestParam);
        return Results.success(result);
    }

    @Operation(summary = "取消支付单")
    @PostMapping("/api/pay/cancel")
    public Result<Void> cancelPayOrder(@RequestBody @Valid PayOrderCancelReqDTO requestParam) {
        long userId = StpUtil.getLoginIdAsLong();
        orderExpireService.cancelPayOrder(requestParam.getPayOrderNo(), userId);
        return Results.success();
    }
}
