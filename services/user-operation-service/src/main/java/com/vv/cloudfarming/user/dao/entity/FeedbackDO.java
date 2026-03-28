package com.vv.cloudfarming.user.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 意见反馈实体
 */
@Data
@TableName("t_feedback")
public class FeedbackDO extends BaseDO implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 提交用户ID
     */
    private Long userId;

    /**
     * 提交人类型：0-普通用户 1-农户
     */
    private Integer submitterType;

    /**
     * 反馈类型编码
     */
    private String feedbackType;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 处理状态：0-待处理 1-已处理
     */
    private Integer status;

    /**
     * 处理回复
     */
    private String replyContent;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理时间
     */
    private Date handleTime;
}
