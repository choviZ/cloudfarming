package com.vv.cloudfarming.starter.idempotent.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * MQ 消费状态枚举
 */
@RequiredArgsConstructor
public enum MQConsumeStatusEnum {
    
    /**
     * 消费中
     */
    CONSUMING("0"),
    
    /**
     * 已消费
     */
    CONSUMED("1");
    
    @Getter
    private final String code;
    
    /**
     * 如果消费状态等于消费中，返回失败
     *
     * @param consumeStatus 消费状态
     * @return 是否完成消费
     */
    public static boolean isComplete(String consumeStatus) {
        return Objects.equals(CONSUMED.code, consumeStatus);
    }
}
