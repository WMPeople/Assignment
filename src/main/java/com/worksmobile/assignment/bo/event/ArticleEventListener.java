package com.worksmobile.assignment.bo.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.worksmobile.assignment.bo.BoardService;

@Component
public class ArticleEventListener {
	@Autowired
	BoardService boardService;
	
}
