package com.vv.cloudfarming.common.enums;

import lombok.Getter;

/**
 * 上下架状态枚举
 */
@Getter
public enum ShelfStatusEnum {

    OFFLINE(0, "下架"),
    ONLINE(1, "上架");

    private final Integer code;
    private final String desc;

    ShelfStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
