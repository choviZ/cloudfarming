package com.vv.cloudfarming.user.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.user.dao.entity.FeedbackDO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 意见反馈分页查询参数
 */
@Data
public class FeedbackPageQueryReqDTO extends Page<FeedbackDO> {

    /**
     * 反馈ID
     */
    private Long id;

    /**
     * 提交人类型：0-普通用户 1-农户
     */
    @Min(value = 0, message = "提交人类型错误")
    @Max(value = 1, message = "提交人类型错误")
    private Integer submitterType;

    /**
     * 反馈类型编码
     */
    private String feedbackType;

    /**
     * 处理状态：0-待处理 1-已处理
     */
    @Min(value = 0, message = "处理状态错误")
    @Max(value = 1, message = "处理状态错误")
    private Integer status;

    /**
     * 关键字（模糊匹配反馈内容）
     */
    private String keyword;

    /**
     * 联系电话
     */
    private String contactPhone;
}
