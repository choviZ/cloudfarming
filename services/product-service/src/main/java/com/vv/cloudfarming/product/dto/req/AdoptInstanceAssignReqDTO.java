package com.vv.cloudfarming.product.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 认养订单分配牲畜请求参数
 */
@Data
public class AdoptInstanceAssignReqDTO {

    /**
     * 农户id
     */
    @NotNull(message = "农户id不能为空")
    private Long farmerId;

    /**
     * 认养用户id
     */
    @NotNull(message = "认养用户id不能为空")
    private Long ownerUserId;

    /**
     * 订单id
     */
    @NotNull(message = "订单id不能为空")
    private Long ownerOrderId;

    /**
     * 养殖实例列表
     */
    @Valid
    @NotEmpty(message = "养殖实例不能为空")
    private List<AdoptInstanceCreateReqDTO> instances;
}
