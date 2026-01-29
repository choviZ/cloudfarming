package com.vv.cloudfarming.common.enums;

import lombok.Getter;

/**
 * 审核状态枚举
 */
@Getter
public enum ReviewStatusEnum {

    /**
     * 待审核
     */
    PENDING(0, "待审核"),

    /**
     * 已通过
     */
    APPROVED(1, "已通过"),

    /**
     * 未通过
     */
    REJECTED(2, "未通过");

    private final Integer status;

    private final String desc;

    ReviewStatusEnum(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    /**
     * 根据状态值获取对应的枚举实例
     * @param status 状态值（0/1/2）
     * @return 对应的枚举实例，若不存在则返回null
     */
    public static ReviewStatusEnum getByStatus(int status) {
        for (ReviewStatusEnum enumObj : ReviewStatusEnum.values()) {
            if (enumObj.getStatus() == status) {
                return enumObj;
            }
        }
        return null;
    }

    /**
     * 根据描述获取对应的枚举实例
     */
    public static ReviewStatusEnum getByDesc(String desc) {
        for (ReviewStatusEnum enumObj : ReviewStatusEnum.values()) {
            if (enumObj.getDesc().equals(desc)) {
                return enumObj;
            }
        }
        return null;
    }
}
