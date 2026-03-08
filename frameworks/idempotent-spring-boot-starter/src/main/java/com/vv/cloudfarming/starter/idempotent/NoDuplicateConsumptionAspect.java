package com.vv.cloudfarming.starter.idempotent;

import com.vv.cloudfarming.starter.idempotent.enums.MQConsumeStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

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
    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

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
        NoDuplicateConsumption annotation = getNoDuplicateConsumptionAnnotation(joinPoint);
        String key = annotation.uniqueKeyPrefix() + resolveKey(annotation.key(), joinPoint);
        String consumingFlag = MQConsumeStatusEnum.CONSUMING.getCode() + ":" + UUID.randomUUID();

        Boolean setIfAbsent = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, consumingFlag, CONSUMING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (setIfAbsent == null) {
            throw new RuntimeException("\u6d88\u606f\u5e42\u7b49\u6807\u8bb0\u5199\u5165\u5931\u8d25");
        }
        if (!setIfAbsent) {
            String status = stringRedisTemplate.opsForValue().get(key);
            boolean complete = MQConsumeStatusEnum.isComplete(status);
            log.error("\u6d88\u606f\u91cd\u590d\u6d88\u8d39\uff1a{}, \u72b6\u6001\uff1a{}", key, complete ? "\u5df2\u6d88\u8d39" : "\u6d88\u8d39\u4e2d");
            if (complete) {
                return null;
            }
            throw new RuntimeException(annotation.message());
        }

        Object result;
        try {
            result = joinPoint.proceed();
            Long markResult = stringRedisTemplate.execute(
                    MARK_CONSUMED_IF_OWNER_SCRIPT,
                    Collections.singletonList(key),
                    consumingFlag,
                    MQConsumeStatusEnum.CONSUMED.getCode(),
                    String.valueOf(CONSUMED_TIMEOUT_SECONDS)
            );
            if (!Long.valueOf(1L).equals(markResult)) {
                log.warn("\u6d88\u606f\u6d88\u8d39\u5b8c\u6210\uff0c\u4f46\u672a\u66f4\u65b0\u4e3a\u5df2\u6d88\u8d39\u72b6\u6001\uff0ckey={}", key);
            }
        } catch (Throwable throwable) {
            stringRedisTemplate.execute(
                    DELETE_IF_OWNER_SCRIPT,
                    Collections.singletonList(key),
                    consumingFlag
            );
            throw throwable;
        }
        return result;
    }

    private String resolveKey(String keyExpression, ProceedingJoinPoint joinPoint) {
        if (!StringUtils.hasText(keyExpression)) {
            throw new RuntimeException("\u6d88\u606f\u5e42\u7b49 key \u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (!keyExpression.contains("#")) {
            return keyExpression;
        }

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod;
        try {
            targetMethod = joinPoint.getTarget().getClass()
                    .getDeclaredMethod(methodSignature.getName(), methodSignature.getMethod().getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("\u89e3\u6790\u6d88\u606f\u5e42\u7b49 key \u5931\u8d25", e);
        }

        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(
                joinPoint.getTarget(),
                targetMethod,
                joinPoint.getArgs(),
                PARAMETER_NAME_DISCOVERER
        );

        Object value;
        try {
            value = SPEL_PARSER.parseExpression(keyExpression).getValue(evaluationContext);
        } catch (Exception ex) {
            throw new RuntimeException("\u89e3\u6790\u6d88\u606f\u5e42\u7b49 key \u5931\u8d25\uff0c\u8868\u8fbe\u5f0f\uff1a" + keyExpression, ex);
        }

        if (value == null || !StringUtils.hasText(value.toString())) {
            throw new RuntimeException("\u89e3\u6790\u6d88\u606f\u5e42\u7b49 key \u4e3a\u7a7a\uff0c\u8868\u8fbe\u5f0f\uff1a" + keyExpression);
        }
        return value.toString();
    }

    public static NoDuplicateConsumption getNoDuplicateConsumptionAnnotation(ProceedingJoinPoint joinPoint)
            throws NoSuchMethodException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = joinPoint.getTarget().getClass()
                .getDeclaredMethod(methodSignature.getName(), methodSignature.getMethod().getParameterTypes());
        return targetMethod.getAnnotation(NoDuplicateConsumption.class);
    }
}
