package com.vv.cloudfarming.product.service.impl;

import cn.hutool.core.lang.Singleton;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.product.constant.StockKeyConstant;
import com.vv.cloudfarming.product.enums.ProductTypeEnum;
import com.vv.cloudfarming.product.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String STOCK_INIT_LUA_PATH = "lua/stock_init.lua";

    @Override
    public Long initStock(Long id, int totalStock, int bizType) {
        // 获取 LUA 脚本，并保存到 Hutool 的单例管理容器，下次直接获取不需要加载
        DefaultRedisScript<Long> buildLuaScript = Singleton.get(STOCK_INIT_LUA_PATH, () -> {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(STOCK_INIT_LUA_PATH)));
            redisScript.setResultType(Long.class);
            return redisScript;
        });
        ProductTypeEnum type = ProductTypeEnum.of(bizType);
        if (type == null) {
            throw new ClientException("不支持的业务类型");
        }
        String availableKey = String.format(StockKeyConstant.STOCK_AVAILABLE_CACHE_KEY, type.getName(), id);
        String lockKey = String.format(StockKeyConstant.STOCK_LOCK_CACHE_KEY, type.getName(), id);
        Long stockInitResult = stringRedisTemplate.execute(
                buildLuaScript,
                Arrays.asList(availableKey, lockKey),
                String.valueOf(totalStock)
        );
        if (stockInitResult == -1){
            throw new ClientException("参数非法");
        } else if (stockInitResult == 1) {
            log.info("库存初始化成功，availableKey：{}，lockKey：{}", availableKey, lockKey);
        }else if (stockInitResult == 0){
            log.info("库存初始化已存在，availableKey：{}，lockKey：{}", availableKey, lockKey);
        }
        return stockInitResult;
    }

    @Override
    public void lock(Long id, int quantity, int bizType) {
        if (Objects.isNull(id) || quantity <= 0) {
            throw new ClientException("商品ID或数量参数非法");
        }
        ProductTypeEnum type = ProductTypeEnum.of(bizType);
        if (type == null) {
            throw new ClientException("不支持的业务类型");
        }
        String availableKey = String.format(StockKeyConstant.STOCK_AVAILABLE_CACHE_KEY, type.getName(), id);
        String lockKey = String.format(StockKeyConstant.STOCK_LOCK_CACHE_KEY, type.getName(), id);

        String lockScript =
                "local availableKey = KEYS[1] " +
                        "local lockKey = KEYS[2] " +
                        "local quantity = tonumber(ARGV[1]) " +
                        "if redis.call('exists', availableKey) == 0 then " +
                        "    return -2 " +
                        "end " +
                        "local available = tonumber(redis.call('get', availableKey)) " +
                        "if available < quantity then " +
                        "    return -1 " +
                        "end " +
                        "redis.call('decrby', availableKey, quantity) " +
                        "redis.call('incrby', lockKey, quantity) " +
                        "return 1";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(lockScript, Long.class);
        Long result = stringRedisTemplate.execute(
                redisScript,
                Arrays.asList(availableKey, lockKey),
                String.valueOf(quantity)
        );
        if (Objects.isNull(result)) {
            throw new ClientException("锁定库存失败，Redis执行异常");
        }
        if (result == -1) {
            throw new ClientException("商品可售库存不足");
        }
        if (result == -2) {
            throw new ServiceException("商品库存缓存不存在，请重试");
        }
    }

    @Override
    public void unlock(Long id, int quantity, int bizType) {
        if (Objects.isNull(id) || quantity <= 0) {
            throw new ClientException("商品ID或数量参数非法");
        }
        ProductTypeEnum type = ProductTypeEnum.of(bizType);
        if (type == null) {
            throw new ClientException("不支持的业务类型");
        }
        String availableKey = String.format(StockKeyConstant.STOCK_AVAILABLE_CACHE_KEY, type.getName(), id);
        String lockKey = String.format(StockKeyConstant.STOCK_LOCK_CACHE_KEY, type.getName(), id);

        String unlockScript =
                "local availableKey = KEYS[1] " +
                        "local lockKey = KEYS[2] " +
                        "local quantity = tonumber(ARGV[1]) " +
                        "if redis.call('exists', lockKey) == 0 then " +
                        "    return -2 " +
                        "end " +
                        "local locked = tonumber(redis.call('get', lockKey)) " +
                        "if locked < quantity then " +
                        "    return -1 " +
                        "end " +
                        "redis.call('decrby', lockKey, quantity) " +
                        "redis.call('incrby', availableKey, quantity) " +
                        "return 1";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(unlockScript, Long.class);
        Long result = stringRedisTemplate.execute(
                redisScript,
                Arrays.asList(availableKey, lockKey),
                String.valueOf(quantity)
        );
        if (result == null) {
            throw new ClientException("释放库存失败，Redis执行异常");
        }
        if (result == -1) {
            throw new ClientException("要释放的库存数量超过已锁定的数量");
        }
        if (result == -2) {
            log.error("锁定库存记录不存在，key：{}", lockKey);
            throw new ServiceException("锁定库存记录不存在");
        }
    }
}