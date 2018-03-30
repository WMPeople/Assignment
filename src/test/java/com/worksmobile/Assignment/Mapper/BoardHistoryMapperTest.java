package com.worksmobile.Assignment.Mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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

	public static final int defaultBoardId = 1;
	private static BoardHistoryDTO defaultHistoryDTO;
	private static NodePtrDTO defaultNodePtrDTO;

	public BoardHistoryMapperTest() {
		defaultNodePtrDTO = new NodePtrDTO(1, 6, 1);
		defaultHistoryDTO = new BoardHistoryDTO();
		defaultHistoryDTO.setBoard_id(1);
		defaultHistoryDTO.setVersion(6);
		defaultHistoryDTO.setBranch(1);

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
	public void testCreateHistory() throws JsonProcessingException {
		BoardDTO article = new BoardDTO();
		article.setBoard_id(defaultBoardId);
		article.setSubject("testInsert");
		article.setContent("testContent");

		BoardHistoryDTO createdHistoryDTO = new BoardHistoryDTO(article, defaultNodePtrDTO);

		BoardHistoryDTO check = boardHistoryMapper.getHistory(defaultNodePtrDTO);
		if (check != null) {
			boardHistoryMapper.deleteHistory(defaultNodePtrDTO);
		}
		boardHistoryMapper.createHistory(createdHistoryDTO);

		BoardHistoryDTO insertedDTO = null;
		insertedDTO = boardHistoryMapper.getHistory(createdHistoryDTO);

		// ingore created time.
		insertedDTO.setCreated(null);
		Utils.assertConvertToJsonObject(createdHistoryDTO, insertedDTO);
	}

	@Test
	public void testUpdateHistory() throws JsonProcessingException {
		BoardHistoryDTO beforeHistoryDTO = null;
		beforeHistoryDTO = boardHistoryMapper.getHistory(defaultNodePtrDTO);
		if (beforeHistoryDTO == null) {
			boardHistoryMapper.createHistory(defaultHistoryDTO);
			beforeHistoryDTO = boardHistoryMapper.getHistory(defaultNodePtrDTO);
		}

		BoardHistoryDTO afterHistoryDTO = beforeHistoryDTO;
		if (beforeHistoryDTO.getParent_version() == null) {
			beforeHistoryDTO.setParent_version(1);
		} else {
			beforeHistoryDTO.setParent_version(null);
		}

		int updateRtn = boardHistoryMapper.updateHistoryParent(afterHistoryDTO);
		assertEquals(1, updateRtn);

		BoardHistoryDTO dbHistoryDTO = boardHistoryMapper.getHistory(defaultNodePtrDTO);
		Utils.assertConvertToJsonObject(afterHistoryDTO, dbHistoryDTO);
		;
	}

	// TODO : testUpdate 와 동일 하다는 결론이면 삭제할것.
	@Test
	public void testPatch() throws JsonProcessingException {
		testUpdateHistory();
	}

	@Test
	public void testDeleteSpecificOne() {
		BoardHistoryDTO boardHistoryDTO = null;
		boardHistoryDTO = boardHistoryMapper.getHistory(defaultNodePtrDTO);

		if (boardHistoryDTO == null) {
			boardHistoryMapper.createHistory(defaultHistoryDTO);
		}
		boardMapper.boardDelete(defaultNodePtrDTO.toMap());
		int deletedColCnt = boardHistoryMapper.deleteHistory(defaultNodePtrDTO);

		assertEquals(1, deletedColCnt);

		BoardHistoryDTO deletedHistoryDTO = null;
		deletedHistoryDTO = boardHistoryMapper.getHistory(defaultNodePtrDTO);
		assertNull(deletedHistoryDTO);
	}
	
	@Test
	public void testGetChildren() {
		BoardHistoryDTO boardHistoryDTO = null;
		boardHistoryDTO = boardHistoryMapper.getHistory(defaultNodePtrDTO);

		if (boardHistoryDTO == null) {
			boardHistoryMapper.createHistory(defaultHistoryDTO);
		}
		
		// TODO : 미완성
	}
}