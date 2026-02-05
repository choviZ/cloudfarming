package com.vv.cloudfarming.order.strategy;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dto.common.ItemDTO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.remote.SkuRemoteService;
import com.vv.cloudfarming.order.remote.StockRemoteService;
import com.vv.cloudfarming.order.utils.RedisIdWorker;
import com.vv.cloudfarming.product.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.order.remote.ReceiveAddressRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品订单创建模板
 * 实现普通商品（SKU）订单的差异化逻辑
 */
@Slf4j
@Component
public class GoodsOrderTemplate extends AbstractOrderCreateTemplate<SkuRespDTO, OrderDetailSkuDO> {

    private final SkuRemoteService skuService;
    private final OrderDetailSkuMapper orderDetailSkuMapper;

    public GoodsOrderTemplate(ReceiveAddressRemoteService addressService,
                              OrderMapper orderMapper,
                              RedisIdWorker redisIdWorker,
                              StockRemoteService stockService,
                              SkuRemoteService skuService,
                              OrderDetailSkuMapper orderDetailSkuMapper) {
        super(addressService, orderMapper, redisIdWorker,stockService);
        this.skuService = skuService;
        this.orderDetailSkuMapper = orderDetailSkuMapper;
    }

    @Override
    public Integer getOrderType() {
        return OrderTypeConstant.GOODS;
    }

    @Override
    protected void validateRequest(OrderCreateContext<SkuRespDTO, OrderDetailSkuDO> ctx) {
        OrderCreateReqDTO req = ctx.getRequest();
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ClientException("商品列表不能为空");
        }
        for (ItemDTO item : req.getItems()) {
            if (item.getBizId() == null) {
                throw new ClientException("商品ID不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new ClientException("商品数量必须大于0");
            }
        }
    }

    @Override
    protected Map<Long, SkuRespDTO> fetchProductInfo(OrderCreateContext<SkuRespDTO, OrderDetailSkuDO> ctx) {
        List<Long> skuIds = ctx.getItems().stream()
                .map(ItemDTO::getBizId)
                .collect(Collectors.toList());

        List<SkuRespDTO> skus = skuService.listSkuDetailsByIds(skuIds).getData();
        if (skus.size() != skuIds.size()) {
            throw new ClientException("部分商品不存在或已下架");
        }

        return skus.stream()
                .collect(Collectors.toMap(SkuRespDTO::getId, s -> s));
    }

    @Override
    protected Long getShopId(ItemDTO item, OrderCreateContext<SkuRespDTO, OrderDetailSkuDO> ctx) {
        SkuRespDTO sku = ctx.getProduct(item.getBizId());
        if (sku == null || sku.getShopId() == null) {
            throw new ClientException("商品不存在或缺少店铺信息: " + item.getBizId());
        }
        return sku.getShopId();
    }

    @Override
    protected BigDecimal calculateTotalAmount(OrderCreateContext<SkuRespDTO, OrderDetailSkuDO> ctx) {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemDTO item : ctx.getCurrentShopItems()) {
            SkuRespDTO sku = ctx.getProduct(item.getBizId());
            BigDecimal itemAmount = sku.getPrice().multiply(new BigDecimal(item.getQuantity()));
            total = total.add(itemAmount);
        }
        return total;
    }

    @Override
    protected List<OrderDetailSkuDO> buildOrderDetails(OrderCreateContext<SkuRespDTO, OrderDetailSkuDO> ctx) {
        OrderDO order = ctx.getCurrentOrder();
        return ctx.getCurrentShopItems().stream().map(item -> {
            SkuRespDTO sku = ctx.getProduct(item.getBizId());
            BigDecimal itemAmount = sku.getPrice().multiply(new BigDecimal(item.getQuantity()));

            return OrderDetailSkuDO.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .skuId(sku.getId())
                    .spuId(sku.getSpuId())
                    .skuName(sku.getSpuTitle())
                    .skuImage(sku.getSpuImage())
                    .skuSpecs(JSONUtil.toJsonStr(sku.getSaleAttrs()))
                    .price(sku.getPrice())
                    .quantity(item.getQuantity())
                    .totalAmount(itemAmount)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    protected void saveOrderDetails(OrderCreateContext<SkuRespDTO, OrderDetailSkuDO> ctx) {
        Db.saveBatch(ctx.getAllDetails());
    }
}
