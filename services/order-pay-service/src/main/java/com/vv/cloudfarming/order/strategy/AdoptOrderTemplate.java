package com.vv.cloudfarming.order.strategy;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.vv.cloudfarming.order.utils.RedisIdWorker;
import com.vv.cloudfarming.product.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.product.dao.mapper.AdoptItemMapper;
import com.vv.cloudfarming.user.service.ReceiveAddressService;
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
public class AdoptOrderTemplate extends AbstractOrderCreateTemplate<AdoptItemDO, OrderDetailAdoptDO> {

    private final AdoptItemMapper adoptItemMapper;
    private final OrderDetailAdoptMapper orderDetailAdoptMapper;

    public AdoptOrderTemplate(ReceiveAddressService addressService,
                              OrderMapper orderMapper,
                              RedisIdWorker redisIdWorker,
                              AdoptItemMapper adoptItemMapper,
                              OrderDetailAdoptMapper orderDetailAdoptMapper) {
        super(addressService, orderMapper, redisIdWorker);
        this.adoptItemMapper = adoptItemMapper;
        this.orderDetailAdoptMapper = orderDetailAdoptMapper;
    }

    @Override
    public Integer getOrderType() {
        return OrderTypeConstant.ADOPT;
    }

    @Override
    protected void validateRequest(OrderCreateContext<AdoptItemDO, OrderDetailAdoptDO> ctx) {
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
    protected Map<Long, AdoptItemDO> fetchProductInfo(OrderCreateContext<AdoptItemDO, OrderDetailAdoptDO> ctx) {
        List<Long> itemIds = ctx.getItems().stream()
                .map(ItemDTO::getBizId)
                .collect(Collectors.toList());

        List<AdoptItemDO> adoptItems = adoptItemMapper.selectBatchIds(itemIds);
        if (adoptItems.size() != itemIds.size()) {
            throw new ClientException("部分认养项目不存在");
        }

        // 校验项目状态
        for (AdoptItemDO adoptItem : adoptItems) {
            if (!ReviewStatusEnum.APPROVED.getStatus().equals(adoptItem.getReviewStatus())) {
                throw new ClientException("认养项目未通过审核: " + adoptItem.getTitle());
            }
            if (!ShelfStatusEnum.ONLINE.getCode().equals(adoptItem.getStatus())) {
                throw new ClientException("认养项目未上架: " + adoptItem.getTitle());
            }
        }

        return adoptItems.stream()
                .collect(Collectors.toMap(AdoptItemDO::getId, item -> item));
    }

    @Override
    protected Long getShopId(ItemDTO item, OrderCreateContext<AdoptItemDO, OrderDetailAdoptDO> ctx) {
        AdoptItemDO adoptItem = ctx.getProduct(item.getBizId());
        if (adoptItem == null || adoptItem.getShopId() == null) {
            throw new ClientException("认养项目不存在或缺少店铺信息: " + item.getBizId());
        }
        return adoptItem.getShopId();
    }

    @Override
    protected void lockStock(OrderCreateContext<AdoptItemDO, OrderDetailAdoptDO> ctx) {
        // 扣减认养项目可用数量
        for (ItemDTO item : ctx.getItems()) {
            LambdaUpdateWrapper<AdoptItemDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(AdoptItemDO::getId, item.getBizId())
                    .ge(AdoptItemDO::getAvailableCount, item.getQuantity())
                    .setSql("available_count = available_count - " + item.getQuantity());
            int updated = adoptItemMapper.update(null, wrapper);
            if (updated != 1) {
                AdoptItemDO adoptItem = ctx.getProduct(item.getBizId());
                throw new ClientException("认养项目库存不足: " + (adoptItem != null ? adoptItem.getTitle() : item.getBizId()));
            }
        }
    }

    @Override
    protected BigDecimal calculateTotalAmount(OrderCreateContext<AdoptItemDO, OrderDetailAdoptDO> ctx) {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemDTO item : ctx.getCurrentShopItems()) {
            AdoptItemDO adoptItem = ctx.getProduct(item.getBizId());
            BigDecimal itemAmount = adoptItem.getPrice().multiply(new BigDecimal(item.getQuantity()));
            total = total.add(itemAmount);
        }
        return total;
    }

    @Override
    protected List<OrderDetailAdoptDO> buildOrderDetails(OrderCreateContext<AdoptItemDO, OrderDetailAdoptDO> ctx) {
        OrderDO order = ctx.getCurrentOrder();
        return ctx.getCurrentShopItems().stream().map(item -> {
            AdoptItemDO adoptItem = ctx.getProduct(item.getBizId());
            BigDecimal itemAmount = adoptItem.getPrice().multiply(new BigDecimal(item.getQuantity()));

            // 计算认养周期
            Calendar calendar = Calendar.getInstance();
            Date startDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, adoptItem.getAdoptDays());
            Date endDate = calendar.getTime();

            return OrderDetailAdoptDO.builder()
                    .orderId(order.getId())
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
    protected void saveOrderDetails(OrderCreateContext<AdoptItemDO, OrderDetailAdoptDO> ctx) {
        Db.saveBatch(ctx.getAllDetails());
    }
}
