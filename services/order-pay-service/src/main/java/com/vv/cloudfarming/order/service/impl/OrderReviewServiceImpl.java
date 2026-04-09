package com.vv.cloudfarming.order.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.entity.OrderSkuReviewDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderSkuReviewMapper;
import com.vv.cloudfarming.order.dto.common.OrderReviewAggDTO;
import com.vv.cloudfarming.order.dto.common.ProductSummaryDTO;
import com.vv.cloudfarming.order.dto.common.SpuReviewSummaryAggDTO;
import com.vv.cloudfarming.order.dto.req.OrderReviewPendingPageReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderSkuReviewCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.SpuReviewPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageWithProductInfoRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderSkuReviewRespDTO;
import com.vv.cloudfarming.order.dto.resp.SpuReviewSummaryRespDTO;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.remote.ShopRemoteService;
import com.vv.cloudfarming.order.remote.UserRemoteService;
import com.vv.cloudfarming.order.service.OrderReviewService;
import com.vv.cloudfarming.order.service.query.OrderProductSummaryQueryService;
import com.vv.cloudfarming.product.dto.resp.ShopRespDTO;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderReviewServiceImpl implements OrderReviewService {

    private static final long DEFAULT_PAGE_NO = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;

    private final OrderMapper orderMapper;
    private final OrderDetailSkuMapper orderDetailSkuMapper;
    private final OrderSkuReviewMapper orderSkuReviewMapper;
    private final OrderProductSummaryQueryService orderProductSummaryQueryService;
    private final ShopRemoteService shopRemoteService;
    private final UserRemoteService userRemoteService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSkuReviewRespDTO createCurrentUserReview(OrderSkuReviewCreateReqDTO requestParam) {
        long userId = StpUtil.getLoginIdAsLong();
        OrderDetailSkuDO orderDetailSku = orderDetailSkuMapper.selectById(requestParam.getOrderDetailSkuId());
        if (orderDetailSku == null) {
            throw new ClientException("订单商品不存在");
        }
        OrderDO order = orderMapper.selectOne(Wrappers.lambdaQuery(OrderDO.class)
            .eq(OrderDO::getOrderNo, orderDetailSku.getOrderNo()));
        if (order == null) {
            throw new ClientException("订单不存在");
        }
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new ClientException("当前订单不属于你");
        }
        if (!Objects.equals(order.getOrderType(), OrderTypeConstant.GOODS)) {
            throw new ClientException("当前订单不支持商品评价");
        }
        if (!Objects.equals(order.getOrderStatus(), OrderStatusEnum.COMPLETED.getCode())) {
            throw new ClientException("订单完成后才可以评价");
        }

        List<String> normalizedImageUrls = normalizeImageUrls(requestParam.getImageUrls());
        String normalizedContent = StrUtil.trim(requestParam.getContent());
        if (StrUtil.isBlank(normalizedContent) && normalizedImageUrls.isEmpty()) {
            throw new ClientException("评价内容不能为空");
        }

        OrderSkuReviewDO existed = orderSkuReviewMapper.selectOne(Wrappers.lambdaQuery(OrderSkuReviewDO.class)
            .eq(OrderSkuReviewDO::getOrderNo, order.getOrderNo())
            .eq(OrderSkuReviewDO::getOrderDetailSkuId, orderDetailSku.getId()));
        if (existed != null) {
            throw new ClientException("该商品已评价");
        }

        UserRespDTO user = getUserSnapshot(userId);
        OrderSkuReviewDO review = OrderSkuReviewDO.builder()
            .orderNo(order.getOrderNo())
            .orderDetailSkuId(orderDetailSku.getId())
            .spuId(orderDetailSku.getSpuId())
            .skuId(orderDetailSku.getSkuId())
            .shopId(order.getShopId())
            .userId(userId)
            .score(requestParam.getScore())
            .content(StrUtil.isBlank(normalizedContent) ? null : normalizedContent)
            .imageUrls(joinImageUrls(normalizedImageUrls))
            .userNameSnapshot(resolveUserName(user))
            .userAvatarSnapshot(user == null || StrUtil.isBlank(user.getAvatar()) ? null : user.getAvatar())
            .build();
        int inserted = orderSkuReviewMapper.insert(review);
        if (inserted <= 0) {
            throw new ServiceException("提交评价失败");
        }
        return toReviewResp(review);
    }

    @Override
    public IPage<OrderPageWithProductInfoRespDTO> pageCurrentUserPendingReviews(OrderReviewPendingPageReqDTO requestParam) {
        long userId = StpUtil.getLoginIdAsLong();
        List<OrderDO> completedGoodsOrders = orderMapper.selectList(Wrappers.lambdaQuery(OrderDO.class)
            .eq(OrderDO::getUserId, userId)
            .eq(OrderDO::getOrderType, OrderTypeConstant.GOODS)
            .eq(OrderDO::getOrderStatus, OrderStatusEnum.COMPLETED.getCode())
            .orderByDesc(OrderDO::getCreateTime)
            .orderByDesc(OrderDO::getId));
        if (completedGoodsOrders.isEmpty()) {
            return emptyPage(requestParam);
        }

        List<String> orderNos = completedGoodsOrders.stream()
            .map(OrderDO::getOrderNo)
            .filter(StrUtil::isNotBlank)
            .toList();
        Map<String, OrderReviewAggDTO> reviewAggMap = mapOrderReviewAgg(orderNos);
        List<OrderDO> pendingOrders = completedGoodsOrders.stream()
            .filter(order -> {
                OrderReviewAggDTO aggDTO = reviewAggMap.get(order.getOrderNo());
                return aggDTO != null && safeLong(aggDTO.getPendingReviewCount()) > 0;
            })
            .toList();
        if (pendingOrders.isEmpty()) {
            return emptyPage(requestParam);
        }

        Map<String, List<ProductSummaryDTO>> productSummariesByOrderNo = orderProductSummaryQueryService.mapByOrderNos(orderNos);
        long current = normalizeCurrent(requestParam.getCurrent());
        long size = normalizeSize(requestParam.getSize());
        int startIndex = (int) Math.min((current - 1) * size, pendingOrders.size());
        int endIndex = (int) Math.min(startIndex + size, pendingOrders.size());
        List<OrderPageWithProductInfoRespDTO> records = pendingOrders.subList(startIndex, endIndex).stream()
            .map(order -> buildOrderPageResp(order, productSummariesByOrderNo, reviewAggMap))
            .toList();

        Page<OrderPageWithProductInfoRespDTO> page = new Page<>(current, size, pendingOrders.size());
        page.setRecords(records);
        return page;
    }

    @Override
    public SpuReviewSummaryRespDTO getSpuReviewSummary(Long spuId) {
        SpuReviewSummaryAggDTO aggDTO = orderSkuReviewMapper.selectSpuReviewSummary(spuId);
        return SpuReviewSummaryRespDTO.builder()
            .totalReviewCount(safeLong(aggDTO == null ? null : aggDTO.getTotalReviewCount()))
            .avgScore(safeAmount(aggDTO == null ? null : aggDTO.getAvgScore()))
            .score1Count(safeLong(aggDTO == null ? null : aggDTO.getScore1Count()))
            .score2Count(safeLong(aggDTO == null ? null : aggDTO.getScore2Count()))
            .score3Count(safeLong(aggDTO == null ? null : aggDTO.getScore3Count()))
            .score4Count(safeLong(aggDTO == null ? null : aggDTO.getScore4Count()))
            .score5Count(safeLong(aggDTO == null ? null : aggDTO.getScore5Count()))
            .build();
    }

    @Override
    public IPage<OrderSkuReviewRespDTO> pageSpuReviews(SpuReviewPageReqDTO requestParam) {
        IPage<OrderSkuReviewDO> reviewPage = orderSkuReviewMapper.selectSpuReviewPage(requestParam, requestParam.getSpuId());
        return reviewPage.convert(this::toReviewResp);
    }

    private UserRespDTO getUserSnapshot(Long userId) {
        try {
            var result = userRemoteService.getUserById(userId);
            if (result == null || !result.isSuccess()) {
                return null;
            }
            return result.getData();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String resolveUserName(UserRespDTO user) {
        if (user == null || StrUtil.isBlank(user.getUsername())) {
            return "用户";
        }
        return user.getUsername();
    }

    private OrderSkuReviewRespDTO toReviewResp(OrderSkuReviewDO review) {
        OrderSkuReviewRespDTO respDTO = BeanUtil.toBean(review, OrderSkuReviewRespDTO.class);
        respDTO.setImageUrls(splitImageUrls(review.getImageUrls()));
        return respDTO;
    }

    private Map<String, OrderReviewAggDTO> mapOrderReviewAgg(List<String> orderNos) {
        if (orderNos == null || orderNos.isEmpty()) {
            return Collections.emptyMap();
        }
        return orderSkuReviewMapper.selectOrderReviewAggByOrderNos(orderNos).stream()
            .collect(Collectors.toMap(OrderReviewAggDTO::getOrderNo, each -> each, (left, right) -> left, LinkedHashMap::new));
    }

    private OrderPageWithProductInfoRespDTO buildOrderPageResp(
        OrderDO order,
        Map<String, List<ProductSummaryDTO>> productSummariesByOrderNo,
        Map<String, OrderReviewAggDTO> reviewAggMap
    ) {
        ShopRespDTO shop = shopRemoteService.getShopById(order.getShopId()).getData();
        OrderReviewAggDTO aggDTO = reviewAggMap.get(order.getOrderNo());
        long pendingReviewCount = safeLong(aggDTO == null ? null : aggDTO.getPendingReviewCount());
        return OrderPageWithProductInfoRespDTO.builder()
            .id(order.getId())
            .orderNo(order.getOrderNo())
            .payOrderNo(order.getPayOrderNo())
            .shopName(shop == null ? "" : shop.getShopName())
            .items(productSummariesByOrderNo.getOrDefault(order.getOrderNo(), Collections.emptyList()))
            .totalPrice(order.getTotalAmount())
            .totalAmount(order.getTotalAmount())
            .actualPayAmount(order.getActualPayAmount())
            .orderType(order.getOrderType())
            .orderStatus(order.getOrderStatus())
            .pendingReviewCount(pendingReviewCount)
            .allReviewed(pendingReviewCount <= 0)
            .createTime(order.getCreateTime())
            .build();
    }

    private List<String> normalizeImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Collections.emptyList();
        }
        return imageUrls.stream()
            .map(StrUtil::trim)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .limit(9)
            .toList();
    }

    private List<String> splitImageUrls(String rawImageUrls) {
        if (StrUtil.isBlank(rawImageUrls)) {
            return Collections.emptyList();
        }
        return StrUtil.split(rawImageUrls, ',').stream()
            .map(StrUtil::trim)
            .filter(StrUtil::isNotBlank)
            .toList();
    }

    private String joinImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        return String.join(",", imageUrls);
    }

    private IPage<OrderPageWithProductInfoRespDTO> emptyPage(OrderReviewPendingPageReqDTO requestParam) {
        Page<OrderPageWithProductInfoRespDTO> page = new Page<>(normalizeCurrent(requestParam.getCurrent()), normalizeSize(requestParam.getSize()), 0);
        page.setRecords(Collections.emptyList());
        return page;
    }

    private long normalizeCurrent(long current) {
        return current > 0 ? current : DEFAULT_PAGE_NO;
    }

    private long normalizeSize(long size) {
        return size > 0 ? size : DEFAULT_PAGE_SIZE;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
