package com.vv.cloudfarming.order.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.order.dto.req.OrderReviewPendingPageReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderSkuReviewCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.SpuReviewPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageWithProductInfoRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderSkuReviewRespDTO;
import com.vv.cloudfarming.order.dto.resp.SpuReviewSummaryRespDTO;
import com.vv.cloudfarming.order.service.OrderReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "商品评价")
@RestController
@RequiredArgsConstructor
public class OrderReviewController {

    private final OrderReviewService orderReviewService;

    @Operation(summary = "提交商品评价")
    @SaCheckLogin
    @PostMapping("/api/order/review/v1/create")
    public Result<OrderSkuReviewRespDTO> createOrderReview(@RequestBody @Valid OrderSkuReviewCreateReqDTO requestParam) {
        return Results.success(orderReviewService.createCurrentUserReview(requestParam));
    }

    @Operation(summary = "分页查询当前用户待评价订单")
    @SaCheckLogin
    @PostMapping("/api/order/review/v1/my/pending/page")
    public Result<IPage<OrderPageWithProductInfoRespDTO>> pageCurrentUserPendingReviews(
        @RequestBody OrderReviewPendingPageReqDTO requestParam
    ) {
        return Results.success(orderReviewService.pageCurrentUserPendingReviews(requestParam));
    }

    @Operation(summary = "查询商品评价汇总")
    @GetMapping("/api/order/review/v1/spu/summary")
    public Result<SpuReviewSummaryRespDTO> getSpuReviewSummary(@RequestParam("spuId") @NotNull Long spuId) {
        return Results.success(orderReviewService.getSpuReviewSummary(spuId));
    }

    @Operation(summary = "分页查询商品评价")
    @PostMapping("/api/order/review/v1/spu/page")
    public Result<IPage<OrderSkuReviewRespDTO>> pageSpuReviews(@RequestBody @Valid SpuReviewPageReqDTO requestParam) {
        return Results.success(orderReviewService.pageSpuReviews(requestParam));
    }
}
