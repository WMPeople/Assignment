package com.worksmobile.assignment.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.worksmobile.assignment.BO.NotLeafNodeException;
import com.worksmobile.assignment.BO.VersionManagementService;
import com.worksmobile.assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.assignment.Mapper.BoardMapper;
import com.worksmobile.assignment.Model.Board;
import com.worksmobile.assignment.Model.BoardHistory;
import com.worksmobile.assignment.Model.NodePtr;
import com.worksmobile.assignment.Util.Utils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VersionManagementServiceAutoSaveTest {

	@Autowired
	VersionManagementService versionManagementService;
	
	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;
	
	private Board defaultBoard;
	private BoardHistory defaultCreated;
	private static final int DEFAULT_COOKIE_ID = 99999;
	private Board autoSaveArticle = new Board();
	
	@Before
	public void createDefault() throws InterruptedException, ExecutionException {
		defaultBoard = new Board();
		defaultBoard.setSubject("versionTestSub");
		defaultBoard.setContent("versionTestCont");
		;

		defaultCreated = versionManagementService.createArticle(defaultBoard);
		
		autoSaveArticle.setSubject("자동 저장중...");
		autoSaveArticle.setContent("temp article content");
		autoSaveArticle.setNodePtr(defaultCreated);
		autoSaveArticle.setCookie_id(DEFAULT_COOKIE_ID);
		
		versionManagementService.createTempArticleOverwrite(autoSaveArticle);
	}
	
	private NodePtr makeChild(NodePtr parentPtr) throws JsonProcessingException {
		Board child = new Board();
		child.setSubject("childSub");
		child.setContent("childCont");
		
		NodePtr childPtr = versionManagementService.modifyVersion(child, parentPtr);
		child.setNodePtr(childPtr);
		
		Board leapBoard = boardMapper.viewDetail(childPtr.toMap());
		assertNotNull(leapBoard);
		int parentVersion = parentPtr.getVersion() == null ? 0 : parentPtr.getVersion();
		assertEquals((Integer) (parentVersion + 1), childPtr.getVersion());
		
		Utils.assertConvertToJsonObject(child, leapBoard);
		
		return childPtr;
	}
	
	private Board makeAutoSave(NodePtr nodePtr) {
		Board autoSave = new Board();
		autoSave.setNodePtr(nodePtr);
		autoSave.setCookie_id(DEFAULT_COOKIE_ID);
		versionManagementService.createTempArticleOverwrite(autoSave);
		return autoSave;
	}
	
	
	@Test
	public void testCreateAutoSaveArticle() throws IOException {
		Board dbTempArticle = boardMapper.viewDetail(autoSaveArticle.toMap());
		Utils.assertConvertToJsonObject(autoSaveArticle.toMap(), dbTempArticle.toMap());
		Utils.assertConvertToJsonObject(autoSaveArticle, dbTempArticle);
	}
	
	@Test
	public void testDeleteArticleHasAutoSave() throws JsonProcessingException, NotLeafNodeException {
		NodePtr rootPtr = defaultCreated;
		
		NodePtr hasChildrenPtr = makeChild(rootPtr);
		
		int childrenCnt = 2;
		List<NodePtr> childrenList = new ArrayList<>(childrenCnt);
		for(int i = 0; i < childrenCnt; i++) {
			childrenList.add(makeChild(hasChildrenPtr));
		}
		
		NodePtr hasChildPtr = childrenList.get(0);
		NodePtr leapPtr = makeChild(hasChildPtr);
		
		Board autoSave = makeAutoSave(leapPtr);
		
		versionManagementService.deleteArticle(leapPtr);
		
		Board dbAutoSavedArticle = boardMapper.viewDetail(autoSave.toMap());
		assertNull(dbAutoSavedArticle);
	}
	
	@Test
	public void testDeleteWhenHasChildNodeAndAutoSave() throws JsonProcessingException {
		NodePtr rootPtr = defaultCreated;
		
		NodePtr middlePtr = makeChild(rootPtr);
		NodePtr childPtr = makeChild(middlePtr);
		
		Board autoSave = makeAutoSave(middlePtr);
		
		versionManagementService.deleteVersion(middlePtr);
		
		BoardHistory childHistory = boardHistoryMapper.getHistory(childPtr);
		NodePtr childParentPtr = childHistory.getParentPtrAndRoot();
		
		assertEquals(rootPtr, childParentPtr);
		
		Board dbAutoSaveArticle = boardMapper.viewDetail(autoSaveArticle.toMap());
		assertNotNull(dbAutoSaveArticle);
		Board dbAtuoSave = boardMapper.viewDetail(autoSave.toMap());
		assertNull(dbAtuoSave);
	}
	
	@Test
	public void testDeleteHasChildrenNodeAndAutoSave() throws JsonProcessingException {
		NodePtr rootPtr = defaultCreated;
		
		NodePtr middlePtr = makeChild(rootPtr);

		int childrenCnt = 10;
		List<NodePtr> childrenList = new ArrayList<>(childrenCnt);
		List<NodePtr> autoSavedList = new ArrayList<>(childrenCnt);
		for(int i = 0; i < childrenCnt; i++) {
			childrenList.add(makeChild(middlePtr));
			autoSavedList.add(makeAutoSave(middlePtr));
		}
		
		versionManagementService.deleteVersion(middlePtr);
		
		for (NodePtr child : childrenList) {
			BoardHistory history = boardHistoryMapper.getHistory(child);
			NodePtr parentPtr = history.getParentPtrAndRoot();
			assertEquals(rootPtr, parentPtr);
		}
	}
}
