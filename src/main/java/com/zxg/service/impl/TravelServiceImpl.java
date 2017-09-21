package com.zxg.service.impl;

import com.zxg.dao.mongodao.MongoTravelDao;
import com.zxg.dao.redisdao.RedisSocialDao;
import com.zxg.domain.social.Status;
import com.zxg.domain.social.Travel;
import com.zxg.domain.social.User;
import com.zxg.domain.util.TravelUtil;
import com.zxg.service.TravelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * Created by Administrator on 2017/8/29.
 */
@Service
public class TravelServiceImpl implements TravelService{
	private RedisSocialDao redisSocialDao;
	private MongoTravelDao mongoTravelDao;
	private MongoTemplate mongoTemplate;
	private RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	public TravelServiceImpl (RedisSocialDao redisSocialDao,
	                          RedisTemplate<String, Object> redisTemplate,
	                          MongoTravelDao mongoTravelDao,
	                          MongoTemplate mongoTemplate) {
		this.redisSocialDao = redisSocialDao;
		this.mongoTravelDao = mongoTravelDao;
		this.redisTemplate = redisTemplate;
		this.mongoTemplate = mongoTemplate;
	}
	
	@Override
	public Travel postTravel(long sid, Travel travel){
		long id = redisSocialDao.postTravel(sid, travel.getDate());
		if (id == -1){
			return null;
		}else {
			travel.setId(id);
			travel.setSid(sid);
			travel.setDate(Long.parseLong(getTravelInfo("travel:"+ id,"date")));
			refresh(sid);
			return mongoTravelDao.save(travel);
		}
	}
	
	@Override
	public Travel updateTravel(Travel travel){
		return mongoTravelDao.save(travel);
	}
	
	private void refresh(long sid){
		int travelDays = Integer.parseInt((String) redisTemplate.opsForHash().get("status:"+sid,"travelDays"));
		Query query = Query.query(Criteria.where("id").is(sid));
		Update update = Update.update("travelDays",travelDays);
		mongoTemplate.updateFirst(query, update, Status.class);
	}
	private String getTravelInfo(String key, String hashKey){
		return (String) redisTemplate.opsForHash().get(key, hashKey);
	}
	@Override
	public boolean deleteTravel (long sid, long tid){
		if (redisSocialDao.deleteStatus(sid, tid)){
			mongoTravelDao.delete(sid);
			return true;
		}else {
			return false;
		}
	}
	
	@Override
	public List<Travel> listTravel (int pageIndex, int pageSize, long sid){
		Map<Long, Double> map = redisSocialDao.listTravel(sid);
		List<TravelUtil> list = new ArrayList<>();
		for (Map.Entry<Long, Double> entry : map.entrySet() ){
			list.add(new TravelUtil(mongoTravelDao.findOne(entry.getKey()), entry.getValue()));
		}
		return list.stream()
				.skip((pageIndex-1) * pageSize)
				.limit(pageSize)
				.sorted(comparing(TravelUtil::getTime))
				.map(TravelUtil::getTravel)
				.collect(toList());
	}
	
}
