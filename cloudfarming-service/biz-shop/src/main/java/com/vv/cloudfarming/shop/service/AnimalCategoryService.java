package com.vv.cloudfarming.shop.service;

import com.vv.cloudfarming.shop.dto.resp.AnimalCategoryRespDTO;

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
