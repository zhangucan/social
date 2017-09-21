package com.zxg.controller;

import com.zxg.domain.social.Status;
import com.zxg.domain.social.User;
import com.zxg.service.StatusService;
import com.zxg.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Created by Administrator on 2017/8/30.
 */
@Controller
public class StatusController {
	
	private StatusService statusService;
	private UserService userService;
	
	@Autowired
	public StatusController (StatusService statusService,
	                         UserService userService) {
		this.statusService = statusService;
		this.userService = userService;
	}
	

	@RequestMapping (value = "/new_status_{user_id}",
			produces = "application/json",
			method = RequestMethod.GET
	)
	public String createStatus(@PathVariable("user_id") String uid, Model model){
		System.out.println(uid);
		User user = userService.findUserById(Long.parseLong(uid));
		model.addAttribute("user", user);
		model.addAttribute("status", new Status());
		return "status/new_status";
	}
	
	@RequestMapping (value = "/new_status_{user_id}",
			produces = "application/json",
			method = RequestMethod.POST
	)
	public String createStatus(@PathVariable("user_id") String uid,
	                           Status status,
	                           RedirectAttributes model){
		long userId = Long.parseLong(uid);
		Status newStatus = statusService.postStatus(userId,status);
		model.addAttribute("status_id",newStatus.getId());
		return "redirect:/new_travel_{status_id}";
	}
	
	
}
