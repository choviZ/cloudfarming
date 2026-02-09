package com.vv.cloudfarming.order.remote;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 收获地址信息远程服务
 */
@FeignClient(value = "cloudfarming-user-operation", contextId = "receiveAddressRemoteService", url = "${aggregation.remote-url:}")
public interface ReceiveAddressRemoteService {

    @GetMapping("/api/receive-address/get")
    Result<ReceiveAddressRespDTO> getReceiveAddressById(@RequestParam("id") Long id);
}
