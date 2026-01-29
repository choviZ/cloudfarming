package com.vv.cloudfarming.product.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车归档记录表
 */
@Data
@TableName("t_cart_archive")
public class CartArchiveDO {

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 存档时间
     */
    private LocalDateTime archiveTime;

    /**
     * 购物车内容，json数组
     */
    private String cartItems;
}