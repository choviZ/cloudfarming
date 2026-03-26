package com.vv.cloudfarming.user.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.user.dao.entity.ArticleDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticlePageQueryReqDTO extends Page<ArticleDO> {

    private Long id;

    private String title;

    private Integer articleType;

    private Integer status;

    private Boolean isTop;
}
