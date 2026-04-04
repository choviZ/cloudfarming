package com.vv.cloudfarming.user.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Admin update featured flag request.
 */
@Data
public class FarmerFeatureUpdateReqDTO {

    @NotNull(message = "农户ID不能为空")
    private Long id;

    @NotNull(message = "精选状态不能为空")
    @Min(value = 0, message = "精选状态错误")
    @Max(value = 1, message = "精选状态错误")
    private Integer featuredFlag;
}
