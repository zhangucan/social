package com.zxg.domain.util;

import com.zxg.domain.social.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Administrator on 2017/8/28.
 */
@AllArgsConstructor
@NoArgsConstructor
public @Data class UserUtil {
	private User user;
	private Double time;
}
