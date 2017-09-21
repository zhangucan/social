package com.zxg.dao.redisdao.impl;

import com.zxg.dao.redisdao.RedisSocialDao;
import com.zxg.utils.RedisTransactionPrcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * Created by Administrator on 2017/8/16.
 */
@Repository
public class RedisSocialDaoImpl implements RedisSocialDao {
	private static int HOME_TIMELINE_SIZE = 1000;
	private static int POSTS_PER_PASS = 1000;
	private static int REFILL_USERS_STEP = 50;
	private final RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	public RedisSocialDaoImpl (RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
	
	/**
	 *  关于为什么要对 小写的用户名加锁 ：
	 *      它可以防止多个请求 在同一时间内使用使用相同的用户名来创建用户，
	 *      对这个用户名加锁后程序检查这个用户名是否已经被其他用户注册了，
	 *      如果尚未被占用，那么程序就会生成一个独一无二的id,并将其与id进行存储
	 *   关于什么是分布式锁
	 *      类似 首先获取锁然后执行 最后释放锁，但是 这种锁既不是给
	 *      同一进程中的多个线程使用，也不是给同一机器的多个进程使用
	 *      而是由不同机器的redis客户端进行获取和释放的。
	 *   为什么不使用 watch multi exec
	 *      watch 是乐观锁 就是说，数据在被其他客户端抢先 修改的情况下只会通知
	 *      当前客户端，并不进行阻止。
	 * @param conn
	 * @param lockName
	 * @param acquireTimeout
	 * @param lockTimeout
	 * @return
	 */
	//分布式锁
	/*这是一个悲观锁，acquireLockWithTimeout 会不断尝试去获取锁，直到超时。
		也就是说，如果长时间获取不到，就会获取锁失败，相当于没加锁！
		具体的超时时间设置为多长，有待后期验证，再做优化。*/
	private String acquireLockWithTimeout (RedisConnection conn, String lockName, int acquireTimeout, int lockTimeout) {
		String id = UUID.randomUUID().toString();
		//"user:" + llogin
		lockName = "lock:" + lockName;
		long end = System.currentTimeMillis() + (acquireTimeout * 1000);
		
		while (System.currentTimeMillis() < end) {
			//尝试获取锁
			// 如果 lockName 为空 那么用id代替他的value 如果不为空则不变
			if (conn.setNX(lockName.getBytes(), id.getBytes())){
				// 获取锁并设置过期时间
				redisTemplate.expire(lockName, lockTimeout, TimeUnit.SECONDS);
				return id;
			//	这里是为了确保客户端崩溃的时候 且此时没有设置锁的过期时间时，为锁设置过期时间
			}else if (redisTemplate.getExpire(lockName) <= 0){
				redisTemplate.expire(lockName, lockTimeout, TimeUnit.SECONDS);
			}
			try{
				Thread.sleep(1);
			}catch(InterruptedException ie){
				Thread.interrupted();
			}
			
		}
		return null;
	}
	
	private boolean releaseLock (String lockName, String identifier) {
		lockName = "lock:" + lockName;
		ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
		while (true) {
			redisTemplate.watch(lockName);
			if (identifier.equals(valueOperations.get(lockName))) {
				
				redisTemplate.setEnableTransactionSupport(true);
				redisTemplate.multi();
				redisTemplate.delete(lockName);
				List<Object> result = redisTemplate.exec();
				
				// null response indicates that the transaction was aborted due
				// to the watched key changing.
				// 代表释放锁失败
				if (result == null){
					continue;
				}
				return true;
			}
			redisTemplate.unwatch();
			break;
		}
		return false;
	}
	
	/**
	 * 功能： 客户端注册用户，首先获得login的锁，防止自己选择的login被别人抢先注册
	 *       如果获得了锁并且login没有被注册，该用户的id自增是根据计数器来做的
	 *       那么 可以继续完成用户的注册。最后将该login的锁释放
	 * @param login
	 * @param name
	 * @return
	 */
	
	@Override
	public long createUser (String login, String name) {
		RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
		String llogin = login.toLowerCase();
		String lock = acquireLockWithTimeout(conn, "user:" + llogin, 10, 1);
		// 如果枷锁不成功 说明正在被占用
		if (lock == null){
			return 0;
		}
		// 如果已经被注册 则返回 -1
		if (redisTemplate.opsForHash().get("users:", llogin) != null) {
			return -1;
		}
		long id = redisTemplate.opsForValue().increment("user:id:",1);
		
		processRedisTransaction( r -> {
			r.opsForHash().put("users:", llogin, String.valueOf(id));
			Map<String,String> values = new HashMap<>();
			values.put("login", login);
			values.put("id", String.valueOf(id));
			values.put("name", name);
			values.put("followers", "0");
			values.put("following", "0");
			values.put("posts", "0");
			values.put("signup", String.valueOf(System.currentTimeMillis()));
			r.opsForHash().putAll("user:" + id, values);
			return r.exec();
		});
		releaseLock("user:" + llogin, lock);
		return id;
	}
	
	@Override
	public Map<Long, Double> listFollowers (long uid){
		Map<Long, Double> map = new HashMap<>();
		Cursor<ZSetOperations.TypedTuple<Object>> cursor =
				redisTemplate.opsForZSet().scan("followers:"+uid, ScanOptions.NONE);
		while (cursor.hasNext()){
			ZSetOperations.TypedTuple<Object> item = cursor.next();
			map.put(Long.valueOf((String) item.getValue()),item.getScore());
		}
		return map;
	}
	@Override
	public Map<Long, Double> listFollowing (long uid){
		Map<Long, Double> map = new HashMap<>();
		Cursor<ZSetOperations.TypedTuple<Object>> cursor =
				redisTemplate.opsForZSet().scan("following:"+uid, ScanOptions.NONE);
		while (cursor.hasNext()){
			ZSetOperations.TypedTuple<Object> item = cursor.next();
			map.put(Long.valueOf((String) item.getValue()),item.getScore());
		}
		return map;
	}
	
	@Override
	public Map<Long, Double> listTravel (long sid){
		Map<Long, Double> map = new HashMap<>();
		Cursor<ZSetOperations.TypedTuple<Object>> cursor =
				redisTemplate.opsForZSet().scan("status_travel:" + sid, ScanOptions.NONE);
		while (cursor.hasNext()){
			ZSetOperations.TypedTuple<Object> item = cursor.next();
			map.put(Long.valueOf((String) item.getValue()),item.getScore());
		}
		return map;
	}
	/**
	 * 功能 ：每发布一条消息，就存储状态信息，首先根据传入的 uid 获取 user:id 哈希表的 login,用来在之后判断用户是否存在
	 *       (这里也有个麻烦事，和获取关注列表 zset中的一条记录路类似，传入的 key 都不能为空)
	 *       之后为该 时间状态创建唯一id，这里使用了类似计数器的String 来给id赋值，创建了 status:id的哈希表，
	 *       最后需要在对应的 user:id 中将 已发布消息数值 增加1
	 * @param uid
	 * @return
	 */
	private long createStatus (long uid) {
		
		List<Object> response = processRedisTransaction(
				r -> {
					r.opsForHash().get("user:" + uid,"login");
					r.opsForValue().increment("status:id:",1);
					return r.exec();
				}
		);
		String login = (String)response.get(0);
		long id = (Long)response.get(1);
		if (login == null) {
			return -1;
		}
		
		Map<String,String> data = new HashMap<>();
		processRedisTransaction(r ->{
			data.put("posted", String.valueOf(System.currentTimeMillis()));
			data.put("id", String.valueOf(id));
			data.put("uid", String.valueOf(uid));
			data.put("travelDays", "0");
			data.put("star", "0");
			data.put("under", "0");
			r.opsForHash().putAll("status:" + id, data);
			r.opsForHash().increment("user:" + uid, "posts", 1);
			return r.exec();
		});
		return id;
	}
	
	
	/**
	 *  功能：
	 *     用来描述用户关注的行为，传入用户id 和 关注该用户 的otherid. 用来生成用户的 关注者有序集合 follows:id(其他人关注你)
	 *     和 正在关注有序集合 following:id（你关注其他人）。然后判断是否已经关注过了，如果是就返回。
	 *     然后，开始在两个有序集合中插入关注和正在关注的id，分值是关注的时间。
	 *     并且，需要更新关注和正在关注者 user:id的哈希表，的follows和following两个字段。
	 *     在这里还有件事，就是要对正在关注的主页时间线里，添加被关注用户的个人时间线信息
	 * @param uid
	 * @param otherUid
	 * @return
	 */
	@Override
	public boolean followUser (long uid, long otherUid){
		String fkey1 = "following:" + uid;
		String fkey2 = "followers:" + otherUid;
		//如果uid 已经关注了otherUid的用户那么直接返回
		if(redisTemplate.opsForZSet().score(fkey1,String.valueOf(otherUid)) != null){
			return false;
		}
		long now = System.currentTimeMillis();
		
		List<Object> response = processRedisTransaction(r -> {
			r.opsForZSet().add(fkey1, String.valueOf(otherUid), now);
			r.opsForZSet().add(fkey2, String.valueOf(uid), now);
			r.opsForZSet().zCard(fkey1);
			r.opsForZSet().zCard(fkey2);
			r.opsForZSet().rangeWithScores("profile:" + otherUid, 0,  - 1);
			return r.exec();
		});
		long following = (Long)response.get(response.size() - 3);
		long followers = (Long)response.get(response.size() - 2);
		Set<ZSetOperations.TypedTuple<Object>> typedTupleSet = (Set<ZSetOperations.TypedTuple<Object>>) response.get(response.size() - 1);
		
		processRedisTransaction(r -> {
			r.opsForHash().put("user:" + uid, "following", String.valueOf(following));
			r.opsForHash().put("user:" + otherUid, "followers", String.valueOf(followers));
			
			for (ZSetOperations.TypedTuple<Object> tuple : typedTupleSet){
				r.opsForZSet().add("home:" + uid, tuple.getValue(),tuple.getScore());
			}
			return r.exec();
		});
		return true;
	}
	
	
	
	
	
	
	/**
	 * 取关用户，和关注用户行为基本一致，不过正好倒置
	 * @param uid
	 * @param otherUid
	 * @return
	 */
	@Override
	public boolean unfollowUser (long uid, long otherUid) {
		String fkey1 = "following:" + uid;
		String fkey2 = "followers:" + otherUid;
		if(redisTemplate.opsForZSet().score(fkey1,String.valueOf(otherUid)) == null){
			return false;
		}
		List<Object> response = processRedisTransaction(r ->{
			r.opsForZSet().remove(fkey1, String.valueOf(otherUid));
			r.opsForZSet().remove(fkey2, String.valueOf(uid));
			r.opsForZSet().zCard(fkey1);
			r.opsForZSet().zCard(fkey2);
			r.opsForZSet().range("profile:" + otherUid,0,-1);
			return r.exec();
		});
		long following = (Long)response.get(response.size() - 3);
		long followers = (Long)response.get(response.size() - 2);
		Set<String> statuses = (Set<String>) response.get(response.size() - 1);
		
		processRedisTransaction(r -> {
			r.opsForHash().put("user:" + uid, "following", String.valueOf(following));
			r.opsForHash().put("user:" + otherUid, "followers", String.valueOf(followers));
			
			if (statuses.size() > 0 ){
				for (String key : statuses){
					r.opsForZSet().remove("home:"+ uid,key);
				}
			}
			return r.exec();
		});
		
		return true;
	}
	
	/**
	 * 功能：
	 *      状态消息的发布,首先创建状态消息，该事件状态包含了事件的简介 发布时间 id 用户id 登录名，存这么多是为了防止多表查询
	 *      首先创建状态消息，然后判断状态消息的发布时间 不存在则直接返回，而那条消息将在，获取消息时过滤删除
	 *      然后将该状态消息存入用户个人时间线里边 profile:id(zset) key是状态消息id value是状态消息创建时间
	 *      以上是用户将一个状态消息创建出来后，其自己的行为，接下来是将消息推送给其关注者。
	 * @param uid
	 * @return
	 */
	
	
	
	
	@Override
	public long postStatus (long uid) {
		long id = createStatus(uid);
		String user_id = (String) redisTemplate.opsForHash().get("status:" + id, "uid");
		if (!user_id.equals(String.valueOf(uid))) {
			return -1;
		}
		long posted = Long.parseLong((String) redisTemplate.opsForHash().get("status:" + id, "posted"));
		// 将状态信息添加到用户的个人时间线里边
		redisTemplate.opsForZSet().add("profile:" + uid, String.valueOf(id), posted);
		//将状态消息推送给用户的关注者
		//syndicateStatus(uid, id, posted, 0);
		return id;
	}
	@Override
	public long postTravel (long sid, long posted) {
		long id = createTravel(sid, posted);
		String status_id = (String) redisTemplate.opsForHash().get("travel:" + id, "sid");
		if (!status_id.equals(String.valueOf(sid))) {
			return -1;
		}
		redisTemplate.opsForZSet().add("status_travel:" + sid, String.valueOf(id), posted);
		return id;
	}
	
	private long createTravel(long sid, long posted){
		List<Object> response = processRedisTransaction(r -> {
			r.opsForHash().get("status:" + sid,"uid");
			r.opsForValue().increment("travel:id:",1);
			return r.exec();
		});
		String uid = (String)response.get(0);
		long id = (Long)response.get(1);
		if (uid == null) {
			return -1;
		}
		Map<String,String> data = new HashMap<>();
		processRedisTransaction(r ->{
			data.put("id", String.valueOf(id));
			data.put("sid", String.valueOf(sid));
			data.put("date", String.valueOf(posted));
			data.put("pictures", "0");
			r.opsForHash().putAll("travel:" + id, data);
			r.opsForHash().increment("status:" + sid, "travelDays", 1);
			return r.exec();
		});
		return id;
	}
	@Override
	public long postPic (long tid, long posted){
		long id = createPic(tid, posted);
		String travel_id = (String) redisTemplate.opsForHash().get("picture:" + id, "tid");
		if (!travel_id.equals(String.valueOf(tid))) {
			return -1;
		}
		redisTemplate.opsForZSet().add("travel_picture:" + tid, String.valueOf(id), posted);
		return id;
		
	}
	
	
	private long createPic(long tid, long posted){
		List<Object> response = processRedisTransaction(r -> {
			r.opsForValue().increment("picture:id:",1);
			return r.exec();
		});
		long id = (Long)response.get(0);
		Map<String,String> data = new HashMap<>();
		processRedisTransaction(r ->{
			data.put("id", String.valueOf(id));
			data.put("tid", String.valueOf(tid));
			data.put("posted", String.valueOf(posted));
			r.opsForHash().putAll("picture:" + id, data);
			r.opsForHash().increment("travel:" + tid, "pictures", 1);
			return r.exec();
		});
		return id;
	}
	@Override
	public boolean deletePic (long tid, long pid){
		RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
		String key = "picture:" + pid;
		String lock = acquireLockWithTimeout(conn, key, 1, 10);
		if (lock == null) {
			return false;
		}
		String travel_id = (String) redisTemplate.opsForHash().get("picture:" + pid, "tid");
		if (!String.valueOf(tid).equals(travel_id)) {
			return false;
		}
		processRedisTransaction(r -> {
			r.delete(key);
			r.opsForZSet().remove("travel_picture:" + tid, String.valueOf(pid));
			r.opsForHash().increment("travel:" + tid, "pictures", -1);
			return r.exec();
		});
		releaseLock(key,lock);
		return true;
	}
	
	@Override
	public Map<Long, Double> listPicture (long tid){
		Map<Long, Double> map = new HashMap<>();
		Cursor<ZSetOperations.TypedTuple<Object>> cursor = redisTemplate.opsForZSet().scan("travel_picture:"+tid, ScanOptions.NONE);
		while (cursor.hasNext()){
			ZSetOperations.TypedTuple<Object> item = cursor.next();
			map.put(Long.valueOf((String) item.getValue()),item.getScore());
		}
		return map;
	}
	
	/**
	 * 获取时间线里给定页数的最新状态
	 *       首先获取通过 用户id 获取给定数量的状态消息 id
	 *       然后根据 获得的状态消息id 获取 给定数目状态消息的hash的哈希表 status:id
	 * @return
	 */
	@Override
	public long postCity (){
		long id = createCity();
		String wantToGo = (String) redisTemplate.opsForHash().get("city:" + id ,"wantToGo");
		redisTemplate.opsForZSet().add("wantToGo_cities:" , String.valueOf(id), Long.parseLong(wantToGo));
		redisTemplate.opsForSet().add("cities:" , String.valueOf(id));
		return id;
	}
	private long createCity(){
		List<Object> response = processRedisTransaction(r -> {
			r.opsForValue().increment("city:id:",1);
			return r.exec();
		});
		long id = (Long) response.get(0);
		Map<String,String> data = new HashMap<>();
		processRedisTransaction(r ->{
			data.put("id",String.valueOf(id));
			data.put("wantToGo", "0");
			data.put("haveBeenTo", "0");
			r.opsForHash().putAll("city:" + id, data);
			return r.exec();
		});
		return id;
	}
	
	@Override
	public Set<Long> listCity (){
		Cursor<Object> curosr = redisTemplate.opsForSet().scan("cities:", ScanOptions.NONE);
		Set<Long> set = new HashSet<>();
		while(curosr.hasNext()){
			set.add(Long.parseLong((String) curosr.next()));
		}
		return set;
	}
	
	@Override
	public Set<Long> unSelectlistCities (long sid){
		Cursor<Object> curosr = redisTemplate.opsForSet().scan("unselect_city:" + sid, ScanOptions.NONE);
		Set<Long> set = new HashSet<>();
		while(curosr.hasNext()){
			set.add(Long.parseLong((String) curosr.next()));
		}
		return set;
	}
	@Override
	public boolean addTravelCities (long sid, long cid){
		Long status_id = Long.parseLong((String) redisTemplate.opsForHash().get("status:" + sid,"id"));
		if (status_id != sid){
			return false;
		}
		redisTemplate.opsForZSet().incrementScore("status_city_day:" + sid, String.valueOf(cid) ,1 );
		redisTemplate.opsForSet().add("status_city:" + sid, String.valueOf(cid));
		redisTemplate.opsForSet().differenceAndStore("cities:", "status_city:" + sid,
				"unselect_city:" + sid);
		return true;
	}
	@Override
	public boolean delTravelCities (long sid, long cid){
		Long status_id = Long.parseLong((String) redisTemplate.opsForHash().get("status:" + sid,"id"));
		if (status_id != sid){
			return false;
		}
		redisTemplate.opsForZSet().incrementScore("status_city_day:" + sid, String.valueOf(cid) ,-1 );
		redisTemplate.opsForSet().remove("status_city:" + sid, String.valueOf(cid));
		redisTemplate.opsForSet().differenceAndStore("cities:", "status_city:" + sid,
				"unselect_city:" + sid);
		return true;
	}
	
	
	
	
	
	
	@Override
	public Map<Long, Double> listHomeStatus (long uid) {
		List<Object> response = processRedisTransaction(r -> {
			r.opsForZSet().rangeWithScores(
					"home:" + uid, 0, -1);
			return r.exec();
		});
		Map<Long, Double> map = new HashMap<>();
		Set<ZSetOperations.TypedTuple<Object>> typedTupleSet = (Set<ZSetOperations.TypedTuple<Object>>) response.get(response.size() - 1);
		for (ZSetOperations.TypedTuple typedTuple : typedTupleSet){
			map.put(Long.valueOf((String) typedTuple.getValue()),typedTuple.getScore());
		}
		return map;
	}
	@Override
	public Map<Long, Double> listProfileStatus (long uid) {
		List<Object> response = processRedisTransaction(r -> {
			r.opsForZSet().rangeWithScores(
					"profile:" + uid, 0, -1);
			return r.exec();
		});
		Map<Long, Double> map = new HashMap<>();
		Set<ZSetOperations.TypedTuple<Object>> typedTupleSet = (Set<ZSetOperations.TypedTuple<Object>>) response.get(response.size() - 1);
		for (ZSetOperations.TypedTuple typedTuple : typedTupleSet){
			map.put(Long.valueOf((String) typedTuple.getValue()),typedTuple.getScore());
		}
		return map;
	}
	
	
	@Override
	public boolean deleteStatus (long uid, long sid) {
		RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
		String key = "status:" + sid;
		String lock = acquireLockWithTimeout(conn, key, 1, 10);
		if (lock == null) {
			return false;
		}
		if (!String.valueOf(uid).equals(redisTemplate.opsForHash().get(key, "uid"))) {
			return false;
		}
		processRedisTransaction(r -> {
			r.delete(key);
			r.opsForZSet().remove("profile:" + uid, String.valueOf(sid));
			r.opsForZSet().remove("home:" + uid, String.valueOf(sid));
			r.opsForZSet().remove("status_travel:"+ sid);
			r.opsForSet().remove("status_travel:" + sid);
			r.opsForHash().increment("user:" + uid, "posts", -1);
			return r.exec();
		});
		releaseLock(key,lock);
		return true;
	}
	
	@Override
	public boolean deleteTravel (long sid, long tid){
		RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
		String key = "travel:" + tid;
		String lock = acquireLockWithTimeout(conn, key, 1, 10);
		
		if (lock == null) {
			return false;
		}
		if (!String.valueOf(sid).equals(redisTemplate.opsForHash().get(key, "sid"))) {
			return false;
		}
		processRedisTransaction(r -> {
			r.delete(key);
			r.opsForZSet().remove("status_travel:" + sid, String.valueOf(tid));
			r.opsForHash().increment("status:"+sid, "travelDays", -1);
			return r.exec();
		});
		releaseLock(key,lock);
		return true;
	}
	/**
	 * 功能：
	 *      用来通知关注者状态消息，广播。首先通过时间 （这里的时间需要重新设计），获取0-1000的所有关注者id
	 *      通过消息id 将其添加到所有关注者的主页时间线里边home:id 有序集合，并且重新对主页时间做修剪
	 *
	 *      上边都是错的，我这里因改用 rabbiMQ 来做
param uid
	 * @param postId
	 * @param postTime
	 * @param start
	 */
	private void syndicateStatus (long uid, long postId, long postTime, double start) {
		//(K key, double min, double max, long offset, long count)
		Set<ZSetOperations.TypedTuple<Object>> typedTupleSet = redisTemplate.opsForZSet().rangeWithScores(
				"followers:" + uid,
				0, -1);
		processRedisTransaction(r -> {
			for (ZSetOperations.TypedTuple<Object> typedTuple : typedTupleSet){
				String follower = (String)typedTuple.getValue();
				//start = typedTuple.getScore();
				r.opsForZSet().add("home:" + follower, String.valueOf(postId), postTime);
				r.opsForZSet().range("home:" + follower, 0, -1);
				// 只保留最新的
				r.opsForZSet().removeRange("home:" + follower,0, 0 - HOME_TIMELINE_SIZE - 1);
			}
			return r.exec();
		});
	}
	
	/**
	 * 这个方法 也应该使用 rabbitMQ
	 * @param following_id
	 * @param timeLine_id
	 */
	@Override
	public void refillTimeline(long following_id, long timeLine_id){
		String timeLine = "home:" + String.valueOf(timeLine_id);
		String following = "following:" + String.valueOf(following_id);
		if (redisTemplate.opsForZSet().zCard(timeLine) >= 750) {
			return;
		}
		Set<Object> typedTuple = redisTemplate.opsForZSet()
				.range(following, 0, -1);
		
		List<Object> response = processRedisTransaction(r -> {
			for (Object status : typedTuple){
				String uid = (String) status;
				//倒序排列 根据时间获取固定的数量的 status
				r.opsForZSet().reverseRangeWithScores("profile:" + uid,
						0, HOME_TIMELINE_SIZE - 1);
			}
			return r.exec();
		});
		/*redisTemplate.setEnableTransactionSupport(true);
		redisTemplate.multi();
		for (Object status : typedTuple){
			String uid = (String) status;
			//倒序排列 根据时间获取固定的数量的 status
			redisTemplate.opsForZSet().reverseRangeWithScores("profile:" + uid,
					0, HOME_TIMELINE_SIZE - 1);
		}
		List<Object> response = redisTemplate.exec();*/
		
		List<ZSetOperations.TypedTuple<Object>> messages = new ArrayList<>();
		for (Object results : response) {
			messages.addAll((Set<ZSetOperations.TypedTuple<Object>>)results);
		}
		// 排序
		//Collections.sort(messages);
		//messages = messages.subList(0, HOME_TIMELINE_SIZE);
		messages = messages.stream()
				.sorted((x, y) -> y.getScore().compareTo(x.getScore()))
				.limit(HOME_TIMELINE_SIZE)
				.collect(toList());
		
		List<ZSetOperations.TypedTuple<Object>> finalMessages = messages;
		processRedisTransaction(r ->{
			if (finalMessages.size() > 0) {
				for (ZSetOperations.TypedTuple<Object> tuple : finalMessages) {
					r.opsForZSet().add(timeLine, tuple.getValue(), tuple.getScore());
				}
			}
			r.opsForZSet().removeRange(timeLine, 0, 0 - HOME_TIMELINE_SIZE - 1);
			return r.exec();
		});
		/*redisTemplate.setEnableTransactionSupport(true);
		redisTemplate.multi();
		if (messages.size() > 0) {
			for (ZSetOperations.TypedTuple<Object> tuple : messages) {
				redisTemplate.opsForZSet().add(timeLine, tuple.getValue(), tuple.getScore());
			}
		}
		redisTemplate.opsForZSet().removeRange(timeLine, 0, 0 - HOME_TIMELINE_SIZE - 1);
		redisTemplate.exec();*/
	}
	
	@Override
	public int getPosts (long id) {
		return (int) redisTemplate.opsForHash().get("user:" + id, "posts");
		
	}
	@Override
	public int getFollowers (long id) {
		return Integer.valueOf((String) redisTemplate.opsForHash().get("user:" + id, "followers"));
	}
	@Override
	public int getFollowing (long id) {
		return Integer.valueOf((String) redisTemplate.opsForHash().get("user:" + id, "following"));
	}
	
	private List<Object> processRedisTransaction(RedisTransactionPrcessor p){
		redisTemplate.setEnableTransactionSupport(true);
		redisTemplate.multi();
		return p.process(redisTemplate);
	}
}
