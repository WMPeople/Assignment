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

/**
 * 게시글 테이블에 대한 매퍼 테스트입니다.
 * @author khh
 *
 */
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
			boardMapper.deleteBoard(defaultBoard.toMap());
    	}
		boardMapper.createBoard(defaultBoard);
			
		Board insertedVO = null;
		insertedVO = boardMapper.viewDetail(defaultBoard.toMap());
		assertEquals(defaultBoard, insertedVO);
	}
    
    @Test
    public void testDelete() throws Exception{
		int deletedCnt = boardMapper.deleteBoard(defaultBoard.toMap());
    	assertEquals(1, deletedCnt);
    	
		Board deletedVO = null;
		deletedVO = boardMapper.viewDetail(defaultBoard.toMap());
    	assertNull(deletedVO);
	}
    
}
