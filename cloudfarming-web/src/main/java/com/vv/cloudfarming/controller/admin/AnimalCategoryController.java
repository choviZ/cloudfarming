package com.vv.cloudfarming.controller.admin;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.shop.dto.resp.AnimalCategoryRespDTO;
import com.vv.cloudfarming.shop.service.AnimalCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/adopt/animal-category")
@Tag(name = "动物分类接口")
public class AnimalCategoryController {

    private final AnimalCategoryService animalCategoryService;

    public AnimalCategoryController(AnimalCategoryService animalCategoryService) {
        this.animalCategoryService = animalCategoryService;
    }

    /**
     * 查询动物分类列表
     */
    @Operation(summary = "查询动物分类列表")
    @GetMapping("/list")
    public Result<List<AnimalCategoryRespDTO>> listAnimalCategories() {
        return Results.success(animalCategoryService.listAnimalCategories());
    }
}
