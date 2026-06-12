-- auction_place_bid.lua
-- 原子出价操作：在一个Lua调用中完成校验+更新
--
-- KEYS[1] = auction:{productId}:price   -- 当前最高价
-- KEYS[2] = auction:{productId}:winner  -- 当前最高出价者ID
-- ARGV[1] = bidAmount                   -- 出价金额
-- ARGV[2] = userId                      -- 出价者ID
-- ARGV[3] = startPrice                  -- 起拍价（首次出价时使用）
-- ARGV[4] = sellerId                    -- 卖家ID（禁止自拍）
--
-- 返回: {0, 错误信息} 或 {1, 新价格}

local currentPrice = redis.call('GET', KEYS[1])
if currentPrice == false then
    currentPrice = ARGV[3]
end

local bidAmount = tonumber(ARGV[1])

-- 卖家不能给自己的商品出价
if ARGV[2] == ARGV[4] then
    return {0, "不能给自己的商品出价"}
end

-- 出价必须高于当前价
if bidAmount <= tonumber(currentPrice) then
    return {0, "出价必须高于当前价 " .. currentPrice}
end

redis.call('SET', KEYS[1], ARGV[1])
redis.call('SET', KEYS[2], ARGV[2])
return {1, ARGV[1]}
