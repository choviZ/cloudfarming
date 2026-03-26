package com.vv.cloudfarming.user.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ArticlePublishReqDTO implements Serializable {

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 100, message = "文章标题长度不能超过100个字符")
    private String title;

    @Size(max = 300, message = "文章摘要长度不能超过300个字符")
    private String summary;

    @Size(max = 255, message = "封面图地址长度不能超过255个字符")
    private String coverImage;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    @NotNull(message = "文章类型不能为空")
    @Min(value = 1, message = "文章类型错误")
    @Max(value = 3, message = "文章类型错误")
    private Integer articleType;

    private Boolean isTop;
}
