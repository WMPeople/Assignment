package com.worksmobile.assignment.bo.event;

import com.worksmobile.assignment.model.Board;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ArticleCreatedEvent {
	
	@Getter private Board article;
}
