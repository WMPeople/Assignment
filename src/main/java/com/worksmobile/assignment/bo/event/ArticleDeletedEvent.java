package com.worksmobile.assignment.bo.event;

import com.worksmobile.assignment.model.NodePtr;

import lombok.Getter;

public class ArticleDeletedEvent {
	
	@Getter private NodePtr leafPtr;
	
	public ArticleDeletedEvent(NodePtr leafPtr) {
		this.leafPtr = leafPtr;
	}
}
