package com.worksmobile.assignment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worksmobile.assignment.bo.CreateTree;

@Controller
public class CreateTreeController {
	
	@Autowired
	private CreateTree createTree;

	@RequestMapping(value = "/displayTree/", method = RequestMethod.GET)
	public ModelAndView displayTree() {
		ModelAndView modelAndView = new ModelAndView("displayTree");
		return modelAndView;
	}
	
	@ResponseBody
	@RequestMapping(value = "/displayTree/api/{root_board_id}", method = RequestMethod.GET)
	public String treeApi(@PathVariable(value = "root_board_id") int rootBoardId) {
		ObjectNode node = createTree.main(rootBoardId);
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(node);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
}
