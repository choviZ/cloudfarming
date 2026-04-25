package com.vv.cloudfarming.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云物流查询配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "logistics.aliyun")
public class AliyunLogisticsProperties {

    /**
     * 是否启用物流查询
     */
    private Boolean enabled = Boolean.TRUE;

    /**
     * 接口基础地址
     */
    private String baseUrl = "https://wuliu.market.alicloudapi.com";

    /**
     * 查询路径
     */
    private String path = "/kdi";

    /**
     * 阿里云市场 AppCode
     */
    private String appCode;
}
