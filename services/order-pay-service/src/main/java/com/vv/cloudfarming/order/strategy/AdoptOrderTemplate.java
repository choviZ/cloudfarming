package com.vv.cloudfarming.order.strategy;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.vv.cloudfarming.common.enums.ReviewStatusEnum;
import com.vv.cloudfarming.common.enums.ShelfStatusEnum;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailAdoptDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailAdoptMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dto.common.ItemDTO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.remote.AdoptItemRemoteService;
import com.vv.cloudfarming.order.remote.ReceiveAddressRemoteService;
import com.vv.cloudfarming.order.remote.StockRemoteService;
import com.vv.cloudfarming.order.utils.RedisIdWorker;
import com.vv.cloudfarming.product.dto.resp.AdoptItemRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 认养订单创建模板
 * 实现认养项目订单的差异化逻辑
 */
@Slf4j
@Component
public class AdoptOrderTemplate extends AbstractOrderCreateTemplate<AdoptItemRespDTO, OrderDetailAdoptDO> {

    private final AdoptItemRemoteService adoptItemService;
    private final OrderDetailAdoptMapper orderDetailAdoptMapper;

    public AdoptOrderTemplate(ReceiveAddressRemoteService addressService,
                              OrderMapper orderMapper,
                              RedisIdWorker redisIdWorker,
                              StockRemoteService stockService,
                              AdoptItemRemoteService adoptItemRemoteService,
                              OrderDetailAdoptMapper orderDetailAdoptMapper) {
        super(addressService, orderMapper, redisIdWorker, stockService);
        this.adoptItemService = adoptItemRemoteService;
        this.orderDetailAdoptMapper = orderDetailAdoptMapper;
    }

    @Override
    public Integer getOrderType() {
        return OrderTypeConstant.ADOPT;
    }

    @Override
    protected void validateRequest(OrderCreateContext<AdoptItemRespDTO, OrderDetailAdoptDO> ctx) {
        OrderCreateReqDTO req = ctx.getRequest();
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ClientException("认养项目列表不能为空");
        }
        for (ItemDTO item : req.getItems()) {
            if (item.getBizId() == null) {
                throw new ClientException("认养项目ID不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new ClientException("认养数量必须大于0");
            }
        }
    }

    @Override
    protected Map<Long, AdoptItemRespDTO> fetchProductInfo(OrderCreateContext<AdoptItemRespDTO, OrderDetailAdoptDO> ctx) {
        List<Long> itemIds = ctx.getItems().stream()
                .map(ItemDTO::getBizId)
                .collect(Collectors.toList());

        List<AdoptItemRespDTO> adoptItems = adoptItemService.batchAdoptItemByIds(itemIds).getData();
        if (adoptItems.size() != itemIds.size()) {
            throw new ClientException("部分认养项目不存在");
        }

        // 校验项目状态
        for (AdoptItemRespDTO adoptItem : adoptItems) {
            if (!ReviewStatusEnum.APPROVED.getStatus().equals(adoptItem.getAuditStatus())) {
                throw new ClientException("认养项目未通过审核: " + adoptItem.getTitle());
            }
            if (!ShelfStatusEnum.ONLINE.getCode().equals(adoptItem.getStatus())) {
                throw new ClientException("认养项目未上架: " + adoptItem.getTitle());
            }
        }
        return adoptItems.stream()
                .collect(Collectors.toMap(AdoptItemRespDTO::getId, item -> item));
    }

    @Override
    protected Long getShopId(ItemDTO item, OrderCreateContext<AdoptItemRespDTO, OrderDetailAdoptDO> ctx) {
        AdoptItemRespDTO adoptItem = ctx.getProduct(item.getBizId());
        if (adoptItem == null || adoptItem.getShopId() == null) {
            throw new ClientException("认养项目不存在或缺少店铺信息: " + item.getBizId());
        }
        return adoptItem.getShopId();
    }

    @Override
    protected BigDecimal calculateTotalAmount(OrderCreateContext<AdoptItemRespDTO, OrderDetailAdoptDO> ctx) {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemDTO item : ctx.getCurrentShopItems()) {
            AdoptItemRespDTO adoptItem = ctx.getProduct(item.getBizId());
            BigDecimal itemAmount = adoptItem.getPrice().multiply(new BigDecimal(item.getQuantity()));
            total = total.add(itemAmount);
        }
        return total;
    }

    @Override
    protected List<OrderDetailAdoptDO> buildOrderDetails(OrderCreateContext<AdoptItemRespDTO, OrderDetailAdoptDO> ctx) {
        OrderDO order = ctx.getCurrentOrder();
        return ctx.getCurrentShopItems().stream().map(item -> {
            AdoptItemRespDTO adoptItem = ctx.getProduct(item.getBizId());
            BigDecimal itemAmount = adoptItem.getPrice().multiply(new BigDecimal(item.getQuantity()));

            // 计算认养周期
            Calendar calendar = Calendar.getInstance();
            Date startDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, adoptItem.getAdoptDays());
            Date endDate = calendar.getTime();

            return OrderDetailAdoptDO.builder()
                    .orderNo(order.getOrderNo())
                    .adoptItemId(adoptItem.getId())
                    .itemName(adoptItem.getTitle())
                    .itemImage(adoptItem.getCoverImage())
                    .price(adoptItem.getPrice())
                    .quantity(item.getQuantity())
                    .totalAmount(itemAmount)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    protected void saveOrderDetails(OrderCreateContext<AdoptItemRespDTO, OrderDetailAdoptDO> ctx) {
        Db.saveBatch(ctx.getAllDetails());
    }
}
