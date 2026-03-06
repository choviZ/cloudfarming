package com.vv.cloudfarming.starter.idempotent.config;

import com.vv.cloudfarming.starter.idempotent.NoDuplicateConsumptionAspect;
import com.vv.cloudfarming.starter.idempotent.NoDuplicateSubmitAspect;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

@AutoConfiguration
public class IdempotentConfiguration {

    @Bean
    @ConditionalOnBean(RedissonClient.class)
    public NoDuplicateSubmitAspect noDuplicateSubmitAspect(RedissonClient redissonClient) {
        return new NoDuplicateSubmitAspect(redissonClient);
    }

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public NoDuplicateConsumptionAspect noDuplicateConsumptionAspect(StringRedisTemplate stringRedisTemplate) {
        return new NoDuplicateConsumptionAspect(stringRedisTemplate);
    }
}
