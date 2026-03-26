package com.vv.cloudfarming.user.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_article")
public class ArticleDO extends BaseDO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String summary;

    private String coverImage;

    private String content;

    private Integer articleType;

    private Integer status;

    private Boolean isTop;

    private Long authorId;

    private LocalDateTime publishTime;
}
