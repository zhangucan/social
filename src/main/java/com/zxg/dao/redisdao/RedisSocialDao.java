package com.zxg.dao.redisdao;


import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017/8/16.
 */
public interface RedisSocialDao {
	
	
	/**
	 * 创建用户
	 * @param login
	 * @param name
	 * @return
	 */
	long createUser(String login, String name);
	
	
	Map<Long, Double> listFollowers (long uid);
	
	Map<Long, Double> listFollowing (long uid);
	
	Map<Long, Double> listTravel (long sid);
	
	/**
	 * 用户关注列表
	 * @param uid
	 * @param otherUid
	 * @return
	 */
	boolean followUser(long uid, long otherUid);
	
	Set<Long> unSelectlistCities (long sid);
	
	boolean addTravelCities (long sid, long cid);
	
	boolean delTravelCities (long sid, long cid);
	
	/**
	 * 取消关注
	 * @param uid
	 * @param otherUid
	 * @return
	 */
	boolean unfollowUser(long uid, long otherUid);
	
	/**
	 * 发布消息的状态
	 * @param uid
	 * @return
	 */
	long postStatus(long uid);
	
	long postTravel (long sid, long posted);
	
	long postPic (long tid, long posted);
	
	long postCity ();
	
	boolean deletePic (long tid, long pid);
	
	Map<Long, Double> listPicture (long tid);
	
	Set<Long> listCity ();
	
	
	/**
	 * 获取时间线上的状态消息
	 * @param uid
	 * @return
	 */
	
	
	Map<Long, Double> listHomeStatus (long uid);
	
	Map<Long, Double> listProfileStatus (long uid);
	
	/**
	 * 删除状态消息
	 * @param uid
	 * @param statusId
	 * @return
	 */
	boolean deleteStatus(long uid, long statusId);
	
	boolean deleteTravel (long sid, long tid);
	
	/**
	 * 这里是个填充任务 应该考虑什么时候用它
	 * @param following_id
	 * @param timeLine_id
	 */
	void refillTimeline( long following_id, long timeLine_id);
	/**
	 * 日后再来做
	 */
	/*void cleanTimelines(long uid, long statusId, double start, boolean onLists);*/
	
	int getPosts(long id);
	
	int getFollowers (long id);
	
	int getFollowing (long id);
}
