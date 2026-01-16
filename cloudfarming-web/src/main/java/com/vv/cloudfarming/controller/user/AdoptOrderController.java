package com.vv.cloudfarming.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.order.dto.req.AdoptOrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.AdoptOrderRespDTO;
import com.vv.cloudfarming.order.service.AdoptOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认养订单控制器
 */
@Tag(name = "认养订单操作")
@RestController
@RequiredArgsConstructor
public class AdoptOrderController {

    private final AdoptOrderService adoptOrderService;

    /**
     * 创建认养订单
     *
     * @param reqDTO 创建认养订单请求DTO
     * @return 认养订单响应DTO
     */
    @SaCheckLogin
    @Operation(summary = "创建认养订单")
    @PostMapping("/v1/adopt/order")
    public Result<AdoptOrderRespDTO> createAdoptOrder(@RequestBody AdoptOrderCreateReqDTO reqDTO) {
        // 从Sa-Token中获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        return Results.success(adoptOrderService.createAdoptOrder(userId, reqDTO));
    }
}
