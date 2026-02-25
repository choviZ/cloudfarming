package com.vv.cloudfarming.order.service.handler.filter;

import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dto.common.ProductItemDTO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.remote.ReceiveAddressRemoteService;
import com.vv.cloudfarming.order.service.basics.chain.OrderAbstractChainHandler;
import com.vv.cloudfarming.order.service.basics.chain.OrderContext;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基础校验
 */
@Component
@RequiredArgsConstructor
public class OrderCreateBaseVerifyChainFilter implements OrderAbstractChainHandler<OrderCreateReqDTO> {

    private final ReceiveAddressRemoteService receiveAddressRemoteService;
    private final OrderContext orderContext;

    @Override
    public void handler(OrderCreateReqDTO requestParam) {
        Integer orderType = requestParam.getOrderType();
        if (!orderType.equals(OrderTypeConstant.ADOPT) && !orderType.equals(OrderTypeConstant.GOODS)) {
            throw new ClientException("不支持的订单类型");
        }
        List<ProductItemDTO> items = requestParam.getItems();
        if (items == null || items.isEmpty()) {
            throw new ClientException("未选择商品");
        }
        Long receiveId = requestParam.getReceiveId();
        Result<ReceiveAddressRespDTO> address = receiveAddressRemoteService.getReceiveAddressById(receiveId);
        if (address.getData() == null) {
            throw new ServiceException("收货地址不存在");
        }
        orderContext.setReceiveAddress(address.getData());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
