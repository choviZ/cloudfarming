package com.vv.cloudfarming.order.enums;

import com.vv.cloudfarming.common.exception.ClientException;
import lombok.Getter;

/**
 * 支付状态枚举类
 * 状态：0-未支付 1-已支付 2-退款中 3-部分退款 4-全部退款 5-支付失败
 */
@Getter
public enum PayStatusEnum {

    /**
     * 未支付
     */
    UNPAID(0, "未支付"),

    /**
     * 已支付
     */
    PAID(1, "已支付"),

    /**
     * 退款中
     */
    REFUNDING(2, "退款中"),

    /**
     * 部分退款
     */
    PART_REFUNDED(3, "部分退款"),

    /**
     * 全部退款
     */
    FULL_REFUNDED(4, "全部退款"),

    /**
     * 支付失败
     */
    PAY_FAILED(5, "支付失败");

    /**
     * 状态码（对应数据库中的payStatus值）
     */
    private final Integer code;

    /**
     * 状态描述（用于前端展示或日志说明）
     */
    private final String desc;

    /**
     * 枚举构造方法
     */
    PayStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 工具方法：根据状态码获取对应的枚举实例（业务中高频使用）
     *
     * @param code 支付状态码
     * @return 对应的PayStatusEnum实例
     * @throws IllegalArgumentException 当code不存在时抛出异常，提示非法参数
     */
    public static PayStatusEnum getByCode(Integer code) {
        // 判空处理，避免空指针
        if (code == null) {
            throw new ClientException("支付状态码不能为null");
        }
        // 遍历所有枚举实例，匹配状态码
        for (PayStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        // 没有匹配的枚举时，抛出异常，快速发现非法数据
        throw new ClientException("不存在的支付状态码：" + code);
    }

    /**
     * 重载方法：支持int类型的code（避免手动装箱）
     *
     * @param code 支付状态码（int类型）
     * @return 对应的PayStatusEnum实例
     */
    public static PayStatusEnum getByCode(int code) {
        return getByCode(Integer.valueOf(code));
    }
}