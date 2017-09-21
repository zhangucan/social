package com.zxg.dao.mongodao;

import com.zxg.domain.social.Status;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Administrator on 2017/8/22.
 */
public interface MongoStatusDao extends MongoRepository<Status, Long> {
}
