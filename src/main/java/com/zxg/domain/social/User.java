package com.zxg.domain.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2017/8/16.
 */
@Document
@AllArgsConstructor
@NoArgsConstructor
public @Data class User implements Serializable{
	@Id
	@Field("id")
	private Long id;
	
	@NotNull
	private String login;
	@NotNull
	private String password;
	private String name;
	private Double sex;
	private String loc;
	private String mail;
	private String introduction;
	private byte[] pic;
	
	private Long signup;
	private Integer followers;
	private Integer following;
	private Integer posts;
}
