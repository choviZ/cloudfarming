package com.vv.cloudfarming.product.dto.req;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建秒杀活动请求参数
 */
@Data
public class SeckillActivityCreateReqDTO {

    /**
     * 活动名称
     */
    @NotBlank(message = "活动名称不能为空")
    private String activityName;

    /**
     * 秒杀 SKU ID
     */
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    /**
     * 商品原价，未传时默认使用 SKU 当前价格
     */
    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal originalPrice;

    /**
     * 秒杀价
     */
    @NotNull(message = "秒杀价不能为空")
    @DecimalMin(value = "0.01", message = "秒杀价必须大于0")
    private BigDecimal seckillPrice;

    /**
     * 秒杀总库存
     */
    @NotNull(message = "秒杀库存不能为空")
    @Min(value = 1, message = "秒杀库存至少为1")
    private Integer totalStock;

    /**
     * 单用户限购
     */
    @NotNull(message = "单用户限购不能为空")
    @Min(value = 1, message = "单用户限购至少为1")
    private Integer limitPerUser;

    /**
     * 活动开始时间
     */
    @NotNull(message = "活动开始时间不能为空")
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    @NotNull(message = "活动结束时间不能为空")
    private LocalDateTime endTime;
}
