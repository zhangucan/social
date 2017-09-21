package com.zxg.utils;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * Created by Administrator on 2017/8/22.
 */
public interface RedisTransactionPrcessor {
	List<Object> process(RedisTemplate<String, Object> redisTemplate);
}
