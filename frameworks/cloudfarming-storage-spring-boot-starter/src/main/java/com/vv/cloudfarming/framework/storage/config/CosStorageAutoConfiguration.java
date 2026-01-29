package com.vv.cloudfarming.framework.storage.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import com.vv.cloudfarming.framework.storage.core.CosManager;
import com.vv.cloudfarming.framework.storage.core.FileUploadService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云对象存储自动配置类
 */
@Configuration
@EnableConfigurationProperties(CosProperties.class)
public class CosStorageAutoConfiguration {

    private final CosProperties cosProperties;

    public CosStorageAutoConfiguration(CosProperties cosProperties) {
        this.cosProperties = cosProperties;
    }

    /**
     * 创建COSClient实例
     * @return COSClient
     */
    @Bean
    @ConditionalOnMissingBean
    public COSClient cosClient() {
        // 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(cosProperties.getSecretId(), cosProperties.getSecretKey());
        // 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(cosProperties.getRegion()));
        // 生成cos客户端
        return new COSClient(cred, clientConfig);
    }

    /**
     * 创建CosManager实例
     * @param cosClient COSClient实例
     * @return CosManager
     */
    @Bean
    @ConditionalOnMissingBean
    public CosManager cosManager(COSClient cosClient) {
        CosManager cosManager = new CosManager();
        // 设置COSClient
        cosManager.setCosClient(cosClient);
        // 设置配置信息
        cosManager.setCosClientConfig(cosProperties);
        return cosManager;
    }

    /**
     * 封装后的上传文件业务对象
     */
    @Bean
    public FileUploadService fileUploadService(CosManager cosManager) {
        return new FileUploadService(cosManager,cosProperties);
    }
}
