package com.vv.cloudfarming.product.remote;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.product.dto.resp.OrderSimpleRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 订单服务远程调用
 */
@FeignClient(name = "cloudfarming-order-pay", contextId = "orderRemoteService", url = "${aggregation.remote-url:}")
public interface OrderRemoteService {

    @PostMapping("/api/order/v1/inner/simple/list")
    Result<List<OrderSimpleRespDTO>> listSimpleOrdersByIds(@RequestBody List<Long> orderIds);
}
