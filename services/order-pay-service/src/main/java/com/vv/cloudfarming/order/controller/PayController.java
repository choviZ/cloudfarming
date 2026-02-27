package com.vv.cloudfarming.order.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.config.AlipayTemplate;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailAdoptDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.entity.PayOrderDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailAdoptMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dao.mapper.PayOrderMapper;
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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "支付宝支付接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/alipay")
public class PayController {

    private static final Logger log = LoggerFactory.getLogger(PayController.class);
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
        PayOrderDO payOrder = payOrderMapper.selectPayOrderByNo(payOrderNo);
        // 创建client
        DefaultAlipayClient defaultAlipayClient = new DefaultAlipayClient(
                alipayTemplate.getGatewayUrl(),
                alipayTemplate.getAppId(),
                alipayTemplate.getPrivateKey(),
                "json",
                alipayTemplate.getCharset(),
                alipayTemplate.getAlipayPublicKey(),
                alipayTemplate.getSignType()
        );
        // 创建 request 并设置参数
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayTemplate.getNotifyUrl()); // 回调接口
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payOrder.getPayOrderNo());
        bizContent.put("total_amount", payOrder.getTotalAmount());
        bizContent.put("subject", payOrder.getPayOrderNo()); // 支付的名称
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());
        request.setReturnUrl("http://localhost:5173/paySuccess"); // 支付完成后跳转的页面路径
        // 执行请求拿到响应
        String form = "";
        try {
            form = defaultAlipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
        response.setContentType("text/html;charset=" + alipayTemplate.getCharset());
        response.getWriter().write(form); // 将完整的表单html输出到页面
        response.getWriter().flush();
        response.getWriter().close();
    }

    @Operation(summary = "支付回调接口")
    @Transactional
    @PostMapping("/callback")
    public void callback(@RequestParam Map<String, String> requestParam) {
        log.info("触发回调，参数{}", requestParam);
        String payOrderNo = requestParam.get("out_trade_no");
        PayOrderDO payOrderDO = payOrderMapper.selectOne(Wrappers.lambdaQuery(PayOrderDO.class).eq(PayOrderDO::getPayOrderNo, payOrderNo));
        if (PayStatusEnum.PAID.getCode().equals(payOrderDO.getPayStatus())) {
            log.info("支付单已处理，无需重复处理");
            return;
        }
        String totalAmount = requestParam.get("total_amount");
        List<OrderDO> orders = orderMapper.selectList(Wrappers.lambdaQuery(OrderDO.class).eq(OrderDO::getPayOrderNo, payOrderNo));
        if (orders == null || orders.isEmpty()) {
            throw new ServiceException("支付单号：" + payOrderNo + "对应的订单不存在");
        }
        // 更新支付表状态
        LambdaUpdateWrapper<PayOrderDO> wrapper = Wrappers.lambdaUpdate(PayOrderDO.class)
                .eq(PayOrderDO::getPayOrderNo, payOrderNo)
                .set(PayOrderDO::getPayStatus, PayStatusEnum.PAID.getCode());
        int payUpdated = payOrderMapper.update(wrapper);
        if (!SqlHelper.retBool(payUpdated)) {
            throw new ServiceException("更新支付单状态失败");
        }
        // 更新订单状态
        LambdaUpdateWrapper<OrderDO> orderWrapper = Wrappers.lambdaUpdate(OrderDO.class)
                .eq(OrderDO::getPayOrderNo, payOrderNo)
                .set(OrderDO::getOrderStatus, OrderStatusEnum.PENDING_SHIPMENT.getCode());
        int orderUpdated = orderMapper.update(orderWrapper);
        if (!SqlHelper.retBool(orderUpdated)) {
            throw new ServiceException("更新订单状态失败");
        }
        // 释放锁定库存
        for (OrderDO order : orders) {
            if (OrderTypeConstant.ADOPT == order.getOrderType()) {
                LambdaQueryWrapper<OrderDetailAdoptDO> adoptDetailWrapper = Wrappers.lambdaQuery(OrderDetailAdoptDO.class)
                        .eq(OrderDetailAdoptDO::getOrderNo, order.getOrderNo());
                List<OrderDetailAdoptDO> orderDetailAdopts = orderDetailAdoptMapper.selectList(adoptDetailWrapper);
                for (OrderDetailAdoptDO adoptDetail : orderDetailAdopts) {
                    LockStockReqDTO lockStockReqDTO = new LockStockReqDTO();
                    lockStockReqDTO.setId(adoptDetail.getId());
                    lockStockReqDTO.setQuantity(adoptDetail.getQuantity());
                    adoptItemRemoteService.unlockAdoptItemStock(lockStockReqDTO);
                }
            } else if (OrderTypeConstant.GOODS == order.getOrderType()) {
                LambdaQueryWrapper<OrderDetailSkuDO> skuDetailWrapper = Wrappers.lambdaQuery(OrderDetailSkuDO.class)
                        .eq(OrderDetailSkuDO::getOrderNo, order.getOrderNo());
                List<OrderDetailSkuDO> skuDetails = orderDetailSkuMapper.selectList(skuDetailWrapper);
                for (OrderDetailSkuDO skuDetail : skuDetails) {
                    LockStockReqDTO lockStockReqDTO = new LockStockReqDTO();
                    lockStockReqDTO.setId(skuDetail.getId());
                    lockStockReqDTO.setQuantity(skuDetail.getQuantity());
                    skuRemoteService.unlockStock(lockStockReqDTO);
                }
            } else {
                throw new ServiceException("不支持的订单类型");
            }
        }
    }
}
