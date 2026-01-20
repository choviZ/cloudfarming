package com.vv.cloudfarming.shop.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.vv.cloudfarming.common.database.BaseDO;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 认养牲畜表 DO
 */
@Data
@Builder
@TableName("t_livestock")
public class LivestockDO extends BaseDO implements Serializable {

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
     * 生日
     */
    private Date birthDate;

    /**
     * 可认养 / 已认养 / 已履约完成
     */
    private Integer status;

    /**
     * 绑定订单id
     */
    private Long ownerOrderId;
}
