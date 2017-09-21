package com.zxg.dao.mongodao;

import com.zxg.domain.social.City;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Administrator on 2017/9/3.
 */
public interface MongoCityDao extends MongoRepository<City, Long>{
}
