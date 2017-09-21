package com.zxg.dao.redisdao.impl;

import com.zxg.dao.redisdao.RedisSearchDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.zxg.utils.CreateIndex.*;

/**
 * Created by Administrator on 2017/8/20.
 */
@Repository
public class RedisSearchDaoImpl implements RedisSearchDao {
	private final RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	public RedisSearchDaoImpl (RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
	
	@Override
	public int indexDocument (String docid, String content) {
		Set<String> words = tokenize(content);
		redisTemplate.setEnableTransactionSupport(true);
		redisTemplate.multi();
		for (String word : words) {
			redisTemplate.opsForSet().add("idx:" + word, docid);
		}
		return redisTemplate.exec().size();
	}
	
	@Override
	public String intersect (int ttl, List<String> list) {
		String id = UUID.randomUUID().toString();
		List<String> otherKeys = setCommon(list);
		redisTemplate.setEnableTransactionSupport(true);
		redisTemplate.multi();
		redisTemplate.opsForSet().intersectAndStore(otherKeys.get(0),otherKeys,"idx:"+id);
		redisTemplate.exec();
		redisTemplate.expire("idx:" + id, ttl, TimeUnit.MILLISECONDS);
		return id;
	}
	
	@Override
	public String union (int ttl, List<String> list) {
		String id = UUID.randomUUID().toString();
		List<String> otherKeys = setCommon(list);
		redisTemplate.setEnableTransactionSupport(true);
		redisTemplate.multi();
		redisTemplate.opsForSet().unionAndStore(otherKeys.get(0),otherKeys,"idx:"+id);
		redisTemplate.exec();
		redisTemplate.expire("idx:" + id, ttl, TimeUnit.MILLISECONDS);
		return id;
	}
	
	@Override
	public String difference (int ttl, List<String> list) {
		String id = UUID.randomUUID().toString();
		List<String> otherKeys = setCommon(list);
		redisTemplate.setEnableTransactionSupport(true);
		redisTemplate.multi();
		redisTemplate.opsForSet().differenceAndStore(otherKeys.get(0),otherKeys,"idx:"+id);
		redisTemplate.exec();
		redisTemplate.expire("idx:" + id, ttl, TimeUnit.MILLISECONDS);
		return id;
	}
	/*这里只完成了 交集操作 之后的工作 待来日学成归来完成*/
	@Override
	public String parseAndSearch (String queryString, int ttl) {
		Map<Query,List<String>> query = parse(queryString);
		List<String> intersect_list = query.get(Query.INTERSECT);
		List<String> union_list = query.get(Query.UNION);
		List<String> difference_list = query.get(Query.DIFFERENCE);
		
		
		if (intersect_list.size() == 0){
			return null;
		}
		return intersect(30, intersect_list);
	}
	
	
	
}
