package com.zxg.service;

import com.zxg.domain.social.Picture;

import java.util.List;

/**
 * Created by Administrator on 2017/8/29.
 */
public interface PictureService {
	Picture postPicture(long tid, Picture picture);
	
	boolean deletePicture (long tid, long pid);
	
	List<Picture> listPictureByPage (int pageIndex, int pageSize, long tid);
}
