package com.vv.cloudfarming.user.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 广告表
 */
@Data
@TableName("t_advert")
public class AdvertDO extends BaseDO implements Serializable {

    /**
     * 轮播图记录的唯一标识符，自动递增
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 图片的URL地址
     */
    private String imageUrl;

    /**
     * 点击图片跳转的链接地址（可选）
     */
    private String linkUrl;

    /**
     * 图片的替代文本，用于SEO和无障碍访问（可选）
     */
    private String altText;

    /**
     * 显示顺序，决定轮播图播放顺序，数值越小优先级越高
     */
    private Integer displayOrder;

    /**
     * 开始显示日期时间（可选）
     */
    private LocalDateTime startDate;

    /**
     * 结束显示日期时间（可选）
     */
    private LocalDateTime endDate;

    /**
     * 是否激活状态
     */
    private Boolean isActive;
}