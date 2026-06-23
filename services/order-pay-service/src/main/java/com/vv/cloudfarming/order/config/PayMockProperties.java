package com.vv.cloudfarming.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 模拟支付配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "pay.mock")
public class PayMockProperties {

    private boolean enabled;

    private String result;

}
