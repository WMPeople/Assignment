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

import com.worksmobile.assignment.bo.BoardHistoryService;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

@Component
public class ArticleHistoryEventListener {
	@Autowired
	BoardHistoryService boardHistoryService;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	// TODO : 아래의 3개의 메소드는 유사합니다. 이를 합칠수 있을 수도?
	@EventListener
	@Async
	public void addHistory(ArticleCreatedEvent articleCreatedEvent) {
		Board article = articleCreatedEvent.getArticle();
		boardHistoryService.createHistory(article, BoardHistory.STATUS_CREATED, new NodePtr());
	}
	
	@EventListener
	@Async
	public void addHistory(ArticleModifiedEvent event) {
		Board article = event.getArticle();
		NodePtr parentPtr = event.getParentPtr();
		boardHistoryService.createHistory(article, BoardHistory.STATUS_MODIFIED, parentPtr);
	}
	
	@EventListener
	@Async
	public void addHistory(ArticleRevoeredEvent event) {
		BoardHistory recoverHistory = event.getRecoverHistory();
		Board recoveredArticle = event.getArticle();
		NodePtr parentPtr = event.getParentPtr();
		
		String status = String.format("%s(%s)", BoardHistory.STATUS_RECOVERED, recoverHistory.toString());
		boardHistoryService.createHistory(recoveredArticle, status, parentPtr);
	}
	

	// TODO : check thread safe
	// TODO : 삭제 대상을 파악할 때 한번에 들고와서 판단할 수 있을 것으로 생각됨.
	@EventListener
	@Async
	public void deleteHistories(ArticleDeletedEvent event) {
		NodePtr leafPtr = event.getLeafPtr();
		
		List<NodePtr> deleteHistoryList = new ArrayList<>();
		Set<Integer> fileIds = new HashSet<>();
		
		while (true) {
			BoardHistory deleteHistory = boardHistoryService.selectHistory(leafPtr);
			NodePtr parentPtr = deleteHistory.getParentPtrAndRoot();
			
			deleteHistoryList.add(deleteHistory);
			fileIds.add(deleteHistory.getFile_id());
			
			List<BoardHistory> brothers = boardHistoryService.selectChildren(parentPtr);
			
			if (brothers.size() > 1 ||
				deleteHistory.isInvisibleRoot()) {
				break;
			} else if(brothers.size() == 0) {
				String json = JsonUtils.jsonStringIfExceptionToString(parentPtr);
				throw new RuntimeException("자기자신이 없습니다.(안보이는 루트 제외) nodePtr : " + json);
			}
			leafPtr = parentPtr;
		}
		
		boardHistoryService.deleteBoardHistory(deleteHistoryList);
		publisher.publishEvent(new AutoSaveDeleteRequestEvent(deleteHistoryList, fileIds));
	}
}
