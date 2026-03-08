package com.vv.cloudfarming.order.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeckillCreateReqDTO {

    @NotNull(message = "秒杀活动ID不能为空")
    private Long seckillId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1")
    private Integer nums;

    @NotNull(message = "收货地址不能为空")
    private Long receiveId;
}
