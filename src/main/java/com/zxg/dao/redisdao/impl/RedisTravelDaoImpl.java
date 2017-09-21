package com.zxg.dao.redisdao.impl;

import com.zxg.dao.redisdao.RedisTravelDao;
import com.zxg.utils.RedisTransactionPrcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/24.
 */
@Repository
public class RedisTravelDaoImpl implements RedisTravelDao{
	
	private final RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	public RedisTravelDaoImpl (RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
	
	@Override
	public Long createTravel (String name, String message) {
		Long now = System.currentTimeMillis();
		long id = redisTemplate.opsForValue().increment("travelNote:id:",1);
		/*processRedisTransaction(r -> {
			Map<String,String> values = new HashMap<>();
			values.put("name", name);
			values.put("id", String.valueOf(id));
			values.put("message", message);
			return
		});*/
		return null;
	}
	public List<Object> processRedisTransaction(RedisTransactionPrcessor p){
		redisTemplate.setEnableTransactionSupport(true);
		redisTemplate.multi();
		return p.process(redisTemplate);
	}
}
