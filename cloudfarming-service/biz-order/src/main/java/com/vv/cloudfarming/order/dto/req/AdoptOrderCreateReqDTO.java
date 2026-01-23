package com.vv.cloudfarming.order.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建认养订单请求DTO
 */
@Data
public class AdoptOrderCreateReqDTO {

    /**
     * 认养项目ID
     */
    @NotNull
    private Long adoptItemId;

    /**
     * 下单数量
     */
    @Min(value = 1)
    private Integer quantity;

    /**
     * 关联的收获地址id
     */
    @NotNull
    private Long receiveId;
}
