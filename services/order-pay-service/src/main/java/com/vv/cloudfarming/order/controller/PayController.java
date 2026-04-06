package com.vv.cloudfarming.order.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.order.config.AlipayTemplate;
import com.vv.cloudfarming.order.constant.BizStatusConstant;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailAdoptDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.entity.PayDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailAdoptMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dao.mapper.PayOrderMapper;
import com.vv.cloudfarming.order.dto.resp.PayConfirmRespDTO;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.order.remote.AdoptItemRemoteService;
import com.vv.cloudfarming.order.remote.SkuRemoteService;
import com.vv.cloudfarming.product.dto.req.LockStockReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "支付宝支付接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/alipay")
public class PayController {

    private static final Logger log = LoggerFactory.getLogger(PayController.class);
    private static final String DEFAULT_RETURN_URL = "http://localhost:5173/paySuccess";
    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";
    private static final String TRADE_FINISHED = "TRADE_FINISHED";

    private final AlipayTemplate alipayTemplate;
    private final PayOrderMapper payOrderMapper;
    private final OrderMapper orderMapper;
    private final OrderDetailAdoptMapper orderDetailAdoptMapper;
    private final OrderDetailSkuMapper orderDetailSkuMapper;
    private final AdoptItemRemoteService adoptItemRemoteService;
    private final SkuRemoteService skuRemoteService;

    @Operation(summary = "支付宝支付")
    @GetMapping("/pay")
    public void pay(@RequestParam String payOrderNo, HttpServletResponse response) throws IOException {
        PayDO payOrder = getPayOrderByNo(payOrderNo);
        DefaultAlipayClient defaultAlipayClient = createAlipayClient();
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayTemplate.getNotifyUrl());
        request.setReturnUrl(resolveReturnUrl());

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payOrder.getPayOrderNo());
        bizContent.put("total_amount", payOrder.getTotalAmount());
        bizContent.put("subject", payOrder.getPayOrderNo());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        try {
            String form = defaultAlipayClient.pageExecute(request).getBody();
            response.setContentType("text/html;charset=" + alipayTemplate.getCharset());
            response.getWriter().write(form);
            response.getWriter().flush();
        } catch (AlipayApiException e) {
            throw wrapAlipayException("发起支付宝支付失败", e);
        }
        response.getWriter().close();
    }

    @Operation(summary = "支付宝异步回调接口")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/callback")
    public String callback(@RequestParam Map<String, String> requestParam) {
        log.info("触发支付宝异步回调，参数={}", requestParam);
        try {
            if (!verifyAlipaySign(requestParam)) {
                log.warn("支付宝异步回调验签失败，参数={}", requestParam);
                return "failure";
            }
            if (!isTradePaid(requestParam.get("trade_status"))) {
                log.info("支付宝异步回调状态无需处理，outTradeNo={}, tradeStatus={}",
                        requestParam.get("out_trade_no"), requestParam.get("trade_status"));
                return "success";
            }
            handlePaySuccess(requestParam.get("out_trade_no"), requestParam.get("total_amount"), requestParam.get("trade_no"));
            return "success";
        } catch (Exception ex) {
            log.error("处理支付宝异步回调失败，参数={}", requestParam, ex);
            return "failure";
        }
    }

    @Operation(summary = "支付成功兜底确认")
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/confirm")
    public Result<PayConfirmRespDTO> confirm(@RequestParam String payOrderNo) {
        PayDO payOrder = getPayOrderByNo(payOrderNo);
        if (PayStatusEnum.PAID.getCode().equals(payOrder.getPayStatus())) {
            return Results.success(buildConfirmResp(payOrder, true, null, "支付已确认"));
        }

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payOrderNo);
        request.setBizContent(bizContent.toString());

        try {
            AlipayTradeQueryResponse queryResponse = createAlipayClient().execute(request);
            if (queryResponse == null || !queryResponse.isSuccess()) {
                String message = queryResponse != null && StringUtils.hasText(queryResponse.getSubMsg())
                        ? queryResponse.getSubMsg()
                        : "支付结果确认失败，请稍后刷新订单列表";
                log.warn("支付宝交易查询失败，payOrderNo={}, response={}", payOrderNo, queryResponse);
                return Results.success(buildConfirmResp(payOrder, false, null, message));
            }

            String tradeStatus = queryResponse.getTradeStatus();
            if (isTradePaid(tradeStatus)) {
                handlePaySuccess(payOrderNo, queryResponse.getTotalAmount(), queryResponse.getTradeNo());
                PayDO latestPayOrder = getPayOrderByNo(payOrderNo);
                return Results.success(buildConfirmResp(latestPayOrder, true, tradeStatus, "支付成功"));
            }

            String message = "支付结果确认中，请稍后刷新订单列表";
            if ("TRADE_CLOSED".equals(tradeStatus)) {
                message = "该支付单已关闭";
            }
            return Results.success(buildConfirmResp(payOrder, false, tradeStatus, message));
        } catch (AlipayApiException e) {
            log.error("调用支付宝交易查询失败，payOrderNo={}", payOrderNo, e);
            throw wrapAlipayException("支付结果确认失败，请稍后重试", e);
        }
    }

    private DefaultAlipayClient createAlipayClient() {
        return new DefaultAlipayClient(
                alipayTemplate.getGatewayUrl(),
                alipayTemplate.getAppId(),
                alipayTemplate.getPrivateKey(),
                "json",
                alipayTemplate.getCharset(),
                alipayTemplate.getAlipayPublicKey(),
                alipayTemplate.getSignType()
        );
    }

    private String resolveReturnUrl() {
        return StringUtils.hasText(alipayTemplate.getReturnUrl()) ? alipayTemplate.getReturnUrl() : DEFAULT_RETURN_URL;
    }

    private boolean verifyAlipaySign(Map<String, String> requestParam) throws AlipayApiException {
        return AlipaySignature.rsaCheckV1(
                requestParam,
                alipayTemplate.getAlipayPublicKey(),
                alipayTemplate.getCharset(),
                alipayTemplate.getSignType()
        );
    }

    private boolean isTradePaid(String tradeStatus) {
        return TRADE_SUCCESS.equals(tradeStatus) || TRADE_FINISHED.equals(tradeStatus);
    }

    private PayDO getPayOrderByNo(String payOrderNo) {
        PayDO payOrder = payOrderMapper.selectPayOrderByNo(payOrderNo);
        if (payOrder == null) {
            throw new ServiceException("支付单不存在，支付单号：" + payOrderNo);
        }
        return payOrder;
    }

    private PayConfirmRespDTO buildConfirmResp(PayDO payOrder, boolean paid, String tradeStatus, String message) {
        return PayConfirmRespDTO.builder()
                .payOrderNo(payOrder.getPayOrderNo())
                .paid(paid)
                .payStatus(payOrder.getPayStatus())
                .tradeStatus(tradeStatus)
                .totalAmount(payOrder.getTotalAmount())
                .message(message)
                .build();
    }

    private void validatePayAmount(PayDO payOrder, String totalAmount) {
        if (!StringUtils.hasText(totalAmount) || payOrder.getTotalAmount() == null) {
            return;
        }
        try {
            BigDecimal paidAmount = new BigDecimal(totalAmount);
            if (payOrder.getTotalAmount().compareTo(paidAmount) != 0) {
                throw new ServiceException("支付金额校验失败，支付单号：" + payOrder.getPayOrderNo());
            }
        } catch (NumberFormatException ex) {
            throw new ServiceException("支付金额格式非法，支付单号：" + payOrder.getPayOrderNo());
        }
    }

    private void handlePaySuccess(String payNo, String totalAmount, String tradeNo) {
        PayDO payDO = getPayOrderByNo(payNo);
        if (PayStatusEnum.PAID.getCode().equals(payDO.getPayStatus())) {
            log.info("支付单已处理，无需重复处理，payOrderNo={}", payNo);
            return;
        }

        validatePayAmount(payDO, totalAmount);

        Long userId = payDO.getBuyerId();
        if (userId == null) {
            throw new ServiceException("支付单缺少买家信息，支付单号：" + payNo);
        }

        List<OrderDO> orders = orderMapper.selectList(
                Wrappers.lambdaQuery(OrderDO.class)
                        .eq(OrderDO::getUserId, userId)
                        .eq(OrderDO::getPayOrderNo, payNo)
        );
        if (orders == null || orders.isEmpty()) {
            throw new ServiceException("支付单号：" + payNo + "对应的订单不存在");
        }

        LambdaUpdateWrapper<PayDO> payWrapper = Wrappers.lambdaUpdate(PayDO.class)
                .eq(PayDO::getPayOrderNo, payNo)
                .eq(PayDO::getPayStatus, PayStatusEnum.UNPAID.getCode())
                .set(PayDO::getPayStatus, PayStatusEnum.PAID.getCode())
                .set(PayDO::getBizStatus, BizStatusConstant.PAID)
                .set(PayDO::getPayTime, LocalDateTime.now());
        int payUpdated = payOrderMapper.update(null, payWrapper);
        if (!SqlHelper.retBool(payUpdated)) {
            PayDO latestPayOrder = getPayOrderByNo(payNo);
            if (PayStatusEnum.PAID.getCode().equals(latestPayOrder.getPayStatus())) {
                log.info("支付单已被并发处理，payOrderNo={}", payNo);
                return;
            }
            throw new ServiceException("更新支付单状态失败");
        }

        for (OrderDO order : orders) {
            int orderUpdated = orderMapper.update(null, Wrappers.lambdaUpdate(OrderDO.class)
                    .eq(OrderDO::getId, order.getId())
                    .eq(OrderDO::getOrderStatus, OrderStatusEnum.PENDING_PAYMENT.getCode())
                    .set(OrderDO::getOrderStatus, resolvePaidOrderStatus(order.getOrderType())));
            if (!SqlHelper.retBool(orderUpdated)) {
                throw new ServiceException("更新订单状态失败");
            }

            if (OrderTypeConstant.ADOPT == order.getOrderType()) {
                LambdaQueryWrapper<OrderDetailAdoptDO> adoptDetailWrapper = Wrappers.lambdaQuery(OrderDetailAdoptDO.class)
                        .eq(OrderDetailAdoptDO::getOrderNo, order.getOrderNo());
                List<OrderDetailAdoptDO> orderDetailAdopts = orderDetailAdoptMapper.selectList(adoptDetailWrapper);
                for (OrderDetailAdoptDO adoptDetail : orderDetailAdopts) {
                    LockStockReqDTO lockStockReqDTO = new LockStockReqDTO();
                    lockStockReqDTO.setId(adoptDetail.getAdoptItemId());
                    lockStockReqDTO.setQuantity(adoptDetail.getQuantity());
                    Integer updated = adoptItemRemoteService.deductAdoptItemStock(lockStockReqDTO).getData();
                    if (updated == null || updated <= 0) {
                        log.error("扣减认养项目锁定库存失败，orderNo={}, adoptItemId={}, quantity={}",
                                order.getOrderNo(), adoptDetail.getAdoptItemId(), adoptDetail.getQuantity());
                    }
                }
            } else if (OrderTypeConstant.GOODS == order.getOrderType()) {
                LambdaQueryWrapper<OrderDetailSkuDO> skuDetailWrapper = Wrappers.lambdaQuery(OrderDetailSkuDO.class)
                        .eq(OrderDetailSkuDO::getOrderNo, order.getOrderNo());
                List<OrderDetailSkuDO> skuDetails = orderDetailSkuMapper.selectList(skuDetailWrapper);
                for (OrderDetailSkuDO skuDetail : skuDetails) {
                    LockStockReqDTO lockStockReqDTO = new LockStockReqDTO();
                    lockStockReqDTO.setId(skuDetail.getSkuId());
                    lockStockReqDTO.setQuantity(skuDetail.getQuantity());
                    Integer updated = skuRemoteService.deductStock(lockStockReqDTO).getData();
                    if (updated == null || updated <= 0) {
                        log.error("扣减SKU锁定库存失败，orderNo={}, skuId={}, quantity={}",
                                order.getOrderNo(), skuDetail.getSkuId(), skuDetail.getQuantity());
                    }
                }
            } else {
                throw new ServiceException("不支持的订单类型");
            }
        }

        log.info("支付成功处理完成，payOrderNo={}, tradeNo={}", payNo, tradeNo);
    }

    private Integer resolvePaidOrderStatus(Integer orderType) {
        if (OrderTypeConstant.ADOPT == orderType) {
            return OrderStatusEnum.PENDING_ASSIGNMENT.getCode();
        }
        if (OrderTypeConstant.GOODS == orderType) {
            return OrderStatusEnum.PENDING_SHIPMENT.getCode();
        }
        throw new ServiceException("不支持的订单类型");
    }

    private ServiceException wrapAlipayException(String fallbackMessage, AlipayApiException e) {
        String exceptionMessage = e.getMessage();
        if (StringUtils.hasText(exceptionMessage) && exceptionMessage.contains("支付宝公钥")) {
            return new ServiceException("支付宝公钥配置错误，请将 alipayPublicKey 替换为支付宝平台公钥");
        }
        return new ServiceException(fallbackMessage);
    }
}
