package com.zxg.domain.travel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/8/24.
 */

@NoArgsConstructor
@AllArgsConstructor
public @Data class TravelsDay {
	
	@Id
	//
	private Long id;
	//今天的日期
	private Long date;
	
	private Long noteId;
	//当前所在地
	private String destinationCity;
	//第几天
	private Integer orderDay;
	//	备注
	private String Remarks;
	
	
	//游玩的地点
	private List<Long> attractions;
	//住的地方
	private Long hotel;
	//随记
	private Long note;
	//路线
	private Long route;
	
}
