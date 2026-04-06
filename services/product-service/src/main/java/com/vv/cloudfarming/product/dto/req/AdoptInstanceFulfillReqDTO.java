package com.vv.cloudfarming.product.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 养殖实例履约请求参数
 */
@Data
public class AdoptInstanceFulfillReqDTO {

    /**
     * 养殖实例ID
     */
    @NotNull(message = "养殖实例ID不能为空")
    private Long instanceId;
}
