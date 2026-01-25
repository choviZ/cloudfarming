package com.vv.cloudfarming.order.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.cloudfarming.common.database.BaseDO;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 认养订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_adopt_order")
public class AdoptOrderDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 买家用户ID
     */
    private Long buyerId;

    /**
     * 认养项目ID
     */
    private Long adoptItemId;

    /**
     * 认养价格（下单时快照）
     */
    private BigDecimal price;

    /**
     * 认养开始日期
     */
    private Date startDate;

    /**
     * 认养结束日期
     */
    private Date endDate;

    /**
     * 支付状态
     */
    private Integer payStatus;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 认养状态：1=认养中 2=已完成 3=已取消
     */
    private Integer orderStatus;

    /**
     * 关联的收获地址id
     */
    private Long receiveId;
}
