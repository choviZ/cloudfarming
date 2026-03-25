package com.vv.cloudfarming.product.service;

import com.vv.cloudfarming.product.dto.req.SearchPageReqDTO;
import com.vv.cloudfarming.product.dto.resp.SearchPageRespDTO;

public interface SearchService {

    /**
     * 全站搜索
     *
     * @param requestParam 搜索参数
     * @return 搜索结果
     */
    SearchPageRespDTO search(SearchPageReqDTO requestParam);
}
