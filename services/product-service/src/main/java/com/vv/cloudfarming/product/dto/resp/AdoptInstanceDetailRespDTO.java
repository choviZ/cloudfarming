package com.vv.cloudfarming.product.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 养殖实例详情响应
 */
@Data
@Builder
public class AdoptInstanceDetailRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实例ID
     */
    private Integer id;

    /**
     * 认养项目ID
     */
    private Long itemId;

    /**
     * 认养项目名称
     */
    private String itemTitle;

    /**
     * 认养项目封面
     */
    private String itemCoverImage;

    /**
     * 耳标号
     */
    private Long earTagNo;

    /**
     * 实例图片
     */
    private String image;

    /**
     * 实例状态
     */
    private Integer status;

    /**
     * 实例状态描述
     */
    private String statusDesc;

    /**
     * 关联订单ID
     */
    private Long ownerOrderId;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 最新日记时间
     */
    private Date latestLogTime;

    /**
     * 死亡时间
     */
    private Date deathTime;

    /**
     * 死亡原因
     */
    private String deathReason;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
