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
import com.vv.cloudfarming.order.dao.entity.PayDO;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dao.mapper.PayOrderMapper;
import com.vv.cloudfarming.order.dto.common.PayOrderOrderRelationDTO;
import com.vv.cloudfarming.order.dto.common.ProductSummaryDTO;
import com.vv.cloudfarming.order.dto.req.PayOrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.PayOrderPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.PayOrderRespDTO;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.order.service.PayService;
import com.vv.cloudfarming.order.service.query.OrderProductSummaryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Lazy
public class PayServiceImpl extends ServiceImpl<PayOrderMapper, PayDO> implements PayService {

    private final OrderMapper orderMapper;
    private final OrderProductSummaryQueryService orderProductSummaryQueryService;

    @Override
    public PayDO createPayOrder(PayOrderCreateReqDTO requestParam) {
        Long buyerId = requestParam.getBuyerId();
        String payOrderNo = requestParam.getPayOrderNo();
        BigDecimal totalAmount = requestParam.getTotalAmount();

        PayDO payOrder = PayDO.builder()
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
        String payOrderNo = requestParam.getPayOrderNo();
        Long buyerId = requestParam.getBuyerId();
        Integer payStatus = requestParam.getPayStatus();
        Integer bizStatus = requestParam.getBizStatus();

        LambdaQueryWrapper<PayDO> wrapper = Wrappers.lambdaQuery(PayDO.class)
            .eq(StrUtil.isNotBlank(payOrderNo), PayDO::getPayOrderNo, payOrderNo)
            .eq(ObjectUtil.isNotNull(buyerId), PayDO::getBuyerId, buyerId)
            .eq(ObjectUtil.isNotNull(bizStatus), PayDO::getBizStatus, bizStatus)
            .eq(ObjectUtil.isNotNull(payStatus), PayDO::getPayStatus, payStatus);
        IPage<PayDO> payOrderPage = baseMapper.selectPage(requestParam, wrapper);

        List<String> payOrderNos = payOrderPage.getRecords().stream()
            .map(PayDO::getPayOrderNo)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .collect(Collectors.toList());

        List<PayOrderOrderRelationDTO> payOrderRelations = payOrderNos.isEmpty()
            ? Collections.emptyList()
            : orderMapper.listPayOrderRelations(payOrderNos);

        Map<String, List<String>> orderNosByPayOrderNo = payOrderRelations.stream()
            .collect(Collectors.groupingBy(
                PayOrderOrderRelationDTO::getPayOrderNo,
                Collectors.mapping(PayOrderOrderRelationDTO::getOrderNo, Collectors.toList())
            ));
        Map<String, List<ProductSummaryDTO>> productSummariesByOrderNo =
            orderProductSummaryQueryService.mapByOrderNos(payOrderRelations.stream()
                .map(PayOrderOrderRelationDTO::getOrderNo)
                .collect(Collectors.toList()));

        return payOrderPage.convert(each -> {
            PayOrderRespDTO result = BeanUtil.toBean(each, PayOrderRespDTO.class);
            result.setItems(mergeProductSummaries(orderNosByPayOrderNo.get(each.getPayOrderNo()), productSummariesByOrderNo));
            return result;
        });
    }

    private List<ProductSummaryDTO> mergeProductSummaries(List<String> orderNos,
                                                          Map<String, List<ProductSummaryDTO>> productSummariesByOrderNo) {
        if (orderNos == null || orderNos.isEmpty()) {
            return Collections.emptyList();
        }
        List<ProductSummaryDTO> items = new ArrayList<>();
        for (String orderNo : orderNos) {
            items.addAll(productSummariesByOrderNo.getOrDefault(orderNo, Collections.emptyList()));
        }
        return items;
    }
}
