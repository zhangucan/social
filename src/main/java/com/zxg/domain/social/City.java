package com.zxg.domain.social;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/24.
 */

@Document
@AllArgsConstructor
@NoArgsConstructor
public @Data class City implements Serializable{
	@Id
	private Long id;
	private String city;
	private String province;
	private String placeName;
	
	private Long wantToGo;
	private Long haveBeenTo;
	
	private String introduction;
	private byte[] pic;
}
