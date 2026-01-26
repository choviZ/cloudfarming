package com.vv.cloudfarming.order.model;

import lombok.Data;

@Data
public class AdoptOrderCreate {
    /**
     * 认养项目ID
     */
    private Long adoptItemId;

    /**
     * 认养数量
     */
    private Integer quantity;

    /**
     * 收货地址ID (用于寄送相关产品或合同)
     */
    private Long receiveId;
}
