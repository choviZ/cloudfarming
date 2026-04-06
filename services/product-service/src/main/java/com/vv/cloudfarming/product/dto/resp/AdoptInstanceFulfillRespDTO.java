package com.vv.cloudfarming.product.dto.resp;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 养殖实例履约结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdoptInstanceFulfillRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联订单ID
     */
    private Long ownerOrderId;

    /**
     * 该订单下的养殖实例是否已全部履约完成
     */
    private Boolean allFulfilled;
}
