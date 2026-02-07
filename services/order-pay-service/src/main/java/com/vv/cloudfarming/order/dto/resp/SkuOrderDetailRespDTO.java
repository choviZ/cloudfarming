package com.vv.cloudfarming.order.dto.resp;

import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuOrderDetailRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 商品SKU ID
     */
    private Long skuId;

    /**
     * 商品SPU ID
     */
    private Long spuId;

    /**
     * 商品名称
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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
