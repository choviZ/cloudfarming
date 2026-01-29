package com.vv.cloudfarming.product.dto.resp;

import com.vv.cloudfarming.product.dto.domain.SpuPriceSummaryDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SpuRespDTO {

    /**
     * SPU ID
     */
    private Long id;

    /**
     * 价格摘要
     */
    private SpuPriceSummaryDTO priceSummary;

    /**
     * 标准商品名称
     */
    private String title;

    /**
     * 商品分类id
     */
    private Long categoryId;

    /**
     * 商品图片URL，多个图片用逗号分隔
     */
    private String images;

    /**
     * 商品状态：0-下架，1-上架，2-待审核
     */
    private Integer status;

    /**
     * 基本属性
     */
    private Map<String,String> attributes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
