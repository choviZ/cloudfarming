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

@Service
public class PayServiceImpl extends ServiceImpl<PayOrderMapper, PayOrderDO> implements PayService {

    @Override
    public PayOrderDO createPayOrder(PayOrderCreateReqDTO requestParam) {
        Long buyerId = requestParam.getBuyerId();
        String payOrderNo = requestParam.getPayOrderNo();
        BigDecimal totalAmount = requestParam.getTotalAmount();

        PayOrderDO payOrder = PayOrderDO.builder()
                .payOrderNo(payOrderNo)
                .buyerId(buyerId)
                .payStatus(PayStatusEnum.UNPAID.getCode())
                .payTime(null)
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
}
