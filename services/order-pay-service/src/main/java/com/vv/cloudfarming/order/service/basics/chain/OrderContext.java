package com.vv.cloudfarming.order.service.basics.chain;

import com.vv.cloudfarming.product.dto.resp.AdoptItemRespDTO;
import com.vv.cloudfarming.product.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Setter
@Getter
public class OrderContext {

    /**
     * 农产品SKU
     */
    private Map<Long, SkuRespDTO> skuMap;

    /**
     * 委托养殖项目
     */
    private Map<Long, AdoptItemRespDTO> adoptItemMap;

    /**
     * 收货地址信息
     */
    private ReceiveAddressRespDTO receiveAddress;
}
