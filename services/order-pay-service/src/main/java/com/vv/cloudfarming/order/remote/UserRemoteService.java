package com.vv.cloudfarming.order.remote;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    value = "cloudfarming-user-operation",
    contextId = "userRemoteService",
    url = "${aggregation.remote-url:}"
)
public interface UserRemoteService {

    @GetMapping("/api/user/get")
    Result<UserRespDTO> getUserById(@RequestParam("id") @NotNull Long id);
}
