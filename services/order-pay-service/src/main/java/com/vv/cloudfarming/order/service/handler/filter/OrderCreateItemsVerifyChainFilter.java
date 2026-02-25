package com.vv.cloudfarming.order.service.handler.filter;

import com.vv.cloudfarming.common.enums.ShelfStatusEnum;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.dto.common.ProductItemDTO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.remote.AdoptItemRemoteService;
import com.vv.cloudfarming.order.remote.SkuRemoteService;
import com.vv.cloudfarming.order.service.basics.chain.OrderAbstractChainHandler;
import com.vv.cloudfarming.order.service.basics.chain.OrderContext;
import com.vv.cloudfarming.product.dto.resp.AdoptItemRespDTO;
import com.vv.cloudfarming.product.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.product.enums.ProductTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * 校验商品信息
 */
@Component
@RequiredArgsConstructor
public class OrderCreateItemsVerifyChainFilter implements OrderAbstractChainHandler<OrderCreateReqDTO> {

    private final SkuRemoteService skuRemoteService;
    private final AdoptItemRemoteService adoptItemRemoteService;
    private final OrderContext orderContext;

    @Override
    public void handler(OrderCreateReqDTO requestPram) {
        List<ProductItemDTO> items = requestPram.getItems();
        List<Long> ids = items.stream().map(ProductItemDTO::getBizId).toList();
        ProductTypeEnum productType = ProductTypeEnum.of(requestPram.getOrderType());

        switch (productType){
            // 校验农产品
            case SPU -> {
                HashMap<Long, SkuRespDTO> skuMap = new HashMap<>();

                List<SkuRespDTO> skus = skuRemoteService.listSkuDetailsByIds(ids).getData();
                if (skus.size() != ids.size()) {
                    throw new ServiceException("包含不存在的商品");
                }
                for (SkuRespDTO sku : skus) {
                    if (!sku.getStatus().equals(ShelfStatusEnum.ONLINE.getCode())) {
                        throw new ServiceException("包含下架的商品");
                    }
                    skuMap.put(sku.getId(), sku);
                }
                orderContext.setSkuMap(skuMap);
            }

            // 校验养殖项目
            case ADOPT -> {
                List<AdoptItemRespDTO> adopts = adoptItemRemoteService.batchAdoptItemByIds(ids).getData();
                if (adopts.size() != ids.size()) {
                    throw new ServiceException("包含不存在的养殖项目");
                }
                HashMap<Long, AdoptItemRespDTO> adoptMap = new HashMap<>();
                for (AdoptItemRespDTO adopt : adopts) {
                    if (!adopt.getStatus().equals(ShelfStatusEnum.ONLINE.getCode())) {
                        throw new ServiceException("包含下架的养殖项目");
                    }
                    adoptMap.put(adopt.getId(), adopt);
                }
                orderContext.setAdoptItemMap(adoptMap);
            }

            default -> throw new ServiceException("不支持的订单类型");
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
