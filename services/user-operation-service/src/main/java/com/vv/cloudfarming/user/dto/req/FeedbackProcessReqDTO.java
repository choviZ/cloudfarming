package com.vv.cloudfarming.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 处理意见反馈请求参数
 */
@Data
public class FeedbackProcessReqDTO implements Serializable {

    /**
     * 反馈ID
     */
    @NotNull(message = "反馈ID不能为空")
    private Long id;

    /**
     * 处理回复
     */
    @NotBlank(message = "处理回复不能为空")
    @Size(max = 500, message = "处理回复不能超过500字")
    private String replyContent;
}
