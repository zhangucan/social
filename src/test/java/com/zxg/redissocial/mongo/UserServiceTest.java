package com.zxg.redissocial.mongo;

import com.mongodb.DB;
import com.zxg.dao.mongodao.dao.MongoUserDao;
import com.zxg.dao.redisdao.RedisSocialDao;
import com.zxg.domain.social.User;
import com.zxg.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by Administrator on 2017/8/28.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {
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
	private MongoUserDao mongoUserDao;
	
	@Test
	public void verifyUser(){
		/*RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
		conn.flushDb();
		mongoTemplate.getCollection("user").drop();*/
		
		User user1 = new User();
		user1.setLogin("347947478");
		user1.setName("zhangucan");
		User user = userService.createUser(user1);
		System.out.println(user);
		User user2 = //mongoUserDao.findUser(user1.getLogin());
		userService.verifyUser(user1.getLogin());
		System.out.println(user2);
	}
	
	
	@Test
	public void createUser(){
		RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
		conn.flushDb();
		mongoTemplate.getCollection("user").drop();
		
		User user1 = new User();
		user1.setLogin("347947478@qq.com");
		user1.setName("zhangucan");
		userService.createUser(user1);
		
		User user2 = new User();
		user2.setLogin("347947478a");
		user2.setName("testa");
		userService.createUser(user2);
		
		User user3 = new User();
		user3.setLogin("347947478b");
		user3.setName("testb");
		userService.createUser(user3);
		
		assert redisTemplate.opsForHash().get("user:1","id").equals(String.valueOf(user1.getId()));
		
		assert redisTemplate.opsForHash().get("user:1","followers")
				.equals(String.valueOf(user1.getFollowers()));
		assert redisTemplate.opsForHash().get("user:1","following")
				.equals(String.valueOf(user1.getFollowing()));
		
		
	}
	
	@Test
	public void followTest(){
		RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
		conn.flushDb();
		mongoTemplate.getCollection("user").drop();
		
		User user1 = new User();
		user1.setLogin("347947478@qq.com");
		user1.setName("test");
		userService.createUser(user1);
		
		User user2 = new User();
		user2.setLogin("347947478a");
		user2.setName("testa");
		userService.createUser(user2);
		
		User user3 = new User();
		user3.setLogin("347947478b");
		user3.setName("testb");
		userService.createUser(user3);
		
		assert userService.followUser(1,2);
		assert userService.followUser(3,2);
		
		assert userService.listFollowersByPage(1,30,2).size() == 2;
		assert userService.listFollowingByPage(1,30,1).size() == 1;
		assert userService.listFollowingByPage(1,30,3).size() == 1;
		
		assert userService.unfollowUser(1,2);
		assert redisTemplate.opsForHash().get("user:1", "following")
				.equals("0");
		assert redisTemplate.opsForHash().get("user:2", "followers")
				.equals("1");
		
		assert userService.unfollowUser(3,2);
		assert redisTemplate.opsForHash().get("user:3", "following")
				.equals("0");
		assert redisTemplate.opsForHash().get("user:2", "followers")
				.equals("0");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
