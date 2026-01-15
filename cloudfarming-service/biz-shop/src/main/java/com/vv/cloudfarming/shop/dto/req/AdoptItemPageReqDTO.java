package com.vv.cloudfarming.shop.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.shop.dao.entity.AdoptItemDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分页查询认养项目请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdoptItemPageReqDTO extends Page<AdoptItemDO> {

    /**
     * 动物分类（代码）
     */
    private String animalCategory;

    /**
     * 标题（模糊查询）
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
     * 用户ID（查询我发布的认养项目）
     */
    private Long userId;
}
