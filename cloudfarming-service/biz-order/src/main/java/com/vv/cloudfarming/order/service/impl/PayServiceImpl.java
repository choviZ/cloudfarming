package com.vv.cloudfarming.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.constant.PayChannelConstant;
import com.vv.cloudfarming.order.dao.entity.PayOrderDO;
import com.vv.cloudfarming.order.dao.mapper.PayOrderMapper;
import com.vv.cloudfarming.order.dto.req.PayOrderCreateReqDTO;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.order.service.PayService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PayServiceImpl extends ServiceImpl<PayOrderMapper, PayOrderDO> implements PayService {

    @Override
    public PayOrderDO createPayOrder(PayOrderCreateReqDTO requestParam) {
        Long buyerId = requestParam.getBuyerId();
        BigDecimal totalAmount = requestParam.getTotalAmount();

        PayOrderDO payOrder = PayOrderDO.builder()
                .payOrderNo(generatePayOrderNo(buyerId))
                .buyerId(buyerId)
                .payStatus(PayStatusEnum.UNPAID.getCode())
                .payChannel(PayChannelConstant.ALIPAY)
                .expireTime(LocalDateTime.now().plusMinutes(15))
                .totalAmount(totalAmount)
                .build();
        int inserted = baseMapper.insert(payOrder);
        if (!SqlHelper.retBool(inserted)) {
            throw new ServiceException("创建支付单失败");
        }
        return payOrder;
    }

    /**
     * 生成支付单号
     *
     * @param buyerId
     * @return
     */
    private String generatePayOrderNo(Long buyerId) {
        long timestamp = System.currentTimeMillis() / 1000;
        String timePart = String.format("%08d", timestamp % 100000000);
        String buyerPart = String.format("%06d", buyerId);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String randomPart = String.format("%04d", random.nextInt(10000));
        return timePart + buyerPart + randomPart;
    }
}
