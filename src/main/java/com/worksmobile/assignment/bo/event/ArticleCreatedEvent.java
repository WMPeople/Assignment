package com.worksmobile.assignment.bo.event;

import com.worksmobile.assignment.model.Board;

import lombok.Getter;

public class ArticleCreatedEvent {
	
	@Getter private Board article;
	
	public ArticleCreatedEvent(Board article) {
		this.article = article;
	}
}
