package com.vv.cloudfarming.order.dto.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 认养订单分配牲畜明细
 */
@Data
public class OrderAssignAdoptItemReqDTO {

    /**
     * 认养项目id
     */
    @NotNull(message = "认养项目不能为空")
    private Long adoptItemId;

    /**
     * 耳标号列表
     */
    @NotEmpty(message = "耳标号不能为空")
    private List<String> earTagNos;
}
