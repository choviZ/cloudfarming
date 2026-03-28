package com.vv.cloudfarming.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 提交意见反馈请求参数
 */
@Data
public class FeedbackSubmitReqDTO implements Serializable {

    /**
     * 反馈类型编码
     */
    @NotBlank(message = "问题类型不能为空")
    private String feedbackType;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;

    /**
     * 反馈内容
     */
    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 1000, message = "反馈内容不能超过1000字")
    private String content;
}
