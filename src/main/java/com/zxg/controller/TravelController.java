package com.zxg.controller;

import com.zxg.domain.social.City;
import com.zxg.domain.social.Status;
import com.zxg.domain.social.Travel;
import com.zxg.domain.social.User;
import com.zxg.service.CityService;
import com.zxg.service.StatusService;
import com.zxg.service.TravelService;
import com.zxg.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017/9/2.
 */
@Controller
public class TravelController {
	private UserService userService;
	private StatusService statusService;
	private TravelService travelService;
	private CityService cityService;
	@Autowired
	public TravelController (UserService userService,
	                         CityService cityService,
	                         StatusService statusService,
	                         TravelService travelService) {
		this.userService = userService;
		this.statusService = statusService;
		this.travelService = travelService;
		this.cityService = cityService;
	}
	
	@RequestMapping(value = "/new_travel_{status_id}",
					produces = "application/json")
	public String getTravel(@PathVariable("status_id") String sid, Model model){
		Status status = statusService.getStatus(Long.parseLong(sid));
		User user = userService.findUserById(status.getUid());
		model.addAttribute("status", status);
		model.addAttribute("user", user);
		model.addAttribute("travel", new Travel());
		model.addAttribute("cities", cityService.listCity());
		return "travel/new_travel";
	}
	
	@RequestMapping(value = "/add_travel_city", produces = "application/json" ,method = RequestMethod.POST)
	public String addTravelCities( @RequestParam Map<String, Object> map, Model model){
		Long sid = Long.parseLong((String) map.get("sid"));
		Long cid = Long.parseLong((String) map.get("cid"));
		
		Status status = statusService.getStatus(sid);
		User user = userService.findUserById(status.getUid());
		model.addAttribute("status", status);
		model.addAttribute("user", user);
		
	
		List<City> cities = cityService.addTravelCities(sid, cid);
		model.addAttribute("cities", cities);
		return "travel/select_city";
	}
	@RequestMapping(value = "/del_travel_city", produces = "application/json" ,method = RequestMethod.POST)
	public String delTravelCities( @RequestParam Map<String, Object> map, Model model){
		Long sid = Long.parseLong((String) map.get("sid"));
		Long cid = Long.parseLong((String) map.get("cid"));
		
		Status status = statusService.getStatus(sid);
		User user = userService.findUserById(status.getUid());
		model.addAttribute("status", status);
		model.addAttribute("user", user);
		
		List<City> cities = cityService.delTravelCities(sid, cid);
		model.addAttribute("cities", cities);
		return "travel/select_city";
	}
	
	@RequestMapping(value = "/unselect_city", produces = "application/json" ,method = RequestMethod.POST)
	public String unSelectCities(@RequestParam Map<String, Object> map, Model model){
		Long sid = Long.parseLong((String) map.get("sid"));
		
		Status status = statusService.getStatus(sid);
		User user = userService.findUserById(status.getUid());
		model.addAttribute("status", status);
		model.addAttribute("user", user);
		
		List<City> cities = cityService.getUnSelectCities(sid);
		model.addAttribute("cities", cities);
		return "travel/list_city";
	}
}
