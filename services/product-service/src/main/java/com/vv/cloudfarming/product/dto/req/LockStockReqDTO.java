package com.vv.cloudfarming.product.dto.req;

import lombok.Data;

/**
 * 库存相关请求
 */
@Data
public class LockStockReqDTO {

    private Long id;

    private Integer quantity;
}
