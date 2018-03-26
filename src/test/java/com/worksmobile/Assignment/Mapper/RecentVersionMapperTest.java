package com.worksmobile.Assignment.Mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.after;

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
import com.worksmobile.Assignment.Domain.RecentVersionDTO;
import com.worksmobile.Assignment.Domain.RecentVersionDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AssignmentApplication.class)
@WebAppConfiguration
@Transactional
public class RecentVersionMapperTest {
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;
	
	@Autowired
	RecnetVersionMapper recentVersionMapper;
	
	private static final int defaultHistoryId = 0;
	private static RecentVersionDTO defaultRecentVersionDTO;
	
	public RecentVersionMapperTest() {
		defaultRecentVersionDTO = new RecentVersionDTO();
		defaultRecentVersionDTO.setBoard_history_id(defaultHistoryId);
		defaultRecentVersionDTO.setVersion(1);
		defaultRecentVersionDTO.setBranch_id(0);
	}
	
	@Test
	public void testGetRecentVersion()
	{
		RecentVersionDTO recentVerionDTO = null;
		recentVerionDTO = recentVersionMapper.getRecentVersion(defaultHistoryId);
		
		if(recentVerionDTO == null)
		{
			recentVersionMapper.createRecentVersion(defaultRecentVersionDTO);
			recentVerionDTO = recentVersionMapper.getRecentVersion(defaultHistoryId);
		}
		
		assertNotNull(recentVerionDTO);
	}
	
    @Test
    public void testCreateRecentVersion() throws Exception{
    	RecentVersionDTO check = recentVersionMapper.getRecentVersion(defaultHistoryId);
    	
    	if(check != null)
    	{
    		recentVersionMapper.deleteRecentVersion(defaultHistoryId);
    	}
		recentVersionMapper.createRecentVersion(defaultRecentVersionDTO);
			
		RecentVersionDTO insertedDTO = null;
		insertedDTO = recentVersionMapper.getRecentVersion(defaultHistoryId);
		
		assertEquals(defaultRecentVersionDTO, insertedDTO);
	}
    
    @Test
    public void testUpdateRecentVersion() throws Exception{
    	RecentVersionDTO beforeDTO = null;
    	beforeDTO = recentVersionMapper.getRecentVersion(defaultHistoryId);
    	if(beforeDTO == null)
    	{
    		recentVersionMapper.createRecentVersion(defaultRecentVersionDTO);
    	}
    	
    	RecentVersionDTO afterDTO = beforeDTO; 
    	if(beforeDTO.getVersion() == 1)
    	{
    		afterDTO.setVersion(2);
    	}
    	else
    	{
    		afterDTO.setVersion(1);
    	}

    	BoardHistoryDTO boardHistoryDTO = null;
    	boardHistoryDTO = boardHistoryMapper.getHistoryBySpecificOne(
    			afterDTO.getBoard_history_id(),
    			afterDTO.getVersion(),
    			afterDTO.getBranch_id()
    			);

    	if(boardHistoryDTO == null)
    	{
    		boardHistoryDTO = new BoardHistoryDTO();
    		boardHistoryDTO.setBoard_history_id(defaultHistoryId);
    		boardHistoryDTO.setVersion(afterDTO.getVersion());
    		boardHistoryDTO.setBranch_id(afterDTO.getBranch_id());
    		boardHistoryDTO.setBoard_id(1);
    		
    		boardHistoryMapper.createHistory(boardHistoryDTO);
    	}
    	
    	int updateRtn = recentVersionMapper.updateRecentVersion(afterDTO);
    	assertEquals(1, updateRtn);
    	
    	RecentVersionDTO dbDTO = recentVersionMapper.getRecentVersion(defaultHistoryId);
    	assertEquals(afterDTO, dbDTO);
	}
    
    // TODO : testUpdate 와 동일 하다는 결론이면 삭제할것.
    @Test
    public void testPatch() throws Exception{
    	testUpdateRecentVersion();
	}
    
    @Test
    public void testDeleteRecentVersion() throws Exception{
    	RecentVersionDTO recentVersionDTO = null;
    	recentVersionDTO = recentVersionMapper.getRecentVersion(defaultHistoryId);
    	
    	if(recentVersionDTO == null)
    	{
    		recentVersionMapper.createRecentVersion(defaultRecentVersionDTO);
    	}
    	int deletedColCnt = recentVersionMapper.deleteRecentVersion(defaultHistoryId);
    	
    	assertEquals(1, deletedColCnt);
    	
    	RecentVersionDTO deletedDTO = null;
    	deletedDTO = recentVersionMapper.getRecentVersion(defaultHistoryId);
    	assertNull(deletedDTO);
	}
}
