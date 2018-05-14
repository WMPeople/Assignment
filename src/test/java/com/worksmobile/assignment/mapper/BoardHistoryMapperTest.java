package com.worksmobile.assignment.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.worksmobile.assignment.AssignmentApplication;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.File;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

/**
 * 게시글 이력 테이블 매퍼의 테스트입니다.
 * @author khh
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AssignmentApplication.class)
@WebAppConfiguration
@Transactional
public class BoardHistoryMapperTest {

	@Autowired
	private BoardHistoryMapper boardHistoryMapper;

	@Autowired
	private BoardMapper boardMapper;
	
	@Autowired
	private FileMapper fileMapper;
	
	@Rule
	public ErrorCollector collector = new ErrorCollector();

	private static BoardHistory defaultHistory;
	private static NodePtr defaultNodePtr;
	private BoardHistory boardHistory = null;

	public BoardHistoryMapperTest() {
		defaultNodePtr = new NodePtr(1000, 6, 1);
		defaultHistory = new BoardHistory();
		defaultHistory.setBoard_id(defaultNodePtr.getBoard_id());
		defaultHistory.setHistory_content(new byte[1]);
		defaultHistory.set_content_compressed(false);
		defaultHistory.setVersion(defaultNodePtr.getVersion());
		defaultHistory.setFile_id(0);
		defaultHistory.setCreated_time("2018-05-12 0:00:00");
		
		defaultHistory.setStatus("Created");
		defaultHistory.setHistory_subject("sub");
	}

	@Test
	public void testGetHistory() {
		BoardHistory history = null;
		history = boardHistoryMapper.selectHistory(defaultNodePtr);
		if (history == null) {
			boardHistoryMapper.createHistory(defaultHistory);
		}

		history = boardHistoryMapper.selectHistory(defaultNodePtr);

		assertNotNull(history);
	}
	
	@Test
	public void testCreateHistory() throws IOException {
		Board article = new Board();
		article.setBoard_id(defaultNodePtr.getBoard_id());
		article.setSubject("testInsert");
		article.setContent("testContent");

		article.setNodePtr(defaultNodePtr);
		BoardHistory createdHistory = BoardAdapter.from(article);
		createdHistory.setStatus(BoardHistory.STATUS_CREATED);

		BoardHistory check = boardHistoryMapper.selectHistory(defaultNodePtr);
		if (check != null) {
			boardHistoryMapper.deleteHistory(defaultNodePtr);
		}
		int createdCnt = boardHistoryMapper.createHistory(createdHistory);
		assertEquals(1, createdCnt);

		BoardHistory inserted = null;
		inserted = boardHistoryMapper.selectHistory(createdHistory);

		JsonUtils.assertConvertToJsonObject(createdHistory, inserted);
	}
	
	private BoardHistory createBoardHistoryIfNotExists() {
		boardHistory = boardHistoryMapper.selectHistory(defaultNodePtr);
		if (boardHistory == null) {
			defaultHistory.setRoot_board_id(defaultHistory.getBoard_id());
			int createdCnt = boardHistoryMapper.createHistory(defaultHistory);
			assertEquals(1, createdCnt);
			boardHistory = boardHistoryMapper.selectHistory(defaultNodePtr);
		}
		assertNotNull(boardHistory);
		return boardHistory;
	}

	@Test
	public void testUpdateHistory() throws JsonProcessingException {
		boardHistory = createBoardHistoryIfNotExists();
		
		if (boardHistory.getParent_version() != null) {
			boardHistory.setParent_version(null);
		} else {
			boardHistory.setParent_version(1);
		}
		if (boardHistory.getRoot_board_id() != 1) {
			boardHistory.setRoot_board_id(1);
		} else {
			boardHistory.setRoot_board_id(0);
		}

		int updateRtn = boardHistoryMapper.updateHistoryParentAndRoot(boardHistory);
		assertEquals(1, updateRtn);

		BoardHistory dbHistory = boardHistoryMapper.selectHistory(defaultNodePtr);
		JsonUtils.assertConvertToJsonObject(boardHistory, dbHistory);
	}

	@Test
	public void testDeleteSpecificOne() {
		boardHistory = createBoardHistoryIfNotExists();
		boardMapper.deleteBoard(defaultNodePtr.toMap());
		int deletedColCnt = boardHistoryMapper.deleteHistory(defaultNodePtr);

		assertEquals(1, deletedColCnt);

		BoardHistory deletedHistory = null;
		deletedHistory = boardHistoryMapper.selectHistory(defaultNodePtr);
		assertNull(deletedHistory);
	}
	
	@Test
	public void testDeleteMultipleWithParentLinked() throws JsonProcessingException {
		int deleteCnt = 10;
		List<NodePtr> historyList = new ArrayList<>(deleteCnt);
		
		boardHistory = createBoardHistoryIfNotExists();
		historyList.add(boardHistory);
		
		for(int i = 0; i < deleteCnt; i++) {
			BoardHistory cloned = boardHistory.clone();
			cloned.setParentNodePtrAndRoot(cloned);
			cloned.setBoard_id(cloned.getBoard_id() + 1);
			
			boardHistoryMapper.createHistory(cloned);
			historyList.add(cloned);
			boardHistory = cloned;
		}
		
		boardHistoryMapper.deleteHistories(historyList);
		
		for(NodePtr ele : historyList) {
			BoardHistory boardHistory = boardHistoryMapper.selectHistory(ele);
			String json = JsonUtils.jsonStringFromObject(ele);
			collector.checkThat(json, boardHistory, is(nullValue()));
		}
	}
	
	@Test
	public void testGetChildren() {
		boardHistory = createBoardHistoryIfNotExists();
		boardHistory.setParentNodePtrAndRoot(boardHistory);
		boardHistory.setVersion(boardHistory.getVersion() + 1);
		boardHistoryMapper.createHistory(boardHistory);
		
		List<BoardHistory> children = boardHistoryMapper.selectChildren(defaultNodePtr);
		assertNotEquals(0, children.size());
		assertEquals(boardHistory, children.get(0));
	}
	
	@Test
	public void testGetFileCount() {
		BoardHistory boardHistory = null;
		boardHistory = boardHistoryMapper.selectHistory(defaultNodePtr);
		if (boardHistory == null || boardHistory.getFile_id() == 0) {
			boardHistoryMapper.deleteHistory(boardHistory);
			File file = new File();
			fileMapper.createFile(file);
			defaultHistory.setFile_id(file.getFile_id());
			boardHistoryMapper.createHistory(defaultHistory);
			boardHistory = boardHistoryMapper.selectHistory(defaultNodePtr);
		}
		assertNotNull(boardHistory);
		int file_id = boardHistory.getFile_id();
		int fileCount = boardHistoryMapper.selectFileCount(file_id);
		assertEquals(1, fileCount);	
	}

	@Test
	public void testGetHistoryByRootBoardId() throws JsonProcessingException {
		int relatedCnt = 6;
		List<BoardHistory> createdSameRootList = new ArrayList<>(relatedCnt);
		boardHistory = createBoardHistoryIfNotExists();
		BoardHistory firstEle = boardHistoryMapper.selectHistory(boardHistory);
		createdSameRootList.add(firstEle.clone());
		for(int i = 0; i < relatedCnt - 1; i++) {
			boardHistory.setParentNodePtrAndRoot(boardHistory);
			boardHistory.setVersion(boardHistory.getVersion() + 1);
			boardHistoryMapper.createHistory(boardHistory);
			BoardHistory dbEle = boardHistoryMapper.selectHistory(boardHistory);
			createdSameRootList.add(dbEle.clone());
		}
		List<BoardHistory> sameRoot = boardHistoryMapper.selectHistoryByRootBoardId(boardHistory.getRoot_board_id());
		assertEquals(relatedCnt, sameRoot.size());
		for(int i = 0; i < relatedCnt; i++) {
			BoardHistory createdEle = createdSameRootList.get(i);
			BoardHistory mapperEle = sameRoot.get(i);
			JsonUtils.assertConvertToJsonObject(createdEle, mapperEle);
		}
	}
}
