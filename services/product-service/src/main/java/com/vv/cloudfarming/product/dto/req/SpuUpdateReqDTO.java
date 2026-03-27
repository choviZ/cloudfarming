package com.vv.cloudfarming.product.dto.req;

import lombok.Data;

@Data
public class SpuUpdateReqDTO {

    private Long id;

    private String title;

    private Long categoryId;

    private String images;
}
