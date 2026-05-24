package com.vv.cloudfarming.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.order.dto.req.AdoptReviewPageReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderReviewPendingPageReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderSkuReviewCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.SpuReviewPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageWithProductInfoRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderSkuReviewRespDTO;
import com.vv.cloudfarming.order.dto.resp.SpuReviewSummaryRespDTO;

/**
 * 订单评论服务
 */
public interface OrderReviewService {

    /**
     * 当前用户提交订单商品/认养评价
     */
    OrderSkuReviewRespDTO createCurrentUserReview(OrderSkuReviewCreateReqDTO requestParam);

    /**
     * 分页查询当前用户待评价的已完成订单
     */
    IPage<OrderPageWithProductInfoRespDTO> pageCurrentUserPendingReviews(OrderReviewPendingPageReqDTO requestParam);

    /**
     * 获取SPU商品的评价汇总（总评价数、平均分、各评分数量）
     */
    SpuReviewSummaryRespDTO getSpuReviewSummary(Long spuId);

    /**
     * 分页查询SPU商品的评价列表
     */
    IPage<OrderSkuReviewRespDTO> pageSpuReviews(SpuReviewPageReqDTO requestParam);

    /**
     * 获取认养项目的评价汇总（总评价数、平均分、各评分数量）
     */
    SpuReviewSummaryRespDTO getAdoptReviewSummary(Long adoptItemId);

    /**
     * 分页查询认养项目的评价列表
     */
    IPage<OrderSkuReviewRespDTO> pageAdoptReviews(AdoptReviewPageReqDTO requestParam);
}
