package com.vv.cloudfarming.order.remote;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.product.dto.resp.ShopRespDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        value = "cloudfarming-product",
        contextId = "shopRemoteService",
        url = "${aggregation.remote-url:}")
public interface ShopRemoteService {

    @GetMapping("/api/shop/get")
    Result<ShopRespDTO> getShopById(@RequestParam("shopId") @NotNull Long shopId);
}