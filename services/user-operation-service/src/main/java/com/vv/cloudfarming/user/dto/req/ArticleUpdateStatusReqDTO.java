package com.vv.cloudfarming.user.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class ArticleUpdateStatusReqDTO implements Serializable {

    @NotNull(message = "文章ID不能为空")
    private Long id;

    @NotNull(message = "文章状态不能为空")
    @Min(value = 0, message = "文章状态错误")
    @Max(value = 1, message = "文章状态错误")
    private Integer status;
}
