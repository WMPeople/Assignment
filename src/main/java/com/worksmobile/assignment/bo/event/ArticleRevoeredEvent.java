package com.worksmobile.assignment.bo.event;

import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;

import lombok.Getter;

public class ArticleRevoeredEvent {

	@Getter private Board article;
	@Getter private BoardHistory recoverHistory;
	@Getter private NodePtr parentPtr;
	
	public ArticleRevoeredEvent(Board article, BoardHistory recoverHistory, NodePtr parentPtr) {
		this.article = article;
		this.recoverHistory = recoverHistory;
		this.parentPtr = parentPtr;
	}
}
