package com.vv.cloudfarming.common.enums;

import lombok.Getter;

/**
 * 上下架状态枚举
 */
@Getter
public enum ShelfStatusEnum {

    ONLINE(0,"上架"),
    OFFLINE(1,"下架");

    private final Integer code;
    private final String desc;

    ShelfStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
