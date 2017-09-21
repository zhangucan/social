package com.zxg.dao.mongodao.dao;

import com.zxg.domain.social.User;

/**
 * Created by Administrator on 2017/8/31.
 */
public interface MongoUserDao {
	User newUser (User user);
	
	void deleteUser (long uid);
	
	User findUser (String login);
	
	User findUser (long id);
}
