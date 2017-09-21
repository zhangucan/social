package com.zxg.dao.mongodao.dao.impl;

import com.zxg.dao.mongodao.MongoUserRepository;
import com.zxg.dao.mongodao.dao.MongoUserDao;
import com.zxg.domain.social.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2017/8/31.
 */
@Repository
public class MongoUserDaoImpl implements MongoUserDao {
	private MongoUserRepository repository;
	private MongoOperations operations;
	@Autowired
	public MongoUserDaoImpl (MongoUserRepository repository,
	                         MongoOperations operations) {
		this.repository = repository;
		this.operations = operations;
	}
	
	@Override
	public User newUser (User user){
		return repository.save(user);
	}
	
	@Override
	public void deleteUser (long uid){
		repository.delete(uid);
	}
	
	@Override
	public User findUser(String login){
		Query query = new Query(Criteria.where("login").is(login));
		return operations.findOne(query,User.class);
	}
	
	@Override
	public User findUser (long id){
		return repository.findOne(id);
	}
	
	
	
	
	
	
	



















}
