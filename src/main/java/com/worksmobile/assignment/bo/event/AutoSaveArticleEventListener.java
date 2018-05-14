package com.worksmobile.assignment.bo.event;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.worksmobile.assignment.bo.BoardTempService;
import com.worksmobile.assignment.mapper.BoardTempMapper;
import com.worksmobile.assignment.model.BoardTemp;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

@Component
public class AutoSaveArticleEventListener {

	@Autowired
	private BoardTempService boardTempService;
	
	@Autowired
	private BoardTempMapper boardTempMapper;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	@TransactionalEventListener(phase=TransactionPhase.BEFORE_COMMIT)
	public void deleteAutoSave(ArticleModifiedEvent event) {
		NodePtr oldNodePtr = event.getParentPtr();
		String cookieId = event.getCookieId();
		
		HashMap<String, Object> deleteParams = oldNodePtr.toMap();
		deleteParams.put("cookie_id", cookieId);
		
		BoardTemp autoSave = boardTempMapper.viewDetail(deleteParams);
		if(autoSave == null) {
			return;
		}
		publisher.publishEvent(new AttachmentChangedEvent(autoSave.getFile_id()));
		
		boardTempService.deleteBoardTemp(deleteParams);
		
	}
	
	@EventListener
	public void deleteAutoSaves(AutoSaveDeleteRequestEvent event) {
		List<NodePtr> deleteList = event.getNodePtrList();
		List<BoardTemp> autoSaveList = boardTempMapper.selectBoardTemps(deleteList);
		int deletedCnt = boardTempMapper.deleteBoardTempsWithoutCookieId(deleteList);
		
		if(deletedCnt != autoSaveList.size()) {
			String json = JsonUtils.jsonStringIfExceptionToString(deleteList);
			throw new RuntimeException("deleted cnt : " + deletedCnt + " but, " + autoSaveList.size() + " expected. \n" +
										"deleteList : " + json);
		}
		
		Set<Integer> fileIds = event.getFileIds();
		for(BoardTemp ele : autoSaveList) {
			fileIds.add(ele.getFile_id());
		}
		publisher.publishEvent(new AttachmentChangedEvent(fileIds));
	}
}
