package com.vv.cloudfarming.order.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 认养订单创建请求参数
 */
@Data
public class AdoptOrderCreateReqDTO {

    /**
     * 认养项目id
     */
    @NotNull
    private Long adoptItemId;

    /**
     * 下单数量
     */
    @NotNull
    @Min(value = 1)
    private Integer quantity;

    /**
     * 收获地址
     */
    @NotNull
    Long receiveId;
}
