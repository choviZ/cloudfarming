package com.vv.cloudfarming.user.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 意见反馈响应参数
 */
@Data
public class FeedbackRespDTO implements Serializable {

    /**
     * 反馈ID
     */
    private Long id;

    /**
     * 提交用户ID
     */
    private Long userId;

    /**
     * 提交用户名
     */
    private String username;

    /**
     * 提交人类型：0-普通用户 1-农户
     */
    private Integer submitterType;

    /**
     * 提交人类型名称
     */
    private String submitterTypeName;

    /**
     * 反馈类型编码
     */
    private String feedbackType;

    /**
     * 反馈类型名称
     */
    private String feedbackTypeName;

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
     * 处理状态名称
     */
    private String statusName;

    /**
     * 处理回复
     */
    private String replyContent;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理人用户名
     */
    private String handlerName;

    /**
     * 处理时间
     */
    private Date handleTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
