package com.vv.cloudfarming.framework.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 腾讯云对象存储配置属性
 */
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosProperties {

    /**
     * accessKey
     */
    private String secretId;

    /**
     * secretKey
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 桶名
     */
    private String bucket;

    /**
     * host
     */
    private String host;
}
