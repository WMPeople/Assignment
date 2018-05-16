package com.worksmobile.assignment.bo.event;

import com.worksmobile.assignment.model.NodePtr;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ArticleDeletedEvent {
	
	@Getter private NodePtr leafPtr;
}
