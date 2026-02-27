package com.vv.cloudfarming.order.remote;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.product.dto.resp.ProductRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品SPU远程调用服务
 */
@FeignClient(value = "cloudfarming-product", contextId = "spuRemoteService",url = "${aggregation.remote-url:}")
public interface SpuRemoteService {

    @GetMapping("/api/spu/get")
    Result<ProductRespDTO> getSpuById(@RequestParam("id") Long id);
}