package com.vv.cloudfarming;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({
        "com.vv.cloudfarming.user.dao.mapper",
        "com.vv.cloudfarming.shop.dao.mapper",
        "com.vv.cloudfarming.operation.dao.mapper",
        "com.vv.cloudfarming.order.dao.mapper"
})
public class CloudFarmingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudFarmingApplication.class, args);
    }
}
