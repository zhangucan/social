package com.zxg.dao.redisdao;


import java.util.List;

/**
 * Created by Administrator on 2017/8/20.
 */
public interface RedisSearchDao {
	int indexDocument(String docid, String content);
	
	String intersect(int ttl, List<String> list);
	String union(int ttl, List<String> list);
	String difference(int ttl, List<String> list);
	String parseAndSearch(String queryString, int ttl);
}
