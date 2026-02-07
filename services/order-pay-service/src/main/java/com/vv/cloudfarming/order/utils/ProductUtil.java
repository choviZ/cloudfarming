package com.vv.cloudfarming.order.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dao.entity.OrderDetailAdoptDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailAdoptMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dto.common.ProductSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductUtil {

    private final OrderDetailAdoptMapper orderDetailAdoptMapper;
    private final OrderDetailSkuMapper orderDetailSkuMapper;

    /**
     * 构建订单列表页中展示的的商品摘要信息
     * @param orderId 订单id
     * @param orderType 订单类型
     * @param productSummaries 摘要信息集合
     */
    public void buildProductSummary(Long orderId, Integer orderType, List<ProductSummaryDTO> productSummaries){
        // 查询关联的认养项目
        if (orderType.equals(OrderTypeConstant.ADOPT)) {
            LambdaQueryWrapper<OrderDetailAdoptDO> adoptWrapper = Wrappers.lambdaQuery(OrderDetailAdoptDO.class)
                    .eq(OrderDetailAdoptDO::getOrderNo, orderId);
            List<OrderDetailAdoptDO> orderDetailAdopts = orderDetailAdoptMapper.selectList(adoptWrapper);
            for (OrderDetailAdoptDO detailAdopt : orderDetailAdopts) {
                ProductSummaryDTO payOrderItemResp = ProductSummaryDTO.builder()
                        .productName(detailAdopt.getItemName())
                        .productType(OrderTypeConstant.ADOPT)
                        .coverImage(detailAdopt.getItemImage())
                        .price(detailAdopt.getPrice())
                        .quantity(detailAdopt.getQuantity())
                        .build();
                productSummaries.add(payOrderItemResp);
            }
        }
        // 查询关联的商品
        else if (orderType.equals(OrderTypeConstant.GOODS)) {
            LambdaQueryWrapper<OrderDetailSkuDO> skuWrapper = Wrappers.lambdaQuery(OrderDetailSkuDO.class)
                    .eq(OrderDetailSkuDO::getOrderId, orderId);
            List<OrderDetailSkuDO> orderDetailSkus = orderDetailSkuMapper.selectList(skuWrapper);
            for (OrderDetailSkuDO detailSku : orderDetailSkus) {
                ProductSummaryDTO payOrderItemResp = ProductSummaryDTO.builder()
                        .productName(detailSku.getSkuName())
                        .productType(OrderTypeConstant.GOODS)
                        .coverImage(detailSku.getSkuImage())
                        .price(detailSku.getPrice())
                        .quantity(detailSku.getQuantity())
                        .build();
                productSummaries.add(payOrderItemResp);
            }
        }
    }
}
