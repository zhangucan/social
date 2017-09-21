package com.zxg.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Created by Administrator on 2017/8/20.
 */
public class CreateIndex {
	private static final Pattern WORDS_RE = Pattern.compile("[\u4E00-\u9FA5]|[a-z']{2,}");
	private static final Pattern QUERY_RE = Pattern.compile("[+-]?[\u4E00-\u9FA5]|[a-z']{2,}");
	/**
	 * 建立发布消息的反向索引
	 * @param content
	 * @return
	 */
	public static Set<String> tokenize(String content) {
		Set<String> words = new HashSet<>();
		Matcher matcher = WORDS_RE.matcher(content.toLowerCase());
		while (matcher.find()){
			String word = matcher.group().trim();
			words.add(word);
		}
		return words;
	}
	
	/**
	 *
	 * @param content
	 * @return
	 */
	public static List<String> query(String content){
		List<String> words = new ArrayList<>();
		Matcher matcher = WORDS_RE.matcher(content.toLowerCase());
		while (matcher.find()){
			String word = matcher.group().trim();
			words.add(word);
		}
		return words;
	}
	public enum Query { INTERSECT, UNION, DIFFERENCE };
	public static Map<Query,List<String>> parse(String query){
		Matcher matcher = QUERY_RE.matcher(query.toLowerCase());
		List<String> words = new ArrayList<>();
		while (matcher.find()){
			String word = matcher.group().trim();
			words.add(word);
		}
		/*这里用到了java高级特性*/
		return words.stream()
				.collect(groupingBy(word -> {
					if (word.charAt(0) == '+') return Query.UNION;
					else if (word.charAt(0) == '-') return Query.DIFFERENCE;
					else return Query.INTERSECT;
				}));
	}
	
	public static List<String> setCommon (List<String> list) {
		return list.stream()
				.map(word -> {
					if (word.charAt(0) == '+') return "idx:" + word.substring(1);
					else if (word.charAt(0) == '-') return "idx" + word.substring(1);
					else return "idx:" + word;
				}).distinct()
				.collect(toList());
	}
	
	
}
