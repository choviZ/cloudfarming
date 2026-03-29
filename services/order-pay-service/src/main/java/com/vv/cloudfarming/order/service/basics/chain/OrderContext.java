package com.vv.cloudfarming.order.service.basics.chain;

import com.vv.cloudfarming.product.dto.resp.AdoptItemRespDTO;
import com.vv.cloudfarming.product.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderContext {

    private final ThreadLocal<Map<Long, SkuRespDTO>> skuMapHolder = new ThreadLocal<>();
    private final ThreadLocal<Map<Long, AdoptItemRespDTO>> adoptItemMapHolder = new ThreadLocal<>();
    private final ThreadLocal<ReceiveAddressRespDTO> receiveAddressHolder = new ThreadLocal<>();

    public Map<Long, SkuRespDTO> getSkuMap() {
        return skuMapHolder.get();
    }

    public void setSkuMap(Map<Long, SkuRespDTO> skuMap) {
        skuMapHolder.set(skuMap);
    }

    public Map<Long, AdoptItemRespDTO> getAdoptItemMap() {
        return adoptItemMapHolder.get();
    }

    public void setAdoptItemMap(Map<Long, AdoptItemRespDTO> adoptItemMap) {
        adoptItemMapHolder.set(adoptItemMap);
    }

    public ReceiveAddressRespDTO getReceiveAddress() {
        return receiveAddressHolder.get();
    }

    public void setReceiveAddress(ReceiveAddressRespDTO receiveAddress) {
        receiveAddressHolder.set(receiveAddress);
    }

    public void clear() {
        skuMapHolder.remove();
        adoptItemMapHolder.remove();
        receiveAddressHolder.remove();
    }
}
