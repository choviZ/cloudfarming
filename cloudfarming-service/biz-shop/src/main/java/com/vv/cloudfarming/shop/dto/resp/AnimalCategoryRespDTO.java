package com.vv.cloudfarming.shop.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 动物分类响应数据
 */
@Data
@AllArgsConstructor
public class AnimalCategoryRespDTO {

    /**
     * 分类代码（用于存库、传参）
     */
    private String code;

    /**
     * 分类名称（用于展示）
     */
    private String name;
}
