package com.vv.cloudfarming.product.enums;

import com.vv.cloudfarming.common.exception.ClientException;
import lombok.Getter;

/**
 * 认养订单状态枚举
 */
@Getter
public enum AdoptOrderStatusEnum {

    /**
     * 认养中
     */
    IN_PROGRESS(1, "认养中"),

    /**
     * 已完成
     */
    COMPLETED(2, "已完成"),

    /**
     * 已取消
     */
    CANCELLED(3, "已取消");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String desc;

    AdoptOrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态码获取枚举
     */
    public static AdoptOrderStatusEnum getByCode(Integer code) {
        if (code == null) {
            throw new ClientException("认养订单状态码不能为null");
        }
        for (AdoptOrderStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new ClientException("不存在的认养订单状态码：" + code);
    }
}