package com.zxg.domain.util;

import com.zxg.domain.social.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Administrator on 2017/8/29.
 */
@AllArgsConstructor
@NoArgsConstructor
public @Data class StatusUtil {
	private Status status;
	private Double posted;
}
