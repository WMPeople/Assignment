package com.worksmobile.assignment.bo;

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
import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.mapper.BoardTempMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.BoardTemp;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

/**
 * 자동저장을 테스트합니다.
 * @author khh
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class VersionManagementServiceAutoSaveTest {
	
	@Autowired
	private BoardMapper boardMapper;
	
	@Autowired
	private BoardTempMapper boardTempMapper;
	
	@Autowired
	private BoardHistoryMapper boardHistoryMapper;
	
	@Autowired
	private BoardTempService boardTempService;
	
	@Autowired
	private VersionManagementService versionManagementService;
	
	private Board defaultBoard;
	private BoardHistory defaultCreated;
	private static final String DEFAULT_JUNIT_COOKIE_ID = "JunitCookieId";
	private static final String DEFAULT_CREATED_TIME = "2018-04-26 오전 11:10:32";
	private BoardTemp autoSaveArticle = new BoardTemp();

	@Before
	public void createDefault() throws InterruptedException, ExecutionException {
		defaultBoard = new Board();
		defaultBoard.setSubject("versionTestSub");
		defaultBoard.setContent("versionTestCont");

		defaultCreated = versionManagementService.createArticle(defaultBoard);

		autoSaveArticle.setSubject("자동 저장중...");
		autoSaveArticle.setContent("temp article content");
		autoSaveArticle.setNodePtr(defaultCreated);
		autoSaveArticle.setCookie_id(DEFAULT_JUNIT_COOKIE_ID);

		boardTempService.makeTempBoard(defaultBoard.getBoard_id(), defaultBoard.getVersion(), DEFAULT_JUNIT_COOKIE_ID, DEFAULT_CREATED_TIME, autoSaveArticle.getContent(), autoSaveArticle.getFile_id(), autoSaveArticle.getSubject());
	}

	private NodePtr makeChild(NodePtr parentPtr) throws JsonProcessingException {
		Board child = new Board();
		child.setSubject("childSub");
		child.setContent("childCont");

		NodePtr childPtr = versionManagementService.modifyVersion(child, parentPtr, DEFAULT_JUNIT_COOKIE_ID);
		child.setNodePtr(childPtr);

		Board leafBoard = boardMapper.viewDetail(childPtr.toMap());
		assertNotNull(leafBoard);
		int parentVersion = parentPtr.getVersion() == null ? 0 : parentPtr.getVersion();
		assertEquals((Integer)(parentVersion + 1), childPtr.getVersion());

		JsonUtils.assertConvertToJsonObject(child, leafBoard);

		return childPtr;
	}

	private BoardTemp makeAutoSave(NodePtr nodePtr) {
		BoardTemp autoSave = new BoardTemp();
		autoSave.setSubject("autoSaveSub");
		autoSave.setContent("autoSaveCont");
		autoSave.setNodePtr(nodePtr);
		autoSave.setCookie_id(DEFAULT_JUNIT_COOKIE_ID);
		boardTempService.makeTempBoard(autoSave.getBoard_id(), autoSave.getVersion(), DEFAULT_JUNIT_COOKIE_ID, DEFAULT_CREATED_TIME, autoSave.getContent(), autoSave.getFile_id(), autoSave.getSubject());
		boardTempService.createTempArticleOverwrite(autoSave, null);
		return autoSave;
	}

	@Test
	public void testCreateAutoSaveArticle() throws IOException {
		BoardTemp dbTempArticle = boardTempMapper.viewDetail(autoSaveArticle.toMap());
		// 만든 시간은 비교하지 않습니다. 
		dbTempArticle.setCreated_time(null);
		JsonUtils.assertConvertToJsonObject(autoSaveArticle.toMap(), dbTempArticle.toMap());
		JsonUtils.assertConvertToJsonObject(autoSaveArticle, dbTempArticle);
	}

	@Test
	public void testDeleteArticleHasAutoSave() throws JsonProcessingException, NotLeafNodeException {
		NodePtr rootPtr = defaultCreated;

		NodePtr hasChildrenPtr = makeChild(rootPtr);

		int childrenCnt = 2;
		List<NodePtr> childrenList = new ArrayList<>(childrenCnt);
		for (int i = 0; i < childrenCnt; i++) {
			childrenList.add(makeChild(hasChildrenPtr));
		}

		NodePtr hasChildPtr = childrenList.get(0);
		NodePtr leafPtr = makeChild(hasChildPtr);

		BoardTemp autoSave = makeAutoSave(leafPtr);

		versionManagementService.deleteArticle(leafPtr);

		BoardTemp dbAutoSavedArticle = boardTempMapper.viewDetail(autoSave.toMap());
		assertNull(dbAutoSavedArticle);
	}

	@Test
	public void testDeleteWhenHasChildNodeAndAutoSave() throws JsonProcessingException {
		NodePtr rootPtr = defaultCreated;

		NodePtr middlePtr = makeChild(rootPtr);
		NodePtr childPtr = makeChild(middlePtr);

		BoardTemp autoSave = makeAutoSave(middlePtr);

		versionManagementService.deleteVersion(middlePtr);

		BoardHistory childHistory = boardHistoryMapper.selectHistory(childPtr);
		NodePtr childParentPtr = childHistory.getParentPtrAndRoot();

		assertEquals(rootPtr, childParentPtr);

		BoardTemp dbAutoSaveArticle = boardTempMapper.viewDetail(autoSaveArticle.toMap());
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
		for (int i = 0; i < childrenCnt; i++) {
			childrenList.add(makeChild(middlePtr));
			autoSavedList.add(makeAutoSave(middlePtr));
		}

		versionManagementService.deleteVersion(middlePtr);

		for (NodePtr child : childrenList) {
			BoardHistory history = boardHistoryMapper.selectHistory(child);
			NodePtr parentPtr = history.getParentPtrAndRoot();
			assertEquals(rootPtr, parentPtr);
		}
	}

}
