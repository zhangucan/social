package com.zxg.dao.mongodao;

import com.zxg.domain.social.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Administrator on 2017/8/21.
 */
public interface MongoUserRepository extends MongoRepository<User, Long> {
	User findByName(String name);
	User findByLogin(String login);
}
