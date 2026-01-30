package com.vv.cloudfarming.order.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * redis生成分布式id
 */
@Component
@RequiredArgsConstructor
public class RedisIdWorker {

    private static final Long EPOCH = 1609459200L;
    private static final Integer SEQUENCE_BITS = 32;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 生成一个id
     */
    public Long generateId(String bizKeyPrefix) {
        // 时间戳
        LocalDateTime now = LocalDateTime.now();

        long timestamp = now.toEpochSecond(ZoneOffset.UTC) - EPOCH;
        // 序列号
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + bizKeyPrefix + date);
        // 拼接
        return timestamp << SEQUENCE_BITS | count;
    }
}
