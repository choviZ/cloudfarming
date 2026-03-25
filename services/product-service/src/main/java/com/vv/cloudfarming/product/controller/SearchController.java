package com.vv.cloudfarming.product.controller;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.product.dto.req.SearchPageReqDTO;
import com.vv.cloudfarming.product.dto.resp.SearchPageRespDTO;
import com.vv.cloudfarming.product.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "搜索控制层")
@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "全站搜索")
    @PostMapping("/api/search/v1/page")
    public Result<SearchPageRespDTO> search(@RequestBody SearchPageReqDTO requestParam) {
        return Results.success(searchService.search(requestParam));
    }
}
