package com.zxg.service;

import com.zxg.domain.social.Travel;

import java.util.List;

/**
 * Created by Administrator on 2017/8/29.
 */
public interface TravelService {
	Travel postTravel(long sid, Travel travel);
	
	Travel updateTravel (Travel travel);
	
	boolean deleteTravel (long sid, long tid);
	
	List<Travel> listTravel (int pageIndex, int pageSize, long sid);
}
