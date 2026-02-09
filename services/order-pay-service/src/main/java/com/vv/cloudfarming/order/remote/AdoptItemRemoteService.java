package com.vv.cloudfarming.order.remote;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.product.dto.resp.AdoptItemRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 认养项目远程调用服务
 */
@FeignClient(value = "cloudfarming-product", contextId = "adoptItemRemoteService", url = "${aggregation.remote-url:}")
public interface AdoptItemRemoteService {

    @PostMapping("/api/adopt/item/v1/batch")
    Result<List<AdoptItemRespDTO>> batchAdoptItemByIds(@RequestBody List<Long> ids);
}