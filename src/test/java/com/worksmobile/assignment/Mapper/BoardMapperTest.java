package com.worksmobile.Assignment.Mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.NodePtrDTO;
import com.worksmobile.Assignment.Mapper.BoardMapper;
import com.worksmobile.Assignment.util.Utils;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class BoardMapperTest {

    @Autowired
    private BoardMapper boardMapper;
    
    private final NodePtrDTO defaultNodePtr = new NodePtrDTO(1, 1, 1);
    private BoardDTO defaultBoardDTO;
    
    @Before
    public void makeDefaultBoardDTO() {
    	defaultBoardDTO = new BoardDTO();
    	defaultBoardDTO.setNodePtrDTO(defaultNodePtr);
    	defaultBoardDTO.setSubject("testSub");
    	defaultBoardDTO.setContent("testCont");
    }
    
    @Test
    public void testSelect() throws JsonProcessingException {
    	BoardDTO vo = null;
    	vo = boardMapper.viewDetail(defaultNodePtr.toMap());
	}

    @Test
    public void testInsert() throws Exception{
    	
    	BoardDTO check = boardMapper.viewDetail(defaultBoardDTO.toMap());
    	if(check != null)
    	{
    		boardMapper.boardDelete(defaultBoardDTO.toMap());
    	}
		boardMapper.boardCreate(defaultBoardDTO);
			
		BoardDTO insertedVO = null;
		insertedVO = boardMapper.viewDetail(defaultBoardDTO.toMap());
		assertEquals(defaultBoardDTO, insertedVO);
	}
    
    @Test
    public void testUpdate() throws Exception{
    	BoardDTO beforeVO = new BoardDTO();
    	beforeVO.setBoard_id(defaultNodePtr.getBoard_id());
    	beforeVO.setVersion(defaultNodePtr.getVersion());
    	beforeVO.setSubject("test1111");
    	beforeVO.setContent("beforeVal");
    	if(null != boardMapper.viewDetail(beforeVO.toMap()))
    	{
    		boardMapper.boardDelete(beforeVO.toMap());
    	}
    	boardMapper.boardCreate(beforeVO);
    	

    	BoardDTO afterVO = new BoardDTO();
    	afterVO.setBoard_id(defaultNodePtr.getBoard_id());
    	afterVO.setVersion(defaultNodePtr.getVersion());
    	afterVO.setSubject("after sub");
    	afterVO.setContent("after con");
    	boardMapper.boardUpdate(afterVO.toMap());
    	
    	BoardDTO updatedVO = null;
    	updatedVO = boardMapper.viewDetail(afterVO.toMap());
    	assertEquals(afterVO, updatedVO);
	}
    
    @Test
    public void testDelete() throws Exception{
    	int deletedCnt = boardMapper.boardDelete(defaultBoardDTO.toMap());
    	assertEquals(1, deletedCnt);
    	
    	BoardDTO deletedVO = null;
    	deletedVO = boardMapper.viewDetail(defaultBoardDTO.toMap());
    	assertNull(deletedVO);
	}
    
}
