package com.vv.cloudfarming.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleStatusEnum {

    OFFLINE(0, "已下线"),
    PUBLISHED(1, "已发布");

    private final Integer code;
    private final String desc;

    public static ArticleStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ArticleStatusEnum each : values()) {
            if (each.code.equals(code)) {
                return each;
            }
        }
        return null;
    }
}
