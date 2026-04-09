package com.vv.cloudfarming.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.order.dto.req.OrderReviewPendingPageReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderSkuReviewCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.SpuReviewPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageWithProductInfoRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderSkuReviewRespDTO;
import com.vv.cloudfarming.order.dto.resp.SpuReviewSummaryRespDTO;

public interface OrderReviewService {

    OrderSkuReviewRespDTO createCurrentUserReview(OrderSkuReviewCreateReqDTO requestParam);

    IPage<OrderPageWithProductInfoRespDTO> pageCurrentUserPendingReviews(OrderReviewPendingPageReqDTO requestParam);

    SpuReviewSummaryRespDTO getSpuReviewSummary(Long spuId);

    IPage<OrderSkuReviewRespDTO> pageSpuReviews(SpuReviewPageReqDTO requestParam);
}
