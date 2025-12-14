package com.vv.cloudfarming.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    /**
     * 普通用户
     */
    ORDINARY(0, "普通用户"),

    /**
     * 农户
     */
    FARMER(1, "农户"),

    /**
     * 管理员
     */
    ADMIN(2, "管理员");

    /**
     * 角色编码
     */
    private final Integer code;

    /**
     * 角色描述
     */
    private final String description;

    /**
     * 根据编码获取枚举
     *
     * @param code 角色编码
     * @return 对应的枚举，如果找不到则返回 null
     */
    public static UserRoleEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserRoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }

    /**
     * 判断是否为管理员
     *
     * @return true 如果是管理员
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * 判断是否为农户
     *
     * @return true 如果是农户
     */
    public boolean isFarmer() {
        return this == FARMER;
    }
}
