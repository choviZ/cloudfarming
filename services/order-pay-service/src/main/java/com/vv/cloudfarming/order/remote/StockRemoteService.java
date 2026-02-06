package com.vv.cloudfarming.order.remote;

import com.vv.cloudfarming.common.result.Result;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 库存远程调用服务
 */
@FeignClient(value = "cloudfarming-product", contextId = "stockRemoteService",url = "${aggregation.remote-url:}")
public interface StockRemoteService {

    @GetMapping("/stock/lock")
    Result<Void> lock(@RequestParam("id") @NotNull Long id, @RequestParam("quantity") @NotNull int quantity, @RequestParam("bizType") @NotNull int bizType);

    @GetMapping("/stock/unlock")
    Result<Void> unlock(@RequestParam("id") @NotNull Long id, @RequestParam("quantity") @NotNull int quantity, @RequestParam("bizType") @NotNull int bizType);
}

