package com.worksmobile.assignment.bo.event;

import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ArticleRecoveredEvent {

	@Getter private Board article;
	@Getter private BoardHistory recoverHistory;
	@Getter private NodePtr parentPtr;
}
