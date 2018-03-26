package com.worksmobile.Assignment.Mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.worksmobile.Assignment.AssignmentApplication;
import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.BoardHistoryDTO;
import com.worksmobile.Assignment.Domain.BranchDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AssignmentApplication.class)
@WebAppConfiguration
@Transactional
public class BoardHistoryMapperTest {
	
	@Autowired
	RecnetVersionMapper recnetVersionMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;
	
	@Autowired
	BranchMapper branchMapper;
	
	public static final int defaultBoardId = 1;
	public static final int defaultHistoryId = 0;
	private static BoardHistoryDTO defaultHistoryDTO;
	
	public BoardHistoryMapperTest() {
		defaultHistoryDTO = new BoardHistoryDTO();
		defaultHistoryDTO.setBoard_history_id(defaultHistoryId);
		defaultHistoryDTO.setVersion(1);
		defaultHistoryDTO.setBranch_id(0);
		defaultHistoryDTO.setStatus("Created");
		defaultHistoryDTO.setBoard_id(defaultBoardId);
	}
	
	private BoardHistoryDTO mapperGetHistoryDTO(BoardHistoryDTO queryHistoryDTO)
	{
		BoardHistoryDTO historyDTO = null;
		historyDTO = boardHistoryMapper.getHistoryBySpecificOne(
				queryHistoryDTO.getBoard_history_id(),
				queryHistoryDTO.getVersion(),
				queryHistoryDTO.getBranch_id()
				);
		
		return historyDTO;
	}
	
	private void testHistoryDTOSimpleEquals(BoardHistoryDTO lhs, BoardHistoryDTO rhs)
	{
		assertEquals(lhs.getBoard_history_id(), rhs.getBoard_history_id());
		assertEquals(lhs.getBoard_id(), rhs.getBoard_id());
		assertEquals(lhs.getBranch_id(), rhs.getBranch_id());
	}
	
	@Test
	public void testGetHistoryByBoardId()
	{
		List<BoardHistoryDTO> historyDTOList = null;
		historyDTOList = boardHistoryMapper.getHistoryByBoardId(defaultBoardId);
		assertNotNull(historyDTOList);
		
		if(historyDTOList.size() == 0)
		{
			boardHistoryMapper.createHistory(defaultHistoryDTO);
			historyDTOList = boardHistoryMapper.getHistoryByBoardId(defaultBoardId);
		}
		
		assertNotEquals(0, historyDTOList.size());
	}
	
	@Test
	public void testGetHistoryByHistoryId()
	{
		List<BoardHistoryDTO> historyDTOList = null;
		historyDTOList = boardHistoryMapper.getHistoryByHistoryId(defaultHistoryId);
		assertNotNull(historyDTOList);
		
		if(historyDTOList.size() == 0)
		{
			boardHistoryMapper.createHistory(defaultHistoryDTO);
			historyDTOList = boardHistoryMapper.getHistoryByHistoryId(defaultHistoryId);
		}
		
		assertNotEquals(0, historyDTOList.size());	
	}
	
	@Test
	public void testGetHistoryBySpecificOne()
	{
		BoardHistoryDTO historyDTO = null;
		historyDTO = mapperGetHistoryDTO(defaultHistoryDTO);
		if(historyDTO == null)
		{
			boardHistoryMapper.createHistory(defaultHistoryDTO);
		}

		historyDTO = mapperGetHistoryDTO(defaultHistoryDTO);
		
		assertNotNull(historyDTO);
	}

    @Test
    public void testCreateHistory() throws Exception{
    	BoardDTO article = new BoardDTO();
    	article.setBoard_id(defaultBoardId);
    	article.setSubject("testInsert");
    	article.setContent("testContent");
    	
    	BoardHistoryDTO createdHistoryDTO = new BoardHistoryDTO(article);
    	
    	BoardHistoryDTO check = mapperGetHistoryDTO(createdHistoryDTO);
    	
    	if(check != null)
    	{
    		boardHistoryMapper.deleteHistoryByHistoryId(article.getBoard_id());
    	}
		boardHistoryMapper.createHistory(createdHistoryDTO);
			
		BoardHistoryDTO insertedDTO = null;
		insertedDTO = mapperGetHistoryDTO(createdHistoryDTO);
		
		
		testHistoryDTOSimpleEquals(createdHistoryDTO, insertedDTO);
	}
    
    @Test
    public void testUpdateHistory() throws Exception{
    	BoardHistoryDTO beforeHistoryDTO = null;
    	beforeHistoryDTO = mapperGetHistoryDTO(defaultHistoryDTO);
    	if(beforeHistoryDTO == null)
    	{
    		boardHistoryMapper.createHistory(defaultHistoryDTO);
    	}
    	
    	BoardHistoryDTO afterHistoryDTO = beforeHistoryDTO; 
    	if(beforeHistoryDTO.getStatus().equals("Created"))
    	{
    		afterHistoryDTO.setStatus("Modified");
    	}
    	else
    	{
    		afterHistoryDTO.setStatus("Created");
    	}
    	
    	int updateRtn = boardHistoryMapper.updateHistory(afterHistoryDTO);
    	assertEquals(1, updateRtn);
    	
    	BoardHistoryDTO dbHistoryDTO = mapperGetHistoryDTO(defaultHistoryDTO);
    	testHistoryDTOSimpleEquals(afterHistoryDTO, dbHistoryDTO);
	}
    
    // TODO : testUpdate 와 동일 하다는 결론이면 삭제할것.
    @Test
    public void testPatch() throws Exception{
    	testUpdateHistory();
	}
    
    @Test
    public void testDeleteSpecificOne() throws Exception{
    	BoardHistoryDTO boardHistoryDTO = null;
    	boardHistoryDTO = mapperGetHistoryDTO(defaultHistoryDTO);
    	
    	if(boardHistoryDTO == null)
    	{
    		boardHistoryMapper.createHistory(defaultHistoryDTO);
    	}
    	recnetVersionMapper.deleteRecentVersion(defaultHistoryId);
    	BranchDTO branchDTO = branchMapper.getBranch(boardHistoryDTO.getBranch_id());
    	branchMapper.deleteBranch(branchDTO);
    	
    	int deletedColCnt = boardHistoryMapper.deleteHistoryBySpecificOne(boardHistoryDTO.getBoard_history_id(),
    			boardHistoryDTO.getVersion(), boardHistoryDTO.getBranch_id());
    	
    	assertEquals(1, deletedColCnt);
    	
    	BoardHistoryDTO deletedHistoryDTO = null;
    	deletedHistoryDTO = mapperGetHistoryDTO(defaultHistoryDTO);
    	assertNull(deletedHistoryDTO);
	}
}
