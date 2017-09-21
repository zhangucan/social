package com.zxg.domain.util;

import com.zxg.domain.social.Picture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Administrator on 2017/8/29.
 */
@AllArgsConstructor
@NoArgsConstructor
public @Data class PictureUtil {
	private Picture picture;
	private Double time;
}
