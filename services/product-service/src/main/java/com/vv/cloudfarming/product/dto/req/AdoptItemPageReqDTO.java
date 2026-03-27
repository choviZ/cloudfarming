package com.vv.cloudfarming.product.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.product.dao.entity.AdoptItemDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分页查询认养项目请求 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdoptItemPageReqDTO extends Page<AdoptItemDO> {

    /**
     * 动物分类
     */
    private String animalCategory;

    /**
     * 标题关键词
     */
    private String title;

    /**
     * 审核状态
     */
    private Integer reviewStatus;

    /**
     * 上架状态
     */
    private Integer status;

    /**
     * 当前用户 ID，用于查询我的发布
     */
    private Long userId;

    /**
     * 店铺 ID，用于公开店铺首页聚合认养项目
     */
    private Long shopId;
}
