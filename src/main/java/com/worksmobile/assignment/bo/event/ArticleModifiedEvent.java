package com.worksmobile.assignment.bo.event;

import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.NodePtr;

import lombok.Getter;

public class ArticleModifiedEvent {

	@Getter private Board article;
	@Getter private NodePtr parentPtr;
	@Getter private String cookieId;

	public ArticleModifiedEvent(Board article, NodePtr parentPtr, String cookieId) {
		this.article = article;
		this.parentPtr = parentPtr;
		this.cookieId = cookieId;
	}
}
