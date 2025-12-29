package com.vv.cloudfarming;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan({
        "com.vv.cloudfarming.user.dao.mapper",
        "com.vv.cloudfarming.shop.dao.mapper",
        "com.vv.cloudfarming.operation.dao.mapper",
        "com.vv.cloudfarming.order.dao.mapper",
        "com.vv.cloudfarming.cart.dao.mapper",
})
@EnableScheduling
public class CloudFarmingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudFarmingApplication.class, args);
    }
}
