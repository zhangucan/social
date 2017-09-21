package com.zxg.service;

import com.zxg.domain.social.City;

import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/9/3.
 */
public interface CityService {
	
	List<City> listCity ();
	
	City saveCity (City city);
	
	List<City> addTravelCities (long sid, long cid);
	
	List<City> delTravelCities (long sid, long cid);
	
	List<City> getUnSelectCities (long sid);
	
	City getCity (long id);
}
