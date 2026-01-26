package com.vv.cloudfarming.order.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.vv.cloudfarming.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单SKU明细表 DO
 * <p>
 * 存储具体的商品 SKU 级信息。
 * 无论是普通商品还是认养项目，都作为“商品”存储在这里。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_order_detail_sku")
public class OrderDetailSkuDO extends BaseDO implements Serializable {

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
     * 订单号 (冗余字段，方便查询)
     */
    private String orderNo;

    /**
     * 商品SKU ID (如果是认养，则是认养项目ID)
     */
    private Long skuId;

    /**
     * 商品SPU ID (可选)
     */
    private Long spuId;

    /**
     * 商品名称 / 认养项目名称
     */
    private String skuName;

    /**
     * 商品图片
     */
    private String skuImage;

    /**
     * 销售属性 (规格) JSON，如 {"颜色":"红", "尺寸":"L"}
     */
    private String skuSpecs;

    /**
     * 购买单价
     */
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 总金额 (price * quantity)
     */
    private BigDecimal totalAmount;
}
