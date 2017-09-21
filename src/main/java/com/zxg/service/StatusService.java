package com.zxg.service;

import com.zxg.domain.social.Status;

import java.util.List;

/**
 * Created by Administrator on 2017/8/22.
 */
public interface StatusService {
	Status postStatus (long uid, Status status);
	
	boolean deleteStatus (long uid, long statusId);
	
	
	void updateStatus (Status status);
	
	List<Status> getHomeStatus (long uid, int pageIndex, int pageSize);
	
	
	List<Status> getProfileStatus (long uid, int pageIndex, int pageSize);
	
	Status getStatus (long sid);
}
