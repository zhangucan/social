package com.zxg.domain.travel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * Created by Administrator on 2017/8/24.
 */
@AllArgsConstructor
@NoArgsConstructor
public @Data class TravelNote {
	@Id
	private Long id;
	private String name;
	private String message;
	private Long date;
}
