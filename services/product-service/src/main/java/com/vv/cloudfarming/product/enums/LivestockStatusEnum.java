package com.vv.cloudfarming.product.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 牲畜认养状态枚举
 */
@Getter
public enum LivestockStatusEnum {

    /**
     * 可认养
     */
    AVAILABLE(0, "可认养"),

    /**
     * 已认养
     */
    ADOPTED(1, "已认养"),

    /**
     * 已履约完成
     */
    FULFILLED(2, "已履约完成");

    /**
     * 存储到数据库的 code 值
     *
     * @EnumValue: 标记 MyBatis-Plus 存库时使用该字段
     * @JsonValue: 标记 Jackson 序列化返回给前端时使用该字段 (如果只想返回 code)
     */
    @EnumValue
    private final Integer code;

    /**
     * 状态描述
     */
    private final String desc;

    LivestockStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举对象
     */
    public static LivestockStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LivestockStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}
