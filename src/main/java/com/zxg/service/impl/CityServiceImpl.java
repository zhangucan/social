package com.zxg.service.impl;

import com.zxg.dao.mongodao.MongoCityDao;
import com.zxg.dao.redisdao.RedisSocialDao;
import com.zxg.domain.social.City;
import com.zxg.domain.social.Status;
import com.zxg.domain.util.CityUtil;
import com.zxg.domain.util.StatusUtil;
import com.zxg.service.CityService;
import com.zxg.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Created by Administrator on 2017/9/3.
 */
@Service
public class CityServiceImpl implements CityService {
	private MongoCityDao mongoCityDao;
	private RedisTemplate<String, Object> redisTemplate;
	private RedisSocialDao redisSocialDao;
	private StatusService statusService;
	@Autowired
	public CityServiceImpl (MongoCityDao mongoCityDao,
	                        RedisSocialDao redisSocialDao,
	                        StatusService statusService,
	                        RedisTemplate<String, Object> redisTemplate){
		this.redisTemplate = redisTemplate;
		this.redisSocialDao = redisSocialDao;
		this.statusService = statusService;
		this.mongoCityDao = mongoCityDao;
	}
	
	
	
	@Override
	public List<City> listCity (){
		Set<Long> set = redisSocialDao.listCity();
		List<City> list = new ArrayList<>();
		for (Long cid : set){
			list.add(mongoCityDao.findOne(cid));
		}
		return list.stream()
				.sorted(comparing(City::getWantToGo).reversed())
				.collect(toList());
	}
	
	@Override
	public City saveCity (City city){
		long id =  redisSocialDao.postCity();
		long wantToGo = Long.parseLong((String)redisTemplate.opsForHash().get("city:" + id,"wantToGo"));
		long haveBeenTo = Long.parseLong((String)redisTemplate.opsForHash().get("city:" + id,"haveBeenTo"));
		city.setHaveBeenTo(wantToGo);
		city.setHaveBeenTo(haveBeenTo);
		city.setId(id);
		return mongoCityDao.save(city);
	}
	
	
	@Override
	public List<City> addTravelCities (long sid, long cid){
		if (!redisSocialDao.addTravelCities(sid, cid)){
			return null;
		}
		Status status = statusService.getStatus(sid);
		City city = mongoCityDao.findOne(cid);
		status.getCities().add(city);
		statusService.updateStatus(status);
		return status.getCities()
				.stream()
				.sorted(comparing(City::getWantToGo).reversed())
				.collect(toList());
	}
	
	
	
	@Override
	public List<City> delTravelCities (long sid, long cid){
		if (!redisSocialDao.delTravelCities(sid, cid)){
			return null;
		}
		Status status = statusService.getStatus(sid);
		City city = mongoCityDao.findOne(cid);
		status.getCities().remove(city);
		statusService.updateStatus(status);
		return status.getCities()
				.stream()
				.sorted(comparing(City::getWantToGo).reversed())
				.collect(toList());
	}
	
	@Override
	public List<City> getUnSelectCities(long sid){
		Set<Long> set = redisSocialDao.unSelectlistCities(sid);
		List<City> list = new ArrayList<>();
		for (Long cid : set){
			list.add(mongoCityDao.findOne(cid));
		}
		return list.stream()
				.sorted(comparing(City::getWantToGo).reversed())
				.collect(toList());
	}
	@Override
	public City getCity (long id){
		return mongoCityDao.findOne(id);
	}
}
