package com.vv.cloudfarming.user.dto.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ArticleRespDTO {

    private Long id;

    private String title;

    private String summary;

    private String coverImage;

    private String content;

    private Integer articleType;

    private String articleTypeDesc;

    private Integer status;

    private String statusDesc;

    private Boolean isTop;

    private Long authorId;

    private LocalDateTime publishTime;

    private Date createTime;

    private Date updateTime;
}
