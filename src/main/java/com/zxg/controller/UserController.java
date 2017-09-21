package com.zxg.controller;

import com.zxg.domain.social.User;
import com.zxg.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Created by Administrator on 2017/8/30.
 */
@Controller
public class UserController {
	
	private UserService userService;
	
	@Autowired
	public UserController (UserService userService) {
		this.userService = userService;
	}
	
	
	
	@RequestMapping(value = "/",method = RequestMethod.GET)
	public String index(Model model){
		model.addAttribute("user", new User());
		return "/login";
	}
	@RequestMapping(value = "/user_login", method = RequestMethod.POST)
	public String userLogin(@ModelAttribute User user, RedirectAttributes model){
		User mongoUser = userService.verifyUser(user.getLogin());
		if (mongoUser != null){
			model.addFlashAttribute("user", mongoUser);
			model.addAttribute("login", mongoUser.getLogin());
			return "redirect:/user_home_{login}";
		}
		return "/login";
	}
	
	@RequestMapping(value = "/user_home_{login}",method = RequestMethod.GET)
	public String home(@PathVariable String login, Model model){
		model.addAttribute("today",System.currentTimeMillis());
		if (!model.containsAttribute("user")){
			model.addAttribute("user",userService.verifyUser(login));
		}
		return "/home";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
