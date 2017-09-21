package com.zxg.domain.social;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/27.
 */
@Document
@AllArgsConstructor
@NoArgsConstructor
public @Data class Picture implements Serializable {
	@Id
	private Long id;

	private Long tid;
	private String name;
	private Long posted;
	private String introduction;
	private String content;
	
	private String contentType;
	private long size;
	private byte[] pic;
}
