package com.vv.cloudfarming.order.strategy;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailAdoptDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailAdoptMapper;
import com.vv.cloudfarming.order.dto.common.ProductItemDTO;
import com.vv.cloudfarming.order.remote.AdoptItemRemoteService;
import com.vv.cloudfarming.order.service.basics.chain.OrderContext;
import com.vv.cloudfarming.product.dto.req.LockStockReqDTO;
import com.vv.cloudfarming.product.dto.resp.AdoptItemRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdoptOrderCreateStrategy implements OrderCreateStrategy {

    private final OrderDetailAdoptMapper orderDetailAdoptMapper;
    private final AdoptItemRemoteService adoptItemRemoteService;

    @Override
    public Integer bizType() {
        return OrderTypeConstant.ADOPT;
    }

    @Override
    public Long resolveShopId(ProductItemDTO item, OrderContext ctx) {
        return ctx.getAdoptItemMap().get(item.getBizId()).getShopId();
    }

    @Override
    public BigDecimal calculateOrderAmount(List<ProductItemDTO> items, OrderContext ctx) {
        BigDecimal total = BigDecimal.ZERO;
        for (ProductItemDTO item : items) {
            AdoptItemRespDTO adopt = ctx.getAdoptItemMap().get(item.getBizId());
            total = total.add(adopt.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    @Override
    public void createOrderDetails(OrderDO order, List<ProductItemDTO> items, OrderContext ctx) {
        ArrayList<OrderDetailAdoptDO> orderDetails = new ArrayList<>();
        for (ProductItemDTO item : items) {
            AdoptItemRespDTO adopt = ctx.getAdoptItemMap().get(item.getBizId());

            OrderDetailAdoptDO detail = OrderDetailAdoptDO.builder()
                    .orderNo(order.getOrderNo())
                    .adoptItemId(adopt.getId())
                    .itemName(adopt.getTitle())
                    .itemImage(adopt.getCoverImage())
                    .price(adopt.getPrice())
                    .quantity(item.getQuantity())
                    .totalAmount(adopt.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .startDate(new Date())
                    .endDate(DateUtil.offset(new Date(), DateField.DAY_OF_MONTH, adopt.getAdoptDays()))
                    .build();
            orderDetails.add(detail);
        }
        orderDetailAdoptMapper.insert(orderDetails);
    }

    @Override
    public void lockedStock(List<ProductItemDTO> items) {
        for (ProductItemDTO item : items){
            LockStockReqDTO lockStockReqDTO = new LockStockReqDTO();
            lockStockReqDTO.setId(item.getBizId());
            lockStockReqDTO.setQuantity(item.getQuantity());

            int updated = adoptItemRemoteService.lockAdoptItemStock(lockStockReqDTO).getData();
            if (!SqlHelper.retBool(updated)){
                log.error("锁定库存失败，类型：委托养殖项目，id：{}，锁定数量：{}",item.getBizId(),item.getQuantity());
                throw new ServiceException("锁定库存失败");
            }
            log.info("锁定库存成功，类型：委托养殖项目，id：{}，锁定数量：{}",item.getBizId(),item.getQuantity());
        }
    }
}
