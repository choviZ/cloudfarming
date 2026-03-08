-- KEYS[1]: 秒杀活动缓存 key（Hash）
-- KEYS[2]: 用户购买计数 key（Hash）
-- ARGV[1]: 本次购买数量
-- ARGV[2]: 用户 ID
-- ARGV[3]: 购买计数 key 过期秒数
-- ARGV[4]: 每人限购数量

local buyNum = tonumber(ARGV[1])
local userId = ARGV[2]
local expireSeconds = tonumber(ARGV[3])
local limit = tonumber(ARGV[4])

if (not buyNum) or buyNum <= 0 then
    return -3
end

local stock = tonumber(redis.call('HGET', KEYS[1], 'stock'))
if (not stock) or stock < buyNum then
    return -1
end

local userClaimCount = tonumber(redis.call('HGET', KEYS[2], userId))
if not userClaimCount then
    userClaimCount = 0
end

if userClaimCount + buyNum > limit then
    return -2
end

if userClaimCount == 0 then
    redis.call('HSET', KEYS[2], userId, buyNum)
    if expireSeconds and expireSeconds > 0 then
        redis.call('EXPIRE', KEYS[2], expireSeconds)
    end
else
    redis.call('HINCRBY', KEYS[2], userId, buyNum)
end

redis.call('HINCRBY', KEYS[1], 'stock', -buyNum)
return 0