package com.zxg.redissocial.mongo;

import com.zxg.dao.mongodao.MongoCityDao;
import com.zxg.dao.redisdao.RedisSocialDao;
import com.zxg.domain.social.City;
import com.zxg.domain.social.Status;
import com.zxg.domain.social.Travel;
import com.zxg.domain.social.User;
import com.zxg.service.CityService;
import com.zxg.service.StatusService;
import com.zxg.service.TravelService;
import com.zxg.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

/**
 * Created by Administrator on 2017/9/3.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class CityServiceTest {
	@Autowired
	private CityService cityService;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private RedisTemplate<String,Object> redisTemplate;
	@Autowired
	private RedisSocialDao redisSocialDao;
	@Autowired
	private UserService userService;
	@Autowired
	private StatusService statusService;
	@Autowired
	private TravelService travelService;
	@Autowired
	private MongoCityDao mongoCityDao;
	
	@Test
	public void travelCitiesTest(){
		RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
		conn.flushDb();
		mongoTemplate.getCollection("status").drop();
		mongoTemplate.getCollection("user").drop();
		mongoTemplate.getCollection("city").drop();
		mongoTemplate.getCollection("travel").drop();
		
		User user = new User();
		user.setLogin("347947478");
		user.setName("user_test");
		User userTest = userService.createUser(user);
		
		Status status = new Status();
		status.setName("status_name");
		Status statusTest = statusService.postStatus(userTest.getId(), status);
		
		Travel travel = new Travel();
		travel.setName("travel_name");
		travel.setDate(System.currentTimeMillis());
		Travel travelTest = travelService.postTravel(statusTest.getId(), travel);
		
		City city = new City();
		for (int i = 0; i < 50; i++){
			city.setCity("城市:" + i);
			city.setProvince("省份:" + i);
			city.setPlaceName("县/乡/镇:" + i);
			city.setHaveBeenTo((long) i);
			city.setWantToGo((long) i + 100);
			city.setIntroduction("这是简介--！这是简介--！这是简介--！这是简介--！这是简介--！");
			city = cityService.saveCity(city);
			System.out.println(city);
		}
		assert redisTemplate.opsForSet().size("cities:") == 50;
		System.out.println(redisTemplate.opsForSet().size("cities:"));
		
		cityService.addTravelCities(statusTest.getId(),1);
		
		System.out.println(statusTest);
		assert redisTemplate.opsForZSet().zCard("status_city_day:"+statusTest.getId()) == 1;
		assert redisTemplate.opsForSet().size("status_city:"+statusTest.getId()) == 1;
		assert redisTemplate.opsForSet().size("unselect_city:"+statusTest.getId()) == 49;
		
		
		Set<ZSetOperations.TypedTuple<Object>> typedTupleSet = redisTemplate.opsForZSet().rangeWithScores("status_city_day:" + statusTest.getId(),0,-1);
		for (ZSetOperations.TypedTuple typedTuple : typedTupleSet){
			System.out.println(typedTuple.getValue()+" test "+typedTuple.getScore());
		}
		
		
		
		
		cityService.addTravelCities(statusTest.getId(),1);
		System.out.println(statusTest);
		assert redisTemplate.opsForZSet().zCard("status_city_day:"+statusTest.getId()) == 1;
		assert redisTemplate.opsForSet().size("status_city:"+statusTest.getId()) == 1;
		assert redisTemplate.opsForSet().size("unselect_city:"+statusTest.getId()) == 49;
		
		
		Set<ZSetOperations.TypedTuple<Object>> typedTupleSet2 = redisTemplate.opsForZSet().rangeWithScores("status_city_day:" + statusTest.getId(),0,-1);
		for (ZSetOperations.TypedTuple typedTuple : typedTupleSet2){
			System.out.println(typedTuple.getValue()+" test "+typedTuple.getScore());
		}
		
		
		
		
		cityService.delTravelCities(statusTest.getId(),1);
		System.out.println(statusTest);
		assert redisTemplate.opsForZSet().zCard("status_city_day:"+statusTest.getId()) == 1;
		assert redisTemplate.opsForSet().size("status_city:"+statusTest.getId()) == 0;
		assert redisTemplate.opsForSet().size("unselect_city:"+statusTest.getId()) == 50;
		
		Set<ZSetOperations.TypedTuple<Object>> typedTupleSet3 = redisTemplate.opsForZSet().rangeWithScores("status_city_day:" + statusTest.getId(),0,-1);
		for (ZSetOperations.TypedTuple typedTuple : typedTupleSet3){
			System.out.println(typedTuple.getValue()+" test "+typedTuple.getScore());
		}
		
	}
	
	
}
