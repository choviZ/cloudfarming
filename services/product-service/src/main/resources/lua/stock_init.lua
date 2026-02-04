-- 库存缓存初始化脚本
-- KEYS[1]: availableKey
-- KEYS[2]: lockKey
-- ARGV[1]: totalStock 数据库总库存
--
-- 返回:
--   1  : 成功
--   0  : 无需初始化（任一key已存在，未执行任何操作）
--  -1  : 参数非法

local total = tonumber(ARGV[1])

-- 1. 参数基础校验
if not total or total < 0 then
    return -1
end

-- 2. 避免重复写入
if redis.call('EXISTS', KEYS[1]) == 0 and redis.call('EXISTS', KEYS[2]) == 0 then
   redis.call('SET', KEYS[1], total)
   redis.call('SET', KEYS[2], 0)
   return 1
else
    return 0
end

