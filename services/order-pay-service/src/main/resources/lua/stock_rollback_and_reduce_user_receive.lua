-- KEYS[1]: 秒杀活动缓存 key（Hash）
-- KEYS[2]: 用户购买计数 key（Hash）
-- ARGV[1]: 回滚数量
-- ARGV[2]: 用户 ID

local buyNum = tonumber(ARGV[1])
local userId = ARGV[2]

if (not buyNum) or buyNum <= 0 then
    return -3
end

local stock = tonumber(redis.call('HGET', KEYS[1], 'stock'))
if not stock then
    return -1
end

local userClaimCount = tonumber(redis.call('HGET', KEYS[2], userId))
if not userClaimCount then
    return -2
end

if userClaimCount < buyNum then
    return -4
end

if userClaimCount == buyNum then
    redis.call('HDEL', KEYS[2], userId)
    if redis.call('HLEN', KEYS[2]) == 0 then
        redis.call('DEL', KEYS[2])
    end
else
    redis.call('HINCRBY', KEYS[2], userId, -buyNum)
end

redis.call('HINCRBY', KEYS[1], 'stock', buyNum)
return 0