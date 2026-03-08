package com.vv.cloudfarming.product.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("t_seckill_activity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillActivityDO extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 关联商品 - SPU ID
     */
    private Long spuId;

    /**
     * 关联商品 - SKU ID
     */
    private Long skuId;

    /**
     * 店铺ID（冗余，便于查询）
     */
    private Long shopId;

    /**
     * 秒杀价格（独立于原价）
     */
    private BigDecimal originalPrice;

    /**
     * 秒杀价
     */
    private BigDecimal seckillPrice;

    /**
     * 秒杀库存
     */
    private Integer totalStock;

    /**
     * 剩余可售库存
     */
    private Integer stock;

    /**
     * 锁定库存
     */
    private Integer lockStock;

    /**
     * 活动规则 - 每人限购数量
     */
    private Integer limitPerUser;

    /**
     * 时间控制 - 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 时间控制 - 活动结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态: 0-未开始 1-进行中 2-已结束 3-已取消
     */
    private Integer status;

    /**
     * 审核状态: 0-待审核 1-通过 2-拒绝
     */
    private Integer auditStatus;
}
