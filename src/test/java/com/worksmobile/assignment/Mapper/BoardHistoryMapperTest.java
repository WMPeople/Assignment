package com.worksmobile.assignment.Mapper;

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
import com.worksmobile.Assignment.AssignmentApplication;
import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.BoardHistoryDTO;
import com.worksmobile.Assignment.Domain.NodePtrDTO;
import com.worksmobile.Assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.Assignment.Mapper.BoardMapper;
import com.worksmobile.Assignment.Mapper.FileMapper;
import com.worksmobile.Assignment.Service.Compress;
import com.worksmobile.Assignment.util.Utils;

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

	private static BoardHistoryDTO defaultHistoryDTO;
	private static NodePtrDTO defaultNodePtrDTO;
	private BoardHistoryDTO boardHistoryDTO = null;

	public BoardHistoryMapperTest() {
		defaultNodePtrDTO = new NodePtrDTO(1000, 6, 1);
		defaultHistoryDTO = new BoardHistoryDTO();
		defaultHistoryDTO.setBoard_id(defaultNodePtrDTO.getBoard_id());
		defaultHistoryDTO.setVersion(defaultNodePtrDTO.getVersion());
		defaultHistoryDTO.setFile_id(1000);

		defaultHistoryDTO.setStatus("Created");
		defaultHistoryDTO.setHistory_subject("sub");
	}

	@Test
	public void testGetHistory() {
		BoardHistoryDTO historyDTO = null;
		historyDTO = boardHistoryMapper.getHistory(defaultNodePtrDTO);
		if (historyDTO == null) {
			boardHistoryMapper.createHistory(defaultHistoryDTO);
		}

		historyDTO = boardHistoryMapper.getHistory(defaultNodePtrDTO);

		assertNotNull(historyDTO);
	}
	
	@Test
	public void testCreateHistory() throws IOException {
		BoardDTO article = new BoardDTO();
		article.setBoard_id(defaultNodePtrDTO.getBoard_id());
		article.setSubject("testInsert");
		article.setContent("testContent");

		BoardHistoryDTO createdHistoryDTO = new BoardHistoryDTO(article, defaultNodePtrDTO, BoardHistoryDTO.STATUS_CREATED);
		createdHistoryDTO.setHistory_content(Compress.compressArticleContent(article));

		BoardHistoryDTO check = boardHistoryMapper.getHistory(defaultNodePtrDTO);
		if (check != null) {
			boardHistoryMapper.deleteHistory(defaultNodePtrDTO);
		}
		int createdCnt = boardHistoryMapper.createHistory(createdHistoryDTO);
		assertEquals(1, createdCnt);

		BoardHistoryDTO insertedDTO = null;
		insertedDTO = boardHistoryMapper.getHistory(createdHistoryDTO);

		Utils.assertConvertToJsonObject(createdHistoryDTO, insertedDTO);
	}
	
	private  BoardHistoryDTO createBoardHistoryIfNotExists() {
		boardHistoryDTO = boardHistoryMapper.getHistory(defaultNodePtrDTO);
		if (boardHistoryDTO == null) {
			defaultHistoryDTO.setRoot_board_id(defaultHistoryDTO.getBoard_id());
			int createdCnt = boardHistoryMapper.createHistory(defaultHistoryDTO);
			assertEquals(1, createdCnt);
			boardHistoryDTO = boardHistoryMapper.getHistory(defaultNodePtrDTO);
		}
		assertNotNull(boardHistoryDTO);
		return boardHistoryDTO;
	}

	@Test
	public void testUpdateHistory() throws JsonProcessingException {
		boardHistoryDTO = createBoardHistoryIfNotExists();
		
		if (boardHistoryDTO.getParent_version() != null) {
			boardHistoryDTO.setParent_version(null);
		} else {
			boardHistoryDTO.setParent_version(1);
		}
		if(boardHistoryDTO.getRoot_board_id() != 1) {
			boardHistoryDTO.setRoot_board_id(1);
		} else {
			boardHistoryDTO.setRoot_board_id(0);
		}

		int updateRtn = boardHistoryMapper.updateHistoryParentAndRoot(boardHistoryDTO);
		assertEquals(1, updateRtn);

		BoardHistoryDTO dbHistoryDTO = boardHistoryMapper.getHistory(defaultNodePtrDTO);
		Utils.assertConvertToJsonObject(boardHistoryDTO, dbHistoryDTO);
	}

	// TODO : testUpdate 와 동일 하다는 결론이면 삭제할것.
	@Test
	public void testPatch() throws JsonProcessingException {
		testUpdateHistory();
	}

	@Test
	public void testDeleteSpecificOne() {
		boardHistoryDTO = createBoardHistoryIfNotExists();
		boardMapper.boardDelete(defaultNodePtrDTO.toMap());
		int deletedColCnt = boardHistoryMapper.deleteHistory(defaultNodePtrDTO);

		assertEquals(1, deletedColCnt);

		BoardHistoryDTO deletedHistoryDTO = null;
		deletedHistoryDTO = boardHistoryMapper.getHistory(defaultNodePtrDTO);
		assertNull(deletedHistoryDTO);
	}
	
	@Test
	public void testGetChildren() {
		boardHistoryDTO = createBoardHistoryIfNotExists();
		boardHistoryDTO.setParentNodePtrAndRoot(boardHistoryDTO);
		boardHistoryDTO.setVersion(boardHistoryDTO.getVersion() + 1);
		boardHistoryMapper.createHistory(boardHistoryDTO);
		
		List<BoardHistoryDTO> children = boardHistoryMapper.getChildren(defaultNodePtrDTO);
		assertNotEquals(0, children.size());
		assertEquals(boardHistoryDTO, children.get(0));
	}
	
	@Test
	public void testGetFileCount() {
		BoardHistoryDTO boardHistoryDTO = null;
		boardHistoryDTO = boardHistoryMapper.getHistory(defaultNodePtrDTO);
		if (boardHistoryDTO == null) {
			boardHistoryMapper.createHistory(defaultHistoryDTO);
			boardHistoryDTO = boardHistoryMapper.getHistory(defaultNodePtrDTO);
		}
		assertNotNull(boardHistoryDTO);
		int file_id = boardHistoryDTO.getFile_id();
		int fileCount = boardHistoryMapper.getFileCount(file_id);
		assertEquals(1, fileCount);	
	}

	@Test
	public void testGetHistoryByRootBoardId() throws JsonProcessingException {
		int relatedCnt = 6;
		List<BoardHistoryDTO> createdSameRootList = new ArrayList<>(relatedCnt);
		boardHistoryDTO = createBoardHistoryIfNotExists();
		BoardHistoryDTO firstEle = boardHistoryMapper.getHistory(boardHistoryDTO);
		createdSameRootList.add(firstEle.clone());
		for(int i = 0; i < relatedCnt - 1; i++) {
			boardHistoryDTO.setParentNodePtrAndRoot(boardHistoryDTO);
			boardHistoryDTO.setVersion(boardHistoryDTO.getVersion() + 1);
			boardHistoryMapper.createHistory(boardHistoryDTO);
			BoardHistoryDTO dbEle = boardHistoryMapper.getHistory(boardHistoryDTO);
			createdSameRootList.add(dbEle.clone());
		}
		List<BoardHistoryDTO> sameRoot = boardHistoryMapper.getHistoryByRootBoardId(boardHistoryDTO.getRoot_board_id());
		assertEquals(relatedCnt, sameRoot.size());
		for(int i = 0; i < relatedCnt; i++) {
			BoardHistoryDTO createdEle = createdSameRootList.get(i);
			BoardHistoryDTO mapperEle = sameRoot.get(i);
			Utils.assertConvertToJsonObject(createdEle, mapperEle);
		}
	}
}
