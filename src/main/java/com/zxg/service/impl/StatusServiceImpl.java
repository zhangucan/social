package com.zxg.service.impl;

import com.zxg.dao.mongodao.MongoStatusDao;
import com.zxg.dao.mongodao.MongoUserRepository;
import com.zxg.dao.redisdao.RedisSocialDao;
import com.zxg.domain.social.City;
import com.zxg.domain.social.Status;
import com.zxg.domain.social.User;
import com.zxg.domain.util.StatusUtil;
import com.zxg.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.mongodb.gridfs.GridFsCriteria.whereMetaData;

/**
 * Created by Administrator on 2017/8/22.
 */
@Service
public class StatusServiceImpl implements StatusService{
	private MongoStatusDao mongoStatusDao;
	private MongoUserRepository mongoUserRepository;
	private RedisSocialDao redisSocialDao;
	private GridFsOperations operations;
	private MongoTemplate mongoTemplate;
	private RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	public StatusServiceImpl (MongoStatusDao mongoStatusDao,
	                          MongoUserRepository mongoUserRepository,
	                          MongoTemplate mongoTemplate,
	                          RedisTemplate<String, Object> redisTemplate,
	                           RedisSocialDao redisSocialDao,
	                           GridFsOperations operations) {
		this.mongoStatusDao = mongoStatusDao;
		this.mongoUserRepository = mongoUserRepository;
		this.redisSocialDao = redisSocialDao;
		this.operations = operations;
		this.mongoTemplate = mongoTemplate;
		this.redisTemplate = redisTemplate;
	}
	
	
	
	@Override
	public Status postStatus (long uid, Status status) {
		long id = redisSocialDao.postStatus(uid);
		if (id == -1){
			return null;
		}else {
			status.setId(id);
			status.setUid(Long.parseLong(getStatusInfo("status:" + id,"uid")));
			status.setPosted(Long.parseLong(getStatusInfo("status:" + id,"posted")));
			status.setStar(Long.parseLong(getStatusInfo("status:" + id,"star")));
			status.setTravelDays(Integer.parseInt(getStatusInfo("status:" + id,"travelDays")));
			status.setUnder(Integer.parseInt(getStatusInfo("status:" + id,"under")));
			status.setUserName(getStatusInfo("user:"+uid,"name"));
			status.setCities(new HashSet<>());
			status.setHotels(new LinkedList<>());
			refresh(uid);
			return mongoStatusDao.save(status);
		}
	}
	private void refresh(long uid){
		int posts = Integer.parseInt((String) redisTemplate.opsForHash().get("user:"+uid,"posts"));
		Query query = Query.query(Criteria.where("id").is(uid));
		Update update = Update.update("posts",posts);
		mongoTemplate.updateFirst(query, update, User.class);
	}
	private String getStatusInfo(String key, String hashKey){
		return (String) redisTemplate.opsForHash().get(key,hashKey);
	}
	@Override
	public boolean deleteStatus (long uid, long statusId) {
		if (!redisSocialDao.deleteStatus(uid, statusId)){
			return false;
		}else {
			operations.delete(Query.query(whereMetaData("id").is(statusId)));
			refresh(uid);
			return true;
		}
	}
	
	@Override
	public void updateStatus(Status status){
		mongoStatusDao.save(status);
	}
	
	@Override
	public List<Status> getHomeStatus (long uid, int pageIndex, int pageSize){
		Map<Long, Double> map = redisSocialDao.listHomeStatus(uid);
		List<StatusUtil> list = new ArrayList<>();
		for (Map.Entry<Long, Double> entry : map.entrySet() ){
			list.add(new StatusUtil(mongoStatusDao.findOne(entry.getKey()), entry.getValue()));
		}
		return list.stream()
				.skip((pageIndex-1) * pageSize)
				.limit(pageSize)
				.sorted(comparing(StatusUtil::getPosted))
				.map(StatusUtil::getStatus)
				.collect(toList());
	}
	@Override
	public List<Status> getProfileStatus (long uid, int pageIndex, int pageSize){
		Map<Long, Double> map = redisSocialDao.listProfileStatus(uid);
		List<StatusUtil> list = new ArrayList<>();
		for (Map.Entry<Long, Double> entry : map.entrySet() ){
			list.add(new StatusUtil(mongoStatusDao.findOne(entry.getKey()), entry.getValue()));
		}
		return list.stream()
				.skip((pageIndex-1) * pageSize)
				.limit(pageSize)
				.sorted(comparing(StatusUtil::getPosted))
				.map(StatusUtil::getStatus)
				.collect(toList());
	}
	
	@Override
	public Status getStatus(long sid){
		return mongoStatusDao.findOne(sid);
	}

	/*private void updatePosts(long uid){
		int posts = redisSocialDao.getPosts(uid);
		Query query = Query.query(Criteria.where("id").is(uid));
		Update update = Update.update("posts",posts);
		mongoTemplate.updateFirst(query, update, User.class);
	}*/
	
	
}
