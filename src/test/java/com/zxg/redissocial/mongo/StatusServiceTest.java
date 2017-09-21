package com.zxg.redissocial.mongo;

import com.zxg.dao.mongodao.MongoStatusDao;
import com.zxg.dao.mongodao.MongoUserRepository;
import com.zxg.dao.redisdao.RedisSocialDao;
import com.zxg.domain.social.Status;
import com.zxg.domain.social.User;
import com.zxg.service.CityService;
import com.zxg.service.StatusService;
import com.zxg.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/27.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StatusServiceTest {
	@Autowired
	private StatusService statusService;
	@Autowired
	private UserService userService;
	@Autowired
	private RedisSocialDao redisSocialDao;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private GridFsTemplate gridFsTemplate;
	@Autowired
	private MongoStatusDao mongoStatusDao;
	@Autowired
	private MongoUserRepository mongoUserRepository;
	@Autowired
	private CityService cityService;
	
	
	@Test
	public void postStatus(){
		RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
		conn.flushDb();
		mongoTemplate.getCollection("user").drop();
		mongoTemplate.getCollection("status").drop();
		
		User user01 = new User();
		user01.setLogin("347947478");
		user01.setName("test");
		userService.createUser(user01);
		
		User user02 = new User();
		user02.setLogin("347947478a");
		user02.setName("testa");
		userService.createUser(user02);
		
		User user03 = new User();
		user03.setLogin("347947478b");
		user03.setName("testb");
		userService.createUser(user03);
		
		Status status01 = new Status();
		status01.setName("test01");
		status01.setUserName(user01.getName());
		
		Status status02 = new Status();
		status02.setName("test02");
		status02.setUserName(user01.getName());
		
		Status status03 = new Status();
		status03.setName("test03");
		status03.setUserName(user02.getName());
		
		assert  statusService.postStatus(user01.getId(), status01) != null;
		assert  statusService.postStatus(user01.getId(), status02) != null;
		assert  statusService.postStatus(user02.getId(), status03) != null;
		
		System.out.println(mongoStatusDao.findOne(status01.getId()));
		assert mongoStatusDao.findOne(status01.getId()).getUid() ==
				Long.parseLong((String) redisTemplate.opsForHash().get("status:"+status01.getId(),"uid"));
		assert mongoStatusDao.findOne(status01.getId()).getUid() ==
				Long.parseLong((String) redisTemplate.opsForHash().get("status:"+status02.getId(),"uid"));
		assert mongoStatusDao.findOne(status03.getId()).getUid() ==
				Long.parseLong((String) redisTemplate.opsForHash().get("status:"+status03.getId(),"uid"));
	}
	@Test
	public void deleteStatus(){
		RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
		conn.flushDb();
		mongoTemplate.getCollection("user").drop();
		mongoTemplate.getCollection("status").drop();
		
		User user01 = new User();
		user01.setLogin("347947478");
		user01.setName("test");
		userService.createUser(user01);
		
		User user02 = new User();
		user02.setLogin("347947478a");
		user02.setName("testa");
		userService.createUser(user02);
		
		User user03 = new User();
		user03.setLogin("347947478b");
		user03.setName("testb");
		userService.createUser(user03);
		
		Status status01 = new Status();
		status01.setName("test01");
		status01.setUserName(user01.getName());
		
		Status status02 = new Status();
		status02.setName("test02");
		status02.setUserName(user01.getName());
		
		Status status03 = new Status();
		status03.setName("test03");
		status03.setUserName(user02.getName());
		
		assert  statusService.postStatus(user01.getId(), status01) != null;
		assert  statusService.postStatus(user01.getId(), status02) != null;
		assert  statusService.postStatus(user02.getId(), status03) != null;
		
		assert mongoUserRepository.findOne(1l).getPosts().equals(2);
		assert redisTemplate.opsForZSet().zCard("profile:" + user01.getId()) == 2;
		assert mongoUserRepository.findOne(2l).getPosts().equals(1);
		assert redisTemplate.opsForZSet().zCard("profile:" + user02.getId()) == 1;
		
		System.out.println(mongoUserRepository.findOne(1l).getPosts());
		statusService.deleteStatus(1,1);
		assert mongoUserRepository.findOne(1l).getPosts().equals(1);
		assert redisTemplate.opsForZSet().zCard("profile:" + user01.getId()) == 1;
	}
	
	@Test
	public void findStatus(){
		RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
		conn.flushDb();
		mongoTemplate.getCollection("user").drop();
		mongoTemplate.getCollection("status").drop();
		
		User user01 = new User();
		user01.setLogin("347947478");
		user01.setName("test");
		userService.createUser(user01);
		
		User user02 = new User();
		user02.setLogin("347947478a");
		user02.setName("testa");
		userService.createUser(user02);
		
		User user03 = new User();
		user03.setLogin("347947478b");
		user03.setName("testb");
		userService.createUser(user03);
		
		Status status01 = new Status();
		status01.setName("test01");
		status01.setUserName(user01.getName());
		
		Status status02 = new Status();
		status02.setName("test02");
		status02.setUserName(user01.getName());
		
		Status status03 = new Status();
		status03.setName("test03");
		status03.setUserName(user02.getName());
		
		assert  statusService.postStatus(user01.getId(), status01) != null;
		assert  statusService.postStatus(user01.getId(), status02) != null;
		assert  statusService.postStatus(user02.getId(), status03) != null;
		
		assert statusService.getStatus(1).getName().equals("test01");
		List<Status> list = statusService.getProfileStatus(1,1,30);
		assert list.size() == 2;
		
		
		userService.followUser(2,1);
		List<Status> list02 = statusService.getHomeStatus(2,1,30);
		assert list02.size() == 2;
		
		userService.followUser(1,2);
		List<Status> list03 = statusService.getHomeStatus(1,1,30);
		assert list03.size() == 1;
	}
	
	@Test
	public void test(){
		RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
		conn.flushDb();
		
		mongoTemplate.getCollection("status").drop();
		
		
		User user01 = new User();
		user01.setLogin("347947478");
		user01.setName("test");
		userService.createUser(user01);
		
		/*Status status02 = new Status();
		status02.setName("test03");
		Status status = statusService.postStatus(1,status02);*/
		
		
		cityService.addTravelCities(8,1);
		System.out.println(mongoStatusDao.findOne(8l));
	}
}
