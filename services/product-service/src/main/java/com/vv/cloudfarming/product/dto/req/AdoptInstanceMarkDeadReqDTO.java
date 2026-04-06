package com.vv.cloudfarming.product.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 养殖实例异常死亡处理请求参数
 */
@Data
public class AdoptInstanceMarkDeadReqDTO {

    /**
     * 养殖实例ID
     */
    @NotNull(message = "养殖实例ID不能为空")
    private Long instanceId;

    /**
     * 死亡时间
     */
    private Date deathTime;

    /**
     * 死亡原因
     */
    @NotBlank(message = "死亡原因不能为空")
    private String deathReason;
}
