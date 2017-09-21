package com.zxg.domain.travel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * Created by Administrator on 2017/8/24.
 */
@NoArgsConstructor
@AllArgsConstructor
public @Data class Attraction {
	@Id
	private Long id;
	//游玩的orDerDAY
	private Long tid;
//	游玩的名称
	private String name;
	//游玩类型
	private String type;
//	价格
	private Double price;
//	地址
	private String loc;
//	电话
	private String phone;
//	其他
	private String other;
}
