package com.vv.cloudfarming.starter.idempotent;

import com.vv.cloudfarming.starter.idempotent.enums.MQConsumeStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Aspect
@RequiredArgsConstructor
@Slf4j
public class NoDuplicateConsumptionAspect {

    private static final int CONSUMING_TIMEOUT_SECONDS = 600;
    private static final int CONSUMED_TIMEOUT_SECONDS = 86400;

    private static final DefaultRedisScript<Long> DELETE_IF_OWNER_SCRIPT = new DefaultRedisScript<>(
            """
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                        return redis.call('del', KEYS[1])
                    end
                    return 0
                    """,
            Long.class
    );

    private static final DefaultRedisScript<Long> MARK_CONSUMED_IF_OWNER_SCRIPT = new DefaultRedisScript<>(
            """
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                        redis.call('set', KEYS[1], ARGV[2], 'EX', ARGV[3])
                        return 1
                    end
                    return 0
                    """,
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(com.vv.cloudfarming.starter.idempotent.NoDuplicateConsumption)")
    public Object noDuplicateConsumption(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解
        NoDuplicateConsumption annotation = getNoDuplicateConsumptionAnnotation(joinPoint);
        String key = annotation.uniqueKeyPrefix() + annotation.key();
        String consumingFlag = MQConsumeStatusEnum.CONSUMING.getCode() + ":" + UUID.randomUUID();
        // 检查 Redis 中值是否存在，存在 false，不存在 true
        Boolean setIfAbsent = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, consumingFlag, CONSUMING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (setIfAbsent == null) {
            throw new RuntimeException("消息幂等标记写入失败");
        }
        if (!setIfAbsent) {
            // 正在执行，或者已经执行完成
            String status = stringRedisTemplate.opsForValue().get(key);
            boolean complete = MQConsumeStatusEnum.isComplete(status);
            log.error("消息重复消费:{},error:{}", key, complete ? "消息已消费" : "等待客户端消费");
            if (complete) {
                // 消息已经消费，返回 null，吞掉这条消息
                return null;
            } else {
                // 消息在消费中，不确定是否会消费成功，抛异常触发重试
                // 如果消费成功，状态会被更新为已消费，下次会被拦截
                throw new RuntimeException(annotation.message());
            }
        }

        Object result;
        try {
            // 执行被注解标记的方法原逻辑
            result = joinPoint.proceed();
            Long markResult = stringRedisTemplate.execute(
                    MARK_CONSUMED_IF_OWNER_SCRIPT,
                    Collections.singletonList(key),
                    consumingFlag,
                    MQConsumeStatusEnum.CONSUMED.getCode(),
                    String.valueOf(CONSUMED_TIMEOUT_SECONDS)
            );
            if (!Long.valueOf(1L).equals(markResult)) {
                log.warn("消息消费完成，但未更新已消费状态,key:{}", key);
            }
        } catch (Throwable throwable) {
            // 消费失败时删除占位，避免 key 卡在“消费中”导致后续无法重试
            stringRedisTemplate.execute(
                    DELETE_IF_OWNER_SCRIPT,
                    Collections.singletonList(key),
                    consumingFlag
            );
            throw throwable;
        }
        return result;
    }

    /**
     * @return 返回自定义防重复提交注解
     */
    public static NoDuplicateConsumption getNoDuplicateConsumptionAnnotation(ProceedingJoinPoint joinPoint)
            throws NoSuchMethodException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = joinPoint.getTarget().getClass()
                .getDeclaredMethod(methodSignature.getName(), methodSignature.getMethod().getParameterTypes());
        return targetMethod.getAnnotation(NoDuplicateConsumption.class);
    }
}
