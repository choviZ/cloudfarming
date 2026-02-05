package com.vv.cloudfarming.product.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.Data;

import java.io.Serializable;

/**
 * 饲养日志实体类
 */
@Data
@TableName("t_adopt_log")
public class AdoptLogDO extends BaseDO implements Serializable {

    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 认养实例id
     */
    private Long instanceId;

    /**
     * 日志类型: 1-喂食 2-体重记录 3-防疫 4-日常记录 5-异常
     */
    private Integer logType;

    /**
     * 文字描述
     */
    private String content;

    /**
     * 图片地址，多个用逗号分隔
     */
    private String imageUrls;

    /**
     * 视频地址
     */
    private String videoUrl;

    /**
     * 本次记录体重（kg）
     */
    private Double weight;

    /**
     * 操作人id（农户）
     */
    private Long operatorId;
}