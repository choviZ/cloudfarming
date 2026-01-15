package com.vv.cloudfarming.shop.dto.req;

import lombok.Data;

/**
 * 创建认养订单请求DTO
 */
@Data
public class AdoptOrderCreateReqDTO {

    /**
     * 认养项目ID
     */
    private Long adoptItemId;
}
