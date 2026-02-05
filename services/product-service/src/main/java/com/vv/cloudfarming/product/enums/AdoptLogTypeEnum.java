package com.vv.cloudfarming.product.enums;

import com.vv.cloudfarming.common.exception.ClientException;
import lombok.Getter;


/**
 * 日志类型枚举
 */
@Getter
public enum AdoptLogTypeEnum {

    FEED(1, "喂食"),

    WEIGHT_RECORD(2, "体重记录"),

    EPIDEMIC_PREVENTION(3, "防疫"),

    DAILY_RECORD(4, "日常记录"),

    ABNORMAL_EVENT(5, "异常事件");

    private final Integer code;
    private final String desc;

    AdoptLogTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AdoptLogTypeEnum getByCode(Integer code) {
        if (code == null) {
            throw new ClientException("日志类型不能为空");
        }
        for (AdoptLogTypeEnum e : AdoptLogTypeEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
