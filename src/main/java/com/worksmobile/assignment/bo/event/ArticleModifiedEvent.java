package com.worksmobile.assignment.bo.event;

import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.NodePtr;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ArticleModifiedEvent {

	@Getter private Board article;
	@Getter private NodePtr parentPtr;
	@Getter private String cookieId;
}
