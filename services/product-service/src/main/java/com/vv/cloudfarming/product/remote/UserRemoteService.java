package com.vv.cloudfarming.product.remote;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "cloudfarming-user-operation")
public interface UserRemoteService {

    @GetMapping("/v1/user")
    Result<UserRespDTO> getUser();
}
