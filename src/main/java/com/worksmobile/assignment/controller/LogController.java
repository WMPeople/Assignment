package com.worksmobile.assignment.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class LogController {
	
	@RequestMapping(value = "api/log", method = RequestMethod.GET)
	public ModelAndView show() throws Exception {
		return new ModelAndView("log");
	}


}
