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

import com.worksmobile.Assignment.AssignmentApplication;
import com.worksmobile.Assignment.Domain.BranchDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AssignmentApplication.class)
@WebAppConfiguration
@Transactional
public class BranchMapperTest {
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;
	
	@Autowired
	BranchMapper branchMapper;
	
	public static final int defaultBranchId = 0;
	private static BranchDTO defaultBranchDTO;
	
	public BranchMapperTest() {
		defaultBranchDTO = new BranchDTO();
		defaultBranchDTO.setBranch_id(defaultBranchId);
		defaultBranchDTO.setBefore_version(1);
		defaultBranchDTO.setCur_version(1);
		defaultBranchDTO.setHistory_id(BoardHistoryMapperTest.defaultHistoryId);
	}
	
	@Test
	public void testGetBranch()
	{
		BranchDTO recentVerionDTO = null;
		recentVerionDTO = branchMapper.getBranch(defaultBranchId);
		
		if(recentVerionDTO == null)
		{
			branchMapper.createBranch(defaultBranchDTO);
			recentVerionDTO = branchMapper.getBranch(defaultBranchId);
		}
		
		assertNotNull(recentVerionDTO);
	}
	
    @Test
    public void testCreateBranch() throws Exception{
    	BranchDTO check = branchMapper.getBranch(defaultBranchId);
    	
    	if(check != null)
    	{
    		branchMapper.deleteBranch(defaultBranchDTO);
    	}
		branchMapper.createBranch(defaultBranchDTO);
			
		BranchDTO insertedDTO = null;
		insertedDTO = branchMapper.getBranch(defaultBranchId);
		
		assertEquals(defaultBranchDTO, insertedDTO);
	}
    
    @Test
    public void testCreateBranchWithNextVal() throws Exception{
    	BranchDTO check = branchMapper.getBranch(defaultBranchId);
    	
    	if(check != null)
    	{
    		branchMapper.deleteBranch(defaultBranchDTO);
    	}
    	BranchDTO createDTO = defaultBranchDTO;
    	createDTO.setNext_version(2);
		branchMapper.createBranch(createDTO);
			
		BranchDTO insertedDTO = null;
		insertedDTO = branchMapper.getBranch(defaultBranchId);
		
		assertEquals(createDTO, insertedDTO);
    }
    
    @Test
    public void testUpdateBranch() throws Exception{
    	BranchDTO beforeDTO = null;
    	beforeDTO = branchMapper.getBranch(defaultBranchId);
    	if(beforeDTO == null)
    	{
    		branchMapper.createBranch(defaultBranchDTO);
    	}
    	
    	BranchDTO afterDTO = beforeDTO; 
    	if(beforeDTO.getNext_version() == 0)
    	{
    		afterDTO.setNext_version(1);
    	}
    	else
    	{
    		afterDTO.setNext_version(0);
    	}

    	int updateRtn = branchMapper.updateBranch(afterDTO);
    	assertEquals(1, updateRtn);
    	
    	BranchDTO dbDTO = branchMapper.getBranch(defaultBranchId);
    	assertEquals(afterDTO, dbDTO);
	}
    
    // TODO : testUpdate 와 동일 하다는 결론이면 삭제할것.
    @Test
    public void testPatch() throws Exception{
    	testUpdateBranch();
	}
    
    @Test
    public void testDeleteBranch() throws Exception{
    	BranchDTO BranchDTO = null;
    	BranchDTO = branchMapper.getBranch(defaultBranchId);
    	
    	if(BranchDTO == null)
    	{
    		branchMapper.createBranch(defaultBranchDTO);
    	}
    	int deletedColCnt = branchMapper.deleteBranch(defaultBranchDTO);
    	
    	assertEquals(1, deletedColCnt);
    	
    	BranchDTO deletedDTO = null;
    	deletedDTO = branchMapper.getBranch(defaultBranchId);
    	assertNull(deletedDTO);
	}
    
    @Test
    public void testDeleteBranchWithNextVal() throws Exception{
    	BranchDTO createDTO = defaultBranchDTO;
    	createDTO.setNext_version(2);

    	BranchDTO BranchDTO = null;
    	BranchDTO = branchMapper.getBranch(defaultBranchId);
    	
    	if(BranchDTO != null)
    	{
    		branchMapper.updateBranch(createDTO);
    	}
    	else
    	{
    		branchMapper.createBranch(createDTO);
    	}
    	int deletedColCnt = branchMapper.deleteBranch(createDTO);
    	
    	assertEquals(1, deletedColCnt);
    	
    	BranchDTO deletedDTO = null;
    	deletedDTO = branchMapper.getBranch(defaultBranchId);
    	assertNull(deletedDTO);
    }
}
