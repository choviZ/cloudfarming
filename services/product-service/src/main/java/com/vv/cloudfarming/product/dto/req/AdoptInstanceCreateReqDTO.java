package com.vv.cloudfarming.product.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 养殖实例创建明细
 */
@Data
public class AdoptInstanceCreateReqDTO {

    /**
     * 认养项目id
     */
    @NotNull(message = "认养项目不能为空")
    private Long itemId;

    /**
     * 耳标号
     */
    @NotNull(message = "耳标号不能为空")
    private Long earTagNo;
}
