package com.vv.cloudfarming.order.dto.req;

import cn.hutool.json.JSONObject;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单请求参数
 */
@Data
public class OrderCreateReqDTO {

    /**
     * 订单类型
     */
    @NotNull
    private Integer orderType;

    /**
     * 业务数据
     */
    @NotNull
    JSONObject bizData;
}
