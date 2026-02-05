package com.vv.cloudfarming.product.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 认养牲畜表 DO
 */
@Data
@Builder
@TableName("t_adopt_instance")
public class AdoptInstanceDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 认养项目id
     */
    private Long itemId;

    /**
     * 耳标号 / 动物的唯一标识
     */
    private Long earTagNo;

    /**
     * 图片
     */
    private String image;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 农户id
     */
    private Long farmerId;

    /**
     * 绑定用户id
     */
    private Long ownerUserId;

    /**
     * 绑定订单id
     */
    private Long ownerOrderId;

    /**
     * 死亡时间
     */
    private Date deathTime;

    /**
     * 死亡原因
     */
    private String deathReason;
}
