package com.vv.cloudfarming.product.enums;

import lombok.Getter;

/**
 * 审核状态枚举
 */
@Getter
public enum AuditStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "通过"),
    REJECTED(2, "拒绝");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String desc;

    AuditStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态码获取枚举
     */
    public static AuditStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (AuditStatusEnum statusEnum : AuditStatusEnum.values()) {
            if (statusEnum.code.equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    /**
     * 判断是否待审核
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * 判断是否通过
     */
    public boolean isApproved() {
        return this == APPROVED;
    }

    /**
     * 判断是否拒绝
     */
    public boolean isRejected() {
        return this == REJECTED;
    }
}
