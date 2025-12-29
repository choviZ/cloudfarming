package com.vv.cloudfarming.cart.task;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.cloudfarming.cart.dao.mapper.CartArchiveMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * 清理购物车定时任务
 */
@Component
@Slf4j
public class ClearCartTask {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CartArchiveMapper cartArchiveMapper;
    private static Long archiveTime = 60 * 60 * 24L;
    private static final String CART_KEY_PREFIX = "cloudfarming:cart:user:";

    @Scheduled(cron = "0 0 2 * * ?")
    public void clear() {
        // 获取所有key
        Set<String> keys = stringRedisTemplate.keys(CART_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            log.info("未找到匹配的 key");
            return;
        }
        keys.forEach(k -> {
            try {
                log.info("处理key：{}", k);
                // 检查ttl
                Long expire = stringRedisTemplate.getExpire(k);
                if (expire <= archiveTime) {
                    // ttl 不足24小时
                    ArrayList<String> list = new ArrayList<>();
                    Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(k);
                    entries.forEach((field, value) -> {
                        list.add(value.toString());
                    });
                    String[] split = k.split(":");
                    String userId = split[split.length - 1];
                    String jsonValue = JSONUtil.toJsonStr(list);
                    log.info("归档：userId:{},value:{}", userId, jsonValue);
                    int saved = cartArchiveMapper.cartArchive(Long.parseLong(userId), jsonValue.toString());
                    if (!SqlHelper.retBool(saved)) {
                        log.error("购物车归档失败，redis key：{}，value：{}", k, jsonValue);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
    }
}
