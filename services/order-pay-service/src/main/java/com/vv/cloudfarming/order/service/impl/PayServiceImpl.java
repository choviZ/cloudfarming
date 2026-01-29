package com.vv.cloudfarming.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.constant.BizStatusConstant;
import com.vv.cloudfarming.order.constant.PayChannelConstant;
import com.vv.cloudfarming.order.dao.entity.PayOrderDO;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dao.mapper.PayOrderMapper;
import com.vv.cloudfarming.order.dto.common.OrderIdAndTypeDTO;
import com.vv.cloudfarming.order.dto.req.PayOrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.PayOrderPageReqDTO;
import com.vv.cloudfarming.order.dto.common.ProductSummaryDTO;
import com.vv.cloudfarming.order.dto.resp.PayOrderRespDTO;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.order.service.PayService;
import com.vv.cloudfarming.order.utils.ProductUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Lazy
public class PayServiceImpl extends ServiceImpl<PayOrderMapper, PayOrderDO> implements PayService {

    private final OrderMapper orderMapper;
    private final ProductUtil productUtil;

    @Override
    public PayOrderDO createPayOrder(PayOrderCreateReqDTO requestParam) {
        Long buyerId = requestParam.getBuyerId();
        String payOrderNo = requestParam.getPayOrderNo();
        BigDecimal totalAmount = requestParam.getTotalAmount();

        PayOrderDO payOrder = PayOrderDO.builder()
                .payOrderNo(payOrderNo)
                .buyerId(buyerId)
                .payStatus(PayStatusEnum.UNPAID.getCode())
                .bizStatus(BizStatusConstant.WAIT_PAY)
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

    @Override
    public IPage<PayOrderRespDTO> listPayOrder(PayOrderPageReqDTO requestParam) {
        // 参数校验
        String payOrderNo = requestParam.getPayOrderNo();
        Long buyerId = requestParam.getBuyerId();
        Integer payStatus = requestParam.getPayStatus();

        LambdaQueryWrapper<PayOrderDO> wrapper = Wrappers.lambdaQuery(PayOrderDO.class)
                .eq(StrUtil.isNotBlank(payOrderNo), PayOrderDO::getPayOrderNo, payOrderNo)
                .eq(ObjectUtil.isNotNull(buyerId), PayOrderDO::getBuyerId, buyerId)
                .eq(ObjectUtil.isNotNull(payStatus), PayOrderDO::getPayStatus, payStatus);
        IPage<PayOrderDO> payOrderPage = baseMapper.selectPage(requestParam, wrapper);

        // 转换
        return payOrderPage.convert(each -> {
            PayOrderRespDTO result = BeanUtil.toBean(each, PayOrderRespDTO.class);
            List<ProductSummaryDTO> payOrderItems = new ArrayList<>();
            // 关联相关的商品
            List<OrderIdAndTypeDTO> orderIds = orderMapper.getOrderIdByPayNo(each.getPayOrderNo());

            orderIds.forEach(order -> {
                productUtil.buildProductSummary(order.getId(), order.getOrderType(), payOrderItems);
            });
            result.setItems(payOrderItems);
            return result;
        });
    }
}
