package com.vv.cloudfarming.product.dto.resp;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class SearchPageRespDTO {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 当前类型：0-认养项目，1-农产品，null-全部
     */
    private Integer productType;

    /**
     * 当前页
     */
    private Long current;

    /**
     * 每页条数
     */
    private Long size;

    /**
     * 当前类型总数
     */
    private Long total = 0L;

    /**
     * 当前类型结果
     */
    private List<SearchItemRespDTO> records = Collections.emptyList();

    /**
     * 农产品总数
     */
    private Long spuTotal = 0L;

    /**
     * 农产品预览结果
     */
    private List<SearchItemRespDTO> spuRecords = Collections.emptyList();

    /**
     * 认养项目总数
     */
    private Long adoptTotal = 0L;

    /**
     * 认养项目预览结果
     */
    private List<SearchItemRespDTO> adoptRecords = Collections.emptyList();
}
