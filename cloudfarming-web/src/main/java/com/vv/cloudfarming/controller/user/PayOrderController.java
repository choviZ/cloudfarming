package com.vv.cloudfarming.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.order.dto.req.PayOrderPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.PayOrderRespDTO;
import com.vv.cloudfarming.order.service.PayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "支付单操作")
@RestController
@RequiredArgsConstructor
public class PayOrderController {

    private final PayService payService;

    @Operation(summary = "分页查询支付单")
    @PostMapping("/v1/order/pay/list")
    public Result<IPage<PayOrderRespDTO>> listPayOrder(@RequestBody PayOrderPageReqDTO requestParam) {
        IPage<PayOrderRespDTO> result = payService.listPayOrder(requestParam);
        return Results.success(result);
    }
}
