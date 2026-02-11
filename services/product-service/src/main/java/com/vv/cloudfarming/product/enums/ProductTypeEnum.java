package com.vv.cloudfarming.product.enums;

import lombok.Getter;

/**
 * 商品类型
 */
@Getter
public enum ProductTypeEnum {

    ADOPT(0,"adopt","云养殖项目"),
    SPU(1,"sku","农产品");

    private Integer code;
    private String name;
    private String desc;

    ProductTypeEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static ProductTypeEnum of(Integer code) {
        for (ProductTypeEnum productTypeEnum : ProductTypeEnum.values()) {
            if (productTypeEnum.code.equals(code)) {
                return productTypeEnum;
            }
        }
        return null;
    }
}
