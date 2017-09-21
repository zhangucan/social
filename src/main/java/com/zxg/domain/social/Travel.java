package com.zxg.domain.social;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2017/8/27.
 */
@Document
@NoArgsConstructor
@AllArgsConstructor
public @Data class Travel implements Serializable {
	@Id
	private Long id;
	
	private Long sid;
	private Long date;
	private String name;
	private String type;
	private City city;
	private String introduction;
	
	private Integer pictures;
	private Hotel hotel;
	
}
