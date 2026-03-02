package com.vv.cloudfarming.order.dao.algorithm;

import cn.hutool.core.collection.CollUtil;
import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;

/**
 * 订单表复合分片算法
 *
 * 路由规则：
 * 1. 优先使用 user_id 的后 6 位
 * 2. 若 SQL 中没有 user_id，则使用 order_no 的后 6 位
 * 3. 最终路由：后 6 位 % 分片数
 *
 * 这样可以保证 user_id 与 order_no（后缀设计一致时）落到同一张分表。
 */
public class OrderTableComplexAlgorithm implements ComplexKeysShardingAlgorithm {

    @Getter
    private Properties props;

    private int shardingCount;

    private static final String SHARDING_COUNT_KEY = "sharding-count";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String ORDER_NO_COLUMN = "order_no";
    private static final long TAIL_BASE = 1_000_000L;

    @Override
    public Collection<String> doSharding(Collection availableTargetNames, ComplexKeysShardingValue shardingValue) {
        Collection<String> targets = normalizeTargets(availableTargetNames);
        Map<String, Collection<Comparable<Long>>> valueMap = shardingValue.getColumnNameAndShardingValuesMap();
        if (CollUtil.isEmpty(valueMap)) {
            return targets;
        }

        Long routeValue = extractTailValue(valueMap.get(USER_ID_COLUMN));
        if (routeValue == null) {
            routeValue = extractTailValue(valueMap.get(ORDER_NO_COLUMN));
        }
        if (routeValue == null) {
            return targets;
        }

        int tableSuffix = Math.floorMod(routeValue.intValue(), shardingCount);
        String targetTable = shardingValue.getLogicTableName() + "_" + tableSuffix;
        if (targets.contains(targetTable)) {
            return Collections.singleton(targetTable);
        }
        return targets;
    }

    @Override
    public void init(Properties props) {
        this.props = props;
        shardingCount = getShardingCount(props);
    }

    private int getShardingCount(final Properties props) {
        Preconditions.checkArgument(props.containsKey(SHARDING_COUNT_KEY), "Sharding count cannot be null.");
        return Integer.parseInt(props.getProperty(SHARDING_COUNT_KEY));
    }

    private Long extractTailValue(Collection<Comparable<Long>> values) {
        if (CollUtil.isEmpty(values)) {
            return null;
        }
        Object raw = values.iterator().next();
        if (raw == null) {
            return null;
        }

        if (raw instanceof Number) {
            return Math.floorMod(((Number) raw).longValue(), TAIL_BASE);
        }

        String value = raw.toString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String tail = value.substring(Math.max(value.length() - 6, 0));
        String digits = keepDigits(tail);
        if (digits.isEmpty()) {
            return null;
        }
        return Math.floorMod(Long.parseLong(digits), TAIL_BASE);
    }

    private String keepDigits(String input) {
        StringBuilder result = new StringBuilder(input.length());
        for (char ch : input.toCharArray()) {
            if (Character.isDigit(ch)) {
                result.append(ch);
            }
        }
        return result.toString();
    }

    private Collection<String> normalizeTargets(Collection availableTargetNames) {
        Collection<String> targets = new LinkedHashSet<>(availableTargetNames.size());
        for (Object each : availableTargetNames) {
            targets.add(String.valueOf(each));
        }
        return targets;
    }
}
