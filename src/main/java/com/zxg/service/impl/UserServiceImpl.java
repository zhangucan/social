package com.zxg.service.impl;

import com.zxg.dao.mongodao.MongoUserRepository;
import com.zxg.dao.mongodao.dao.MongoUserDao;
import com.zxg.dao.redisdao.RedisSocialDao;
import com.zxg.domain.social.User;
import com.zxg.domain.util.UserUtil;
import com.zxg.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import static java.util.stream.Collectors.*;


/**
 * Created by Administrator on 2017/8/21.
 */
@Service
public class UserServiceImpl implements UserService{
	
	private MongoUserRepository mongoUserRepository;
	private RedisSocialDao redisSocialDao;
	private MongoTemplate mongoTemplate;
	private RedisTemplate<String, Object> redisTemplate;
	private MongoUserDao mongoUserDao;
	@Autowired
	public UserServiceImpl (MongoUserRepository mongoUserRepository,
	                        MongoTemplate mongoTemplate,
	                        MongoUserDao mongoUserDao,
	                         RedisTemplate<String, Object> redisTemplate,
	                         RedisSocialDao redisSocialDao) {
		this.mongoUserRepository = mongoUserRepository;
		this.mongoTemplate = mongoTemplate;
		this.redisSocialDao = redisSocialDao;
		this.mongoUserDao = mongoUserDao;
		this.redisTemplate = redisTemplate;
	}
	
	public User createUser (User user) {
		long id = redisSocialDao.createUser(user.getLogin(), user.getName());
		if (id == -1){
			return null;
		}
		user.setId(id);
		user.setFollowers(Integer.parseInt(getUseInfo("user:" + id, "followers")));
		user.setFollowing(Integer.parseInt(getUseInfo("user:" + id, "following")));
		user.setPosts(Integer.parseInt(getUseInfo("user:" + id, "posts")));
		user.setSignup(Long.parseLong(getUseInfo("user:" + id, "signup")));
		mongoUserRepository.save(user);
		return user;
	}
	private String getUseInfo(String key, String hashKey){
		return (String)redisTemplate.opsForHash().get(key,hashKey);
	}
	
	public long createUser(String login, String name){
		return redisSocialDao.createUser(login, name);
	}
	
	public User findUserById (long id){
		return mongoUserRepository.findOne(id);
	}
	
	public List<User> listUserByPage (int pageIndex, int pageSize){
		Page<User> page = null;
		List<User> list = null;
		
		Sort sort = new Sort(Sort.Direction.DESC,"signup");
		Pageable pageable = new PageRequest(pageIndex, pageSize, sort);
		
		page = mongoUserRepository.findAll(pageable);
		list = page.getContent();
		return list;
	}
	
	
	public boolean followUser(long uid, long otherUid){
		if (redisSocialDao.followUser(uid, otherUid)){
			refresh(uid, otherUid);
			return true;
		}
		return false;
	}
	
	public boolean unfollowUser(long uid, long otherUid){
		if (redisSocialDao.unfollowUser(uid, otherUid)){
			refresh(uid, otherUid);
			return true;
		}
		return false;
	}
	
	@Override
	public List<User> listFollowersByPage (int pageIndex, int pageSize, long uid){
		Map<Long, Double> map = redisSocialDao.listFollowers(uid);
		List<UserUtil> list = new ArrayList<>();
		for (Map.Entry<Long, Double> entry : map.entrySet() ){
			list.add(new UserUtil(mongoUserRepository.findOne(entry.getKey()), entry.getValue()));
		}
		return list.stream()
				.skip((pageIndex-1) * pageSize)
				.limit(pageSize)
				.sorted(comparing(UserUtil::getTime))
				.map(UserUtil::getUser)
				.collect(toList());
	}
	@Override
	public List<User> listFollowingByPage (int pageIndex, int pageSize, long uid){
		Map<Long, Double> map = redisSocialDao.listFollowing(uid);
		List<UserUtil> list = new ArrayList<>();
		for (Map.Entry<Long, Double> entry : map.entrySet() ){
			list.add(new UserUtil(mongoUserRepository.findOne(entry.getKey()), entry.getValue()));
		}
		return list.stream()
				.skip((pageIndex-1) * pageSize)
				.limit(pageSize)
				.sorted(comparing(UserUtil::getTime))
				.map(UserUtil::getUser)
				.collect(toList());
	}
	
	/*这个方法以后需要修改 这只是临时方法*/
	@Override
	public User verifyUser (String login) {
		User mongo_user =  mongoUserDao.findUser(login);
		if (mongo_user.getLogin().equals(login)){
			return mongo_user;
		}
		return null;
	}
	
	
	
	private void  refresh(long uid, long otherUid){
		int followers = redisSocialDao.getFollowers(otherUid);
		int following = redisSocialDao.getFollowing(uid);

		Query followers_query = Query.query(Criteria.where("id").is(otherUid));
		Update followers_update = Update.update("followers",followers);
		mongoTemplate.updateFirst(followers_query, followers_update, User.class);
		
		Query following_query = Query.query(Criteria.where("id").is(uid));
		Update following_update = Update.update("following",following);
		mongoTemplate.updateFirst(following_query, following_update, User.class);
	}
	
	
}
