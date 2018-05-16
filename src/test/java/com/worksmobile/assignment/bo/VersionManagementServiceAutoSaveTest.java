package com.worksmobile.assignment.bo;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.worksmobile.assignment.util.BoardUtil;
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
	
	@Autowired
	private BoardUtil boardUtil;
	
	private Board defaultCreated;
	private static final String DEFAULT_JUNIT_COOKIE_ID = "JunitCookieId";
	private static final String DEFAULT_CREATED_TIME = "2018-04-26 오전 11:10:32";
	private BoardTemp autoSaveArticle;

	@Before
	public void createDefault() throws InterruptedException, ExecutionException {
		defaultCreated = BoardUtil.makeArticle("versionTestSub", "versionTestCont");
		
		defaultCreated = versionManagementService.createArticle(defaultCreated);
		await().untilAsserted(() -> assertThat(boardHistoryMapper.selectHistory(defaultCreated), is(notNullValue())));
		
		autoSaveArticle = new BoardTemp();
		autoSaveArticle.setSubject("자동 저장중...");
		autoSaveArticle.setContent("temp article content");
		autoSaveArticle.setNodePtr(defaultCreated);
		autoSaveArticle.setCookie_id(DEFAULT_JUNIT_COOKIE_ID);

		boardTempService.makeTempBoard(defaultCreated.getBoard_id(), defaultCreated.getVersion(), DEFAULT_JUNIT_COOKIE_ID, DEFAULT_CREATED_TIME, autoSaveArticle.getContent(), autoSaveArticle.getFile_id(), autoSaveArticle.getSubject());
	}
	
	private BoardTemp makeAutoSave(final NodePtr nodePtr) {
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

		NodePtr hasChildrenPtr = boardUtil.makeChild(rootPtr);

		int childrenCnt = 2;
		List<NodePtr> childrenList = new ArrayList<>(childrenCnt);
		for (int i = 0; i < childrenCnt; i++) {
			childrenList.add(boardUtil.makeChild(hasChildrenPtr));
		}

		NodePtr hasChildPtr = childrenList.get(0);
		NodePtr leafPtr = boardUtil.makeChild(hasChildPtr);

		BoardTemp autoSave = makeAutoSave(leafPtr);

		versionManagementService.deleteArticle(leafPtr);

		await().untilAsserted(() -> assertThat(boardTempMapper.viewDetail(autoSave.toMap()), is(nullValue())));
	}

	@Test
	public void testDeleteWhenHasChildNodeAndAutoSave() throws JsonProcessingException {
		NodePtr rootPtr = defaultCreated;

		NodePtr middlePtr = boardUtil.makeChild(rootPtr);
		NodePtr childPtr = boardUtil.makeChild(middlePtr);

		BoardTemp autoSave = makeAutoSave(middlePtr);

		versionManagementService.deleteVersion(middlePtr);

		BoardHistory childHistory = boardHistoryMapper.selectHistory(childPtr);
		NodePtr childParentPtr = childHistory.getParentPtrAndRoot();

		assertEquals(new NodePtr(rootPtr), childParentPtr);
		
		await().untilAsserted(() -> assertThat(boardTempMapper.viewDetail(autoSave.toMap()), is(nullValue())));
		Board dbAtuoSave = boardMapper.viewDetail(autoSave.toMap());
		assertNull(dbAtuoSave);
	}
	
	@Test
	public void testDeleteHasChildrenNodeAndAutoSave() throws JsonProcessingException {
		NodePtr rootPtr = new NodePtr(defaultCreated);

		NodePtr middlePtr = boardUtil.makeChild(rootPtr);

		int childrenCnt = 10;
		List<NodePtr> childrenList = new ArrayList<>(childrenCnt);
		List<NodePtr> autoSavedList = new ArrayList<>(childrenCnt);
		for (int i = 0; i < childrenCnt; i++) {
			childrenList.add(boardUtil.makeChild(middlePtr));
			autoSavedList.add(makeAutoSave(middlePtr));
		}

		versionManagementService.deleteVersion(middlePtr);
		
		for (NodePtr child : childrenList) {
			BoardHistory childhistory = boardHistoryMapper.selectHistory(child);
			NodePtr parentPtr = childhistory.getParentPtrAndRoot();
			JsonUtils.assertConvertToJsonObject(rootPtr, parentPtr);
		}
	}
	
	@Test
	public void testModifyAricleHasAutoSave() {
		BoardTemp autoSave = makeAutoSave(defaultCreated);
		NodePtr beforeModified = new NodePtr(autoSave);

		NodePtr modifiedNodePtr = versionManagementService.modifyVersion(defaultCreated, defaultCreated, DEFAULT_JUNIT_COOKIE_ID);
		BoardTemp modifiedAutoSave = makeAutoSave(modifiedNodePtr);
		BoardTemp dbModifiedAutoSave = makeAutoSave(modifiedAutoSave);
		assertNotNull(dbModifiedAutoSave);
		
		HashMap<String, Object> params = beforeModified.toMap();
		params.put("cookie_id", DEFAULT_JUNIT_COOKIE_ID);
		BoardTemp beforeAutoSave = boardTempMapper.viewDetail(params);
		assertNull(beforeAutoSave);
	}
}
