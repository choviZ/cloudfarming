package com.vv.cloudfarming.order.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_pay")
public class PayDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 支付单号
     */
    private String payOrderNo;

    /**
     * 买家id
     */
    private Long buyerId;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 支付状态
     */
    private Integer payStatus;

    /**
     * 业务状态
     */
    private Integer bizStatus;

    /**
     * 支付方式：alipay
     */
    private Integer payChannel;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
}
