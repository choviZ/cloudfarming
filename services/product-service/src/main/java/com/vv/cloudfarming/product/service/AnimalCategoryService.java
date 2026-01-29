package com.vv.cloudfarming.product.service;

import com.vv.cloudfarming.product.dto.resp.AnimalCategoryRespDTO;

import java.util.List;

/**
 * 动物分类服务接口层
 */
public interface AnimalCategoryService {

    /**
     * 查询动物分类列表
     */
    List<AnimalCategoryRespDTO> listAnimalCategories();
}
