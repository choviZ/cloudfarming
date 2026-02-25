package com.vv.cloudfarming.order.strategy;

import cn.hutool.json.JSONUtil;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dto.common.ProductItemDTO;
import com.vv.cloudfarming.order.remote.SkuRemoteService;
import com.vv.cloudfarming.order.service.basics.chain.OrderContext;
import com.vv.cloudfarming.product.dto.req.LockStockReqDTO;
import com.vv.cloudfarming.product.dto.resp.SkuRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SkuOrderCreateStrategy implements OrderCreateStrategy {

    private final OrderDetailSkuMapper orderDetailSkuMapper;
    private final SkuRemoteService skuRemoteService;

    @Override
    public Integer bizType() {
        return OrderTypeConstant.GOODS;
    }

    @Override
    public Long resolveShopId(ProductItemDTO item, OrderContext ctx) {
        return ctx.getSkuMap().get(item.getBizId()).getShopId();
    }

    @Override
    public BigDecimal calculateOrderAmount(List<ProductItemDTO> items, OrderContext ctx) {
        BigDecimal total = BigDecimal.ZERO;
        for (ProductItemDTO item : items) {
            SkuRespDTO sku = ctx.getSkuMap().get(item.getBizId());
            total = total.add(sku.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    @Override
    public void createOrderDetails(OrderDO order, List<ProductItemDTO> items, OrderContext ctx) {
        ArrayList<OrderDetailSkuDO> orderDetails = new ArrayList<>();
        for (ProductItemDTO item : items) {
            SkuRespDTO skuRespDTO = ctx.getSkuMap().get(item.getBizId());

            OrderDetailSkuDO orderDetailSkuDO = OrderDetailSkuDO.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .skuId(item.getBizId())
                    .spuId(skuRespDTO.getSpuId())
                    .skuName(skuRespDTO.getSpuTitle())
                    .skuImage(skuRespDTO.getSpuImage())
                    .skuSpecs(JSONUtil.toJsonStr(skuRespDTO.getSaleAttrs()))
                    .price(skuRespDTO.getPrice())
                    .quantity(item.getQuantity())
                    .totalAmount(skuRespDTO.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();

            orderDetails.add(orderDetailSkuDO);
        }
        orderDetailSkuMapper.insert(orderDetails);
    }

    @Override
    public void lockedStock(List<ProductItemDTO> items) {
        for (ProductItemDTO item : items){
            LockStockReqDTO requestParam = new LockStockReqDTO();
            requestParam.setId(item.getBizId());
            requestParam.setQuantity(item.getQuantity());

            Integer result = skuRemoteService.lockStock(requestParam).getData();
            if (result == null || result <= 0){
                log.error("锁定库存失败，类型：农产品，id：{}，锁定数量：{}",item.getBizId(),item.getQuantity());
                throw new RuntimeException("库存不足");
            }
            log.info("锁定库存成功，类型：农产品，id：{}，锁定数量：{}",item.getBizId(),item.getQuantity());
        }
    }
}
