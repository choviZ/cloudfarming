package com.vv.cloudfarming.order.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 农户完成认养实例履约请求参数
 */
@Data
public class OrderFulfillAdoptReqDTO {

    /**
     * 养殖实例ID
     */
    @NotNull(message = "养殖实例ID不能为空")
    private Long instanceId;
}
