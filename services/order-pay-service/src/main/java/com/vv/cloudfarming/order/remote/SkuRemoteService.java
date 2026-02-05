package com.vv.cloudfarming.order.remote;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.product.dto.resp.SkuRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 库存远程调用服务
 */
@FeignClient(value = "cloudfarming-product", contextId = "skuRemoteService")
public interface SkuRemoteService {

    @PostMapping("/v1/sku/list")
    Result<List<SkuRespDTO>> listSkuDetailsByIds(@RequestBody List<Long> ids);
}