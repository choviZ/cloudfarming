package com.vv.cloudfarming.controller.user;


import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.vv.cloudfarming.order.config.AlipayTemplate;
import com.vv.cloudfarming.order.dao.entity.PayOrderDO;
import com.vv.cloudfarming.order.dao.mapper.PayOrderMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/alipay")
public class PayController {

    private final AlipayTemplate alipayTemplate;
    private final PayOrderMapper payOrderMapper;

    @GetMapping("/pay")
    public void pay(String payOrderNo, HttpServletResponse response) throws IOException {
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
}
