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
import com.worksmobile.assignment.bo.NotExistHistoryException;
import com.worksmobile.assignment.bo.TreeViewerService;

@Controller
public class TreeViewerController {
	
	@Autowired
	private TreeViewerService treeViewerService;

	@RequestMapping(value = "/treeViewer", method = RequestMethod.GET)
	public String treeViewer() {
		return "treeViewer";
	}
	
	@RequestMapping(value = "/treeViewer/{root_board_id}", method = RequestMethod.GET)
	public ModelAndView treeViewer(@PathVariable(value = "root_board_id") int rootBoardId) {
		ModelAndView modelAndView = new ModelAndView("treeViewer");
		modelAndView.addObject("rootBoardId", rootBoardId);
		return modelAndView;
	}
	
	@ResponseBody
	@RequestMapping(value = "/treeViewer/api/{root_board_id}", method = RequestMethod.GET)
	public String treeViewerApi(@PathVariable(value = "root_board_id") int rootBoardId) throws NotExistHistoryException, JsonProcessingException{
		ObjectNode node = treeViewerService.getTreeJson(rootBoardId);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(node);
	}
	
}
