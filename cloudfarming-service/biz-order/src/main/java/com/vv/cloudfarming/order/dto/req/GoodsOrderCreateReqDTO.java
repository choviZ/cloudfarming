package com.vv.cloudfarming.order.dto.req;

import com.vv.cloudfarming.order.dto.OrderItemDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GoodsOrderCreateReqDTO {

    /**
     * 商品列表
     */
    @NotEmpty
    @Valid
    List<OrderItemDTO> items;

    /**
     * 收获地址
     */
    @NotNull
    Long receiveId;

    /**
     * 备注
     */
    private String remark;
}
