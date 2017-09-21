package com.zxg.restful;

import com.zxg.domain.social.Status;
import com.zxg.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Administrator on 2017/9/2.
 */
@RestController
public class RestStatusController {
	private StatusService statusService;
	
	@Autowired
	public RestStatusController (StatusService statusService) {
		this.statusService = statusService;
	}
	
	@GetMapping(value = "/rest_new_status_{user_id}", produces = "application/json")
	public ResponseEntity<Status> createStatus(@PathVariable("user_id") String uid, Status status){
		status.setUid(Long.parseLong(uid));
		statusService.postStatus(Long.parseLong(uid), status);
		return  ResponseEntity
					.ok()
					.header(HttpHeaders.CONTENT_TYPE,
							"application/json")
					.body(status);
	}
	@PostMapping(value = "/rest_new_status_{user_id}")
	public @ResponseBody Status getStatus(@PathVariable("user_id") String uid, @ModelAttribute("status") Status status){
		return  statusService.postStatus(Long.parseLong(uid), status);
	}
}
