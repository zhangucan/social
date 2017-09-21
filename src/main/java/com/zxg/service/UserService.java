package com.zxg.service;

import com.zxg.domain.social.User;

import java.util.List;


/**
 * Created by Administrator on 2017/8/21.
 */
public interface UserService {
	User createUser (User user);
	
	long createUser (String login, String name);
	
	User findUserById (long id);
	
	List<User> listUserByPage (int pageIndex, int pageSize);
	boolean followUser(long uid, long otherUid);
	boolean unfollowUser(long uid, long otherUid);
	
	List<User> listFollowersByPage (int pageIndex, int pageSize, long uid);
	
	List<User> listFollowingByPage (int pageIndex, int pageSize, long uid);
	
	
	/*这个方法以后需要修改 这只是临时方法*/
	User verifyUser (String login);
}
