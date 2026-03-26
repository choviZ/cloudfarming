package com.vv.cloudfarming.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleTypeEnum {

    NOTICE(1, "平台公告"),
    POLICY(2, "农业政策"),
    KNOWLEDGE(3, "养殖知识");

    private final Integer code;
    private final String desc;

    public static ArticleTypeEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ArticleTypeEnum each : values()) {
            if (each.code.equals(code)) {
                return each;
            }
        }
        return null;
    }
}
