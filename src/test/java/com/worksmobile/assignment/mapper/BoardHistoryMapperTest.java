package com.worksmobile.assignment.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.worksmobile.assignment.AssignmentApplication;
import com.worksmobile.assignment.bo.Compress;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AssignmentApplication.class)
@WebAppConfiguration
@Transactional
public class BoardHistoryMapperTest {

	@Autowired
	BoardHistoryMapper boardHistoryMapper;

	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	FileMapper fileMapper;

	private static BoardHistory defaultHistory;
	private static NodePtr defaultNodePtr;
	private BoardHistory boardHistory = null;

	public BoardHistoryMapperTest() {
		defaultNodePtr = new NodePtr(1000, 6, 1);
		defaultHistory = new BoardHistory();
		defaultHistory.setBoard_id(defaultNodePtr.getBoard_id());
		defaultHistory.setVersion(defaultNodePtr.getVersion());
		defaultHistory.setFile_id(1000);

		defaultHistory.setStatus("Created");
		defaultHistory.setHistory_subject("sub");
	}

	@Test
	public void testGetHistory() {
		BoardHistory history = null;
		history = boardHistoryMapper.getHistory(defaultNodePtr);
		if (history == null) {
			boardHistoryMapper.createHistory(defaultHistory);
		}

		history = boardHistoryMapper.getHistory(defaultNodePtr);

		assertNotNull(history);
	}
	
	@Test
	public void testCreateHistory() throws IOException {
		Board article = new Board();
		article.setBoard_id(defaultNodePtr.getBoard_id());
		article.setSubject("testInsert");
		article.setContent("testContent");

		BoardHistory createdHistory = new BoardHistory(article, defaultNodePtr, BoardHistory.STATUS_CREATED);
		createdHistory.setHistory_content(Compress.compressArticleContent(article));

		BoardHistory check = boardHistoryMapper.getHistory(defaultNodePtr);
		if (check != null) {
			boardHistoryMapper.deleteHistory(defaultNodePtr);
		}
		int createdCnt = boardHistoryMapper.createHistory(createdHistory);
		assertEquals(1, createdCnt);

		BoardHistory inserted = null;
		inserted = boardHistoryMapper.getHistory(createdHistory);

		JsonUtils.assertConvertToJsonObject(createdHistory, inserted);
	}
	
	private BoardHistory createBoardHistoryIfNotExists() {
		boardHistory = boardHistoryMapper.getHistory(defaultNodePtr);
		if (boardHistory == null) {
			defaultHistory.setRoot_board_id(defaultHistory.getBoard_id());
			int createdCnt = boardHistoryMapper.createHistory(defaultHistory);
			assertEquals(1, createdCnt);
			boardHistory = boardHistoryMapper.getHistory(defaultNodePtr);
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

		BoardHistory dbHistory = boardHistoryMapper.getHistory(defaultNodePtr);
		JsonUtils.assertConvertToJsonObject(boardHistory, dbHistory);
	}

	// TODO : testUpdate 와 동일 하다는 결론이면 삭제할것.
	@Test
	public void testPatch() throws JsonProcessingException {
		testUpdateHistory();
	}

	@Test
	public void testDeleteSpecificOne() {
		boardHistory = createBoardHistoryIfNotExists();
		boardMapper.boardDelete(defaultNodePtr.toMap());
		int deletedColCnt = boardHistoryMapper.deleteHistory(defaultNodePtr);

		assertEquals(1, deletedColCnt);

		BoardHistory deletedHistory = null;
		deletedHistory = boardHistoryMapper.getHistory(defaultNodePtr);
		assertNull(deletedHistory);
	}
	
	@Test
	public void testGetChildren() {
		boardHistory = createBoardHistoryIfNotExists();
		boardHistory.setParentNodePtrAndRoot(boardHistory);
		boardHistory.setVersion(boardHistory.getVersion() + 1);
		boardHistoryMapper.createHistory(boardHistory);
		
		List<BoardHistory> children = boardHistoryMapper.getChildren(defaultNodePtr);
		assertNotEquals(0, children.size());
		assertEquals(boardHistory, children.get(0));
	}
	
	@Test
	public void testGetFileCount() {
		BoardHistory boardHistory = null;
		boardHistory = boardHistoryMapper.getHistory(defaultNodePtr);
		if (boardHistory == null) {
			boardHistoryMapper.createHistory(defaultHistory);
			boardHistory = boardHistoryMapper.getHistory(defaultNodePtr);
		}
		assertNotNull(boardHistory);
		int file_id = boardHistory.getFile_id();
		int fileCount = boardHistoryMapper.getFileCount(file_id);
		assertEquals(1, fileCount);	
	}

	@Test
	public void testGetHistoryByRootBoardId() throws JsonProcessingException {
		int relatedCnt = 6;
		List<BoardHistory> createdSameRootList = new ArrayList<>(relatedCnt);
		boardHistory = createBoardHistoryIfNotExists();
		BoardHistory firstEle = boardHistoryMapper.getHistory(boardHistory);
		createdSameRootList.add(firstEle.clone());
		for(int i = 0; i < relatedCnt - 1; i++) {
			boardHistory.setParentNodePtrAndRoot(boardHistory);
			boardHistory.setVersion(boardHistory.getVersion() + 1);
			boardHistoryMapper.createHistory(boardHistory);
			BoardHistory dbEle = boardHistoryMapper.getHistory(boardHistory);
			createdSameRootList.add(dbEle.clone());
		}
		List<BoardHistory> sameRoot = boardHistoryMapper.getHistoryByRootBoardId(boardHistory.getRoot_board_id());
		assertEquals(relatedCnt, sameRoot.size());
		for(int i = 0; i < relatedCnt; i++) {
			BoardHistory createdEle = createdSameRootList.get(i);
			BoardHistory mapperEle = sameRoot.get(i);
			JsonUtils.assertConvertToJsonObject(createdEle, mapperEle);
		}
	}
}
