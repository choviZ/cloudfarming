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
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.constant.PayChannelConstant;
import com.vv.cloudfarming.order.dao.entity.OrderDetailAdoptDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.entity.PayOrderDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailAdoptMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dao.mapper.PayOrderMapper;
import com.vv.cloudfarming.order.dto.common.OrderIdAndTypeDTO;
import com.vv.cloudfarming.order.dto.req.PayOrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.PayOrderPageReqDTO;
import com.vv.cloudfarming.order.dto.resp.PayOrderItemRespDTO;
import com.vv.cloudfarming.order.dto.resp.PayOrderRespDTO;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.order.service.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayServiceImpl extends ServiceImpl<PayOrderMapper, PayOrderDO> implements PayService {

    private final OrderMapper orderMapper;
    private final OrderDetailAdoptMapper orderDetailAdoptMapper;
    private final OrderDetailSkuMapper orderDetailSkuMapper;

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
            List<PayOrderItemRespDTO> payOrderItems = new ArrayList<>();
            // 关联相关的商品
            List<OrderIdAndTypeDTO> orderIds = orderMapper.getOrderIdByPayNo(each.getPayOrderNo());

            orderIds.forEach(order -> {
                Long id = order.getId();
                Integer orderType = order.getOrderType();
                // 查询关联的认养项目
                if (orderType.equals(OrderTypeConstant.ADOPT)) {
                    LambdaQueryWrapper<OrderDetailAdoptDO> adoptWrapper = Wrappers.lambdaQuery(OrderDetailAdoptDO.class)
                            .eq(OrderDetailAdoptDO::getOrderId, id);
                    List<OrderDetailAdoptDO> orderDetailAdopts = orderDetailAdoptMapper.selectList(adoptWrapper);
                    for (OrderDetailAdoptDO detailAdopt : orderDetailAdopts) {
                        PayOrderItemRespDTO payOrderItemResp = PayOrderItemRespDTO.builder()
                                .productName(detailAdopt.getItemName())
                                .productType(OrderTypeConstant.ADOPT)
                                .coverImage(detailAdopt.getItemImage())
                                .quantity(detailAdopt.getQuantity())
                                .build();
                        payOrderItems.add(payOrderItemResp);
                    }
                }
                // 查询关联的商品
                else if (orderType.equals(OrderTypeConstant.GOODS)) {
                    LambdaQueryWrapper<OrderDetailSkuDO> skuWrapper = Wrappers.lambdaQuery(OrderDetailSkuDO.class)
                            .eq(OrderDetailSkuDO::getOrderId, id);
                    List<OrderDetailSkuDO> orderDetailSkus = orderDetailSkuMapper.selectList(skuWrapper);
                    for (OrderDetailSkuDO detailSku : orderDetailSkus) {
                        PayOrderItemRespDTO payOrderItemResp = PayOrderItemRespDTO.builder()
                                .productName(detailSku.getSkuName())
                                .productType(OrderTypeConstant.GOODS)
                                .coverImage(detailSku.getSkuImage())
                                .quantity(detailSku.getQuantity())
                                .build();
                        payOrderItems.add(payOrderItemResp);
                    }
                }
            });
            result.setItems(payOrderItems);
            return result;
        });
    }
}
