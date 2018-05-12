package com.worksmobile.assignment.bo.event;

import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;

import lombok.Getter;

public class ArticleModifiedEvent {

	@Getter private Board article;
	@Getter private BoardHistory parentPtr;
	@Getter private String cookieId;

	public ArticleModifiedEvent(Board article, BoardHistory parentPtr, String cookieId) {
		this.article = article;
		this.parentPtr = parentPtr;
		this.cookieId = cookieId;
	}
}
