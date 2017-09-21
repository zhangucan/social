package com.zxg.domain.social;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/8/21.
 */
@Document
@AllArgsConstructor
@NoArgsConstructor
public @Data class Status implements Serializable{
	@Id
	@Field("id")
	private Long id;
	
	private Long uid;
	private String userName;
	
	private String name;
	private Long posted;
	private String introduction;
	private Integer travelDays;
	private Integer under;
	private Long star;
	private byte[] pic;
	private Set<City> cities;
	private List<Hotel> hotels;
}
