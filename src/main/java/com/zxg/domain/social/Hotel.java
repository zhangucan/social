package com.zxg.domain.social;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/24.
 */
@AllArgsConstructor
@NoArgsConstructor
public @Data class Hotel implements Serializable {
	private Long id;
	private Long tid;
	private String name;
	
	private Long begin;
	private Long end;
	
	private Double price;
	private String loc;
	private String phone;
	private String other;
	private byte[] pic;
}
