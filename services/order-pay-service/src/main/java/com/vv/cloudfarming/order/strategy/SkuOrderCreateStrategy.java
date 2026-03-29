package com.vv.cloudfarming.order.strategy;

import cn.hutool.json.JSONUtil;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dto.common.ProductItemDTO;
import com.vv.cloudfarming.order.remote.SkuRemoteService;
import com.vv.cloudfarming.order.remote.SpuRemoteService;
import com.vv.cloudfarming.order.service.basics.chain.OrderContext;
import com.vv.cloudfarming.product.dto.req.LockStockReqDTO;
import com.vv.cloudfarming.product.dto.resp.ProductRespDTO;
import com.vv.cloudfarming.product.dto.resp.SkuRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SkuOrderCreateStrategy implements OrderCreateStrategy {

    private final OrderDetailSkuMapper orderDetailSkuMapper;
    private final SkuRemoteService skuRemoteService;
    private final SpuRemoteService spuRemoteService;

    @Override
    public Integer bizType() {
        return OrderTypeConstant.GOODS;
    }

    @Override
    public Long resolveShopId(ProductItemDTO item, OrderContext ctx) {
        SkuRespDTO sku = requireSku(item, ctx);
        ProductRespDTO spu = spuRemoteService.getSpuById(sku.getSpuId()).getData();
        if (spu == null || spu.getProductSpu() == null || spu.getProductSpu().getShopId() == null) {
            throw new ServiceException("商品店铺信息不存在，请刷新后重试");
        }
        return spu.getProductSpu().getShopId();
    }

    @Override
    public BigDecimal calculateOrderAmount(List<ProductItemDTO> items, OrderContext ctx) {
        BigDecimal total = BigDecimal.ZERO;
        for (ProductItemDTO item : items) {
            SkuRespDTO sku = requireSku(item, ctx);
            total = total.add(sku.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    @Override
    public void createOrderDetails(OrderDO order, List<ProductItemDTO> items, OrderContext ctx) {
        ArrayList<OrderDetailSkuDO> orderDetails = new ArrayList<>();
        for (ProductItemDTO item : items) {
            SkuRespDTO skuRespDTO = requireSku(item, ctx);
            OrderDetailSkuDO orderDetailSkuDO = OrderDetailSkuDO.builder()
                .orderNo(order.getOrderNo())
                .skuId(item.getBizId())
                .spuId(skuRespDTO.getSpuId())
                .skuName("默认商品名称")
                .skuImage(skuRespDTO.getSkuImage())
                .skuSpecs(JSONUtil.toJsonStr(skuRespDTO.getSaleAttribute()))
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
        for (ProductItemDTO item : items) {
            LockStockReqDTO requestParam = new LockStockReqDTO();
            requestParam.setId(item.getBizId());
            requestParam.setQuantity(item.getQuantity());

            Integer result = skuRemoteService.lockStock(requestParam).getData();
            if (result == null || result <= 0) {
                log.error("锁定库存失败，类型：农产品，id={}, 锁定数量={}", item.getBizId(), item.getQuantity());
                throw new RuntimeException("库存不足");
            }
            log.info("锁定库存成功，类型：农产品，id={}, 锁定数量={}", item.getBizId(), item.getQuantity());
        }
    }

    @Override
    public void unlockStock(List<ProductItemDTO> items) {
        for (ProductItemDTO item : items) {
            LockStockReqDTO requestParam = new LockStockReqDTO();
            requestParam.setId(item.getBizId());
            requestParam.setQuantity(item.getQuantity());

            Integer updated = skuRemoteService.unlockStock(requestParam).getData();
            if (updated == null || updated <= 0) {
                log.warn("恢复库存失败，类型：农产品，id={}, 恢复数量={}", item.getBizId(), item.getQuantity());
            }
        }
    }

    private SkuRespDTO requireSku(ProductItemDTO item, OrderContext ctx) {
        Map<Long, SkuRespDTO> skuMap = ctx.getSkuMap();
        if (skuMap == null) {
            throw new ServiceException("订单上下文缺少商品规格信息，请重试");
        }
        SkuRespDTO sku = skuMap.get(item.getBizId());
        if (sku == null) {
            throw new ServiceException("商品规格信息不存在或已失效，请重新下单");
        }
        return sku;
    }
}
