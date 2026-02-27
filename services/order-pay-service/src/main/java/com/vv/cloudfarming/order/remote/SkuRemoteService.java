package com.vv.cloudfarming.order.remote;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.product.dto.req.LockStockReqDTO;
import com.vv.cloudfarming.product.dto.resp.SkuRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 库存远程调用服务
 */
@FeignClient(value = "cloudfarming-product", contextId = "skuRemoteService",url = "${aggregation.remote-url:}")
public interface SkuRemoteService {

    @PostMapping("/api/sku/list")
    Result<List<SkuRespDTO>> listSkuDetailsByIds(@RequestBody List<Long> ids);

    /**
     * 锁定库存
     */
    @PostMapping("/api/sku/stock/lock")
    Result<Integer> lockStock(@RequestBody LockStockReqDTO requestParam);

    @PostMapping("/api/sku/stock/unlock")
    Result<Integer> unlockStock(@RequestBody LockStockReqDTO requestParam);

    @PostMapping("/api/sku/stock/deduct")
    Result<Integer> deductStock(@RequestBody LockStockReqDTO requestParam);
}