package com.vv.cloudfarming.shop.dao.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.cloudfarming.common.database.BaseDO;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 店铺表实体
 */
@Data
@Builder
@TableName("t_shops")
public class Shop extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 农户（用户）ID
     */
    private Long farmerId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺头像
     */
    private String shopAvatar;

    /**
     * 店铺横幅
     */
    private String shopBanner;

    /**
     * 店铺描述
     */
    private String description;

    /**
     * 店铺公告
     */
    private String announcement;

    /**
     * 状态: 1-正常, 2-禁用
     */
    private Integer status;
}