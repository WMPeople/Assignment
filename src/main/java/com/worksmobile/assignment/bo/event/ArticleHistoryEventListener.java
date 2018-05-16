package com.worksmobile.assignment.bo.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.worksmobile.assignment.bo.BoardHistoryService;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;

@Component
public class ArticleHistoryEventListener {
	@Autowired
	private BoardHistoryService boardHistoryService;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	@TransactionalEventListener
	@Async
	public void addHistory(ArticleCreatedEvent articleCreatedEvent) {
		Board article = articleCreatedEvent.getArticle();
		boardHistoryService.createHistory(article, BoardHistory.STATUS_CREATED, new NodePtr());
	}
	
	@TransactionalEventListener
	@Async
	public void addHistory(ArticleModifiedEvent event) {
		Board article = event.getArticle();
		NodePtr parentPtr = event.getParentPtr();
		boardHistoryService.createHistory(article, BoardHistory.STATUS_MODIFIED, parentPtr);
		boardHistoryService.updateHistoryLock(parentPtr, true, false);
	}
	
	@TransactionalEventListener
	@Async
	public void addHistory(ArticleRecoveredEvent event) {
		BoardHistory recoverHistory = event.getRecoverHistory();
		Board recoveredArticle = event.getArticle();
		NodePtr parentPtr = event.getParentPtr();
		
		String status = String.format("%s(%s)", BoardHistory.STATUS_RECOVERED, recoverHistory.getNodePtrStr());
		boardHistoryService.createHistory(recoveredArticle, status, parentPtr);
		boardHistoryService.updateHistoryLock(parentPtr, true, false);
	}
	
	@EventListener
	@Async
	public void deleteHistories(ArticleDeletedEvent event) {
		NodePtr leafPtr = event.getLeafPtr();
		
		List<NodePtr> deleteHistoryList = new ArrayList<>();
		Set<Integer> fileIds = new HashSet<>();
		
		while (true) {
			BoardHistory deleteHistory = boardHistoryService.selectHistory(leafPtr);
			NodePtr parentPtr = deleteHistory.getParentPtrAndRoot();
			
			boardHistoryService.deleteBoardHistory(deleteHistory);
			List<BoardHistory> siblings = boardHistoryService.selectChildren(parentPtr);
			
			deleteHistoryList.add(deleteHistory);
			fileIds.add(deleteHistory.getFile_id());
			
			if (siblings.size() != 0 ||
				deleteHistory.isInvisibleRoot()) {
				break;
			} 
			leafPtr = parentPtr;
		}
		
		publisher.publishEvent(new AutoSaveDeleteRequestEvent(deleteHistoryList, fileIds));
	}
}
