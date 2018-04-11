package com.worksmobile.assignment.mapper;

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
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.NodePtr;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class BoardMapperTest {

    @Autowired
    private BoardMapper boardMapper;
    
	private final NodePtr defaultNodePtr = new NodePtr(1, 1, 1);
	private Board defaultBoard;
    
    @Before
	public void makeDefaultBoard() {
		defaultBoard = new Board();
		defaultBoard.setNodePtr(defaultNodePtr);
		defaultBoard.setSubject("testSub");
		defaultBoard.setContent("testCont");
    }
    
    @Test
    public void testSelect() throws JsonProcessingException {
		Board vo = boardMapper.viewDetail(defaultNodePtr.toMap());
		assertNotNull(vo);
	}

    @Test
    public void testInsert() throws Exception{
    	
		Board check = boardMapper.viewDetail(defaultBoard.toMap());
    	if(check != null)
    	{
			boardMapper.boardDelete(defaultBoard.toMap());
    	}
		boardMapper.boardCreate(defaultBoard);
			
		Board insertedVO = null;
		insertedVO = boardMapper.viewDetail(defaultBoard.toMap());
		assertEquals(defaultBoard, insertedVO);
	}
    
    @Test
    public void testUpdate() throws Exception{
		Board beforeVO = new Board();
    	beforeVO.setBoard_id(defaultNodePtr.getBoard_id());
    	beforeVO.setVersion(defaultNodePtr.getVersion());
    	beforeVO.setSubject("test1111");
    	beforeVO.setContent("beforeVal");
    	if(null != boardMapper.viewDetail(beforeVO.toMap()))
    	{
    		boardMapper.boardDelete(beforeVO.toMap());
    	}
    	boardMapper.boardCreate(beforeVO);
    	

		Board afterVO = new Board();
    	afterVO.setBoard_id(defaultNodePtr.getBoard_id());
    	afterVO.setVersion(defaultNodePtr.getVersion());
    	afterVO.setSubject("after sub");
    	afterVO.setContent("after con");
    	boardMapper.boardUpdate(afterVO);
    	
		Board updatedVO = null;
    	updatedVO = boardMapper.viewDetail(afterVO.toMap());
    	assertEquals(afterVO, updatedVO);
	}
    
    @Test
    public void testDelete() throws Exception{
		int deletedCnt = boardMapper.boardDelete(defaultBoard.toMap());
    	assertEquals(1, deletedCnt);
    	
		Board deletedVO = null;
		deletedVO = boardMapper.viewDetail(defaultBoard.toMap());
    	assertNull(deletedVO);
	}
    
}
