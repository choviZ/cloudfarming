package com.vv.cloudfarming.aggregation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 云养殖平台聚合服务应用启动器
 */
@SpringBootApplication(scanBasePackages = {
        "com.vv.cloudfarming.user",
        "com.vv.cloudfarming.product",
        "com.vv.cloudfarming.order",
        "com.vv.cloudfarming.aggregation"
})
@MapperScan({
        "com.vv.cloudfarming.user.dao.mapper",
        "com.vv.cloudfarming.product.dao.mapper",
        "com.vv.cloudfarming.order.dao.mapper",
})
@EnableDiscoveryClient
@EnableScheduling
public class AggregationApplication {
    public static void main(String[] args) {
        SpringApplication.run(AggregationApplication.class, args);
    }
}
