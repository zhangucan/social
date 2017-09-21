package com.zxg.service.impl;

import com.zxg.dao.mongodao.MongoPictureDao;
import com.zxg.dao.redisdao.RedisSocialDao;
import com.zxg.domain.social.Picture;
import com.zxg.domain.util.PictureUtil;
import com.zxg.service.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * Created by Administrator on 2017/8/29.
 */
@Service
public class PictureServiceImpl implements PictureService{
	private MongoPictureDao mongoPictureDao;
	private RedisSocialDao redisSocialDao;
	private RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	public PictureServiceImpl (MongoPictureDao mongoPictureDao,
	                           RedisSocialDao redisSocialDao,
	                           RedisTemplate<String, Object> redisTemplate) {
		this.mongoPictureDao = mongoPictureDao;
		this.redisSocialDao = redisSocialDao;
		this.redisTemplate = redisTemplate;
	}
	
	@Override
	public Picture postPicture (long tid, Picture picture){
		long id = redisSocialDao.postPic(tid,picture.getPosted());
		if (id == -1){
			return null;
		}else {
			picture.setId(id);
			picture.setTid(tid);
			picture.setPosted(Long.parseLong(getPictureInfo("picture:" + id, "posted")));
			return mongoPictureDao.save(picture);
		}
	}
	
	@Override
	public boolean deletePicture (long tid, long pid){
		if (redisSocialDao.deletePic(tid, pid)){
			mongoPictureDao.delete(pid);
			return true;
		}
		return false;
	}
	
	@Override
	public List<Picture> listPictureByPage (int pageIndex, int pageSize, long tid){
		Map<Long, Double> map = redisSocialDao.listPicture(tid);
		List<PictureUtil> list = new ArrayList<>();
		for (Map.Entry<Long, Double> entry : map.entrySet() ){
			list.add(new PictureUtil(mongoPictureDao.findOne(entry.getKey()), entry.getValue()));
		}
		return list.stream()
				.skip((pageIndex-1) * pageSize)
				.limit(pageSize)
				.sorted(comparing(PictureUtil::getTime))
				.map(PictureUtil::getPicture)
				.collect(toList());
	}
	private String getPictureInfo(String key, String hashKey){
		return (String) redisTemplate.opsForHash().get(key, hashKey);
	}
}
