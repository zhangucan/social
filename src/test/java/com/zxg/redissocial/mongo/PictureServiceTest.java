package com.zxg.redissocial.mongo;

import com.zxg.dao.mongodao.MongoStatusDao;
import com.zxg.dao.mongodao.MongoTravelDao;
import com.zxg.dao.mongodao.MongoUserRepository;
import com.zxg.dao.redisdao.RedisSocialDao;
import com.zxg.domain.social.Picture;
import com.zxg.domain.social.Status;
import com.zxg.domain.social.Travel;
import com.zxg.domain.social.User;
import com.zxg.service.PictureService;
import com.zxg.service.StatusService;
import com.zxg.service.TravelService;
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

import java.util.List;

/**
 * Created by Administrator on 2017/8/29.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class PictureServiceTest {
	@Autowired
	private PictureService pictureService;
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
	private MongoTravelDao mongoTravelDao;
	@Autowired
	private TravelService travelService;
	@Test
	public void postAndDelPicture(){
		RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
		conn.flushDb();
		mongoTemplate.getCollection("user").drop();
		mongoTemplate.getCollection("status").drop();
		mongoTemplate.getCollection("travel").drop();
		mongoTemplate.getCollection("picture").drop();
		
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
		
		Travel travel01 = new Travel();
		travel01.setName("test_travel_name_01");
		travel01.setIntroduction("test_travel_introduction_01");
		travel01.setDate(System.currentTimeMillis());
		assert travelService.postTravel(1, travel01) != null;
		
		Travel travel02 = new Travel();
		travel02.setName("test_travel_name_02");
		travel02.setIntroduction("test_travel_introduction_02");
		travel02.setDate(System.currentTimeMillis());
		assert travelService.postTravel(1, travel02) != null;

		Travel travel03 = new Travel();
		travel03.setName("test_travel_name_03");
		travel03.setIntroduction("test_travel_introduction_03");
		travel03.setDate(System.currentTimeMillis());
		assert travelService.postTravel(1, travel03) != null;
		
		Picture picture01 = new Picture();
		picture01.setPosted(System.currentTimeMillis());
		picture01.setName("picture01");
		
		Picture picture02 = new Picture();
		picture02.setPosted(System.currentTimeMillis());
		picture02.setName("picture02");
		
		Picture picture03 = new Picture();
		picture03.setPosted(System.currentTimeMillis());
		picture03.setName("picture03");
		
		
		assert pictureService.postPicture(1, picture01) != null;
		assert pictureService.postPicture(1, picture02) != null;
		assert pictureService.postPicture(1, picture03) != null;
		
		assert Long.parseLong((String)redisTemplate.opsForHash().get("travel:1","pictures")) == 3;
		
		List<Picture> list = pictureService.listPictureByPage(1,30,1);
		for (Picture picture : list){
			System.out.println(picture);
		}
		
		assert pictureService.deletePicture(1,1);
		assert Long.parseLong((String)redisTemplate.opsForHash().get("travel:1","pictures")) == 2;
		
		System.out.println("************************************");
		List<Picture> list2 = pictureService.listPictureByPage(1,30,1);
		for (Picture picture : list2){
			System.out.println(picture);
		}
	}
	
	
	
}
