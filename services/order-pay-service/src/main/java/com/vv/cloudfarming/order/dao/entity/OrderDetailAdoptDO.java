package com.vv.cloudfarming.order.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 认养订单明细表 DO
 * <p>
 * 仅当 order_type = 2 (认养订单) 时，会有对应的记录。
 * 存储认养项目的具体信息及扩展业务数据。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_order_detail_adopt")
public class OrderDetailAdoptDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联的订单ID (t_order.id)
     */
    private Long orderId;

    /**
     * 认养项目ID
     */
    private Long adoptItemId;

    /**
     * 认养项目名称
     */
    private String itemName;

    /**
     * 项目图片
     */
    private String itemImage;

    /**
     * 认养价格
     */
    private BigDecimal price;

    /**
     * 认养数量
     */
    private Integer quantity;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 认养开始时间
     */
    private Date startDate;

    /**
     * 认养结束时间
     */
    private Date endDate;
}
