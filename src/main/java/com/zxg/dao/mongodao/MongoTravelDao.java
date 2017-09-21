package com.zxg.dao.mongodao;

import com.zxg.domain.social.Travel;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Administrator on 2017/8/29.
 */
public interface MongoTravelDao extends MongoRepository<Travel, Long>{
}
