package com.vv.cloudfarming.product.service.impl;

import com.vv.cloudfarming.product.dto.resp.AnimalCategoryRespDTO;
import com.vv.cloudfarming.product.enums.AnimalCategoryEnum;
import com.vv.cloudfarming.product.service.AnimalCategoryService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 动物分类服务实现层
 */
@Service
public class AnimalCategoryServiceImpl implements AnimalCategoryService {

    @Override
    public List<AnimalCategoryRespDTO> listAnimalCategories() {
        return Arrays.stream(AnimalCategoryEnum.values())
                .map(e -> new AnimalCategoryRespDTO(e.getCode(), e.getName()))
                .collect(Collectors.toList());
    }
}
