package com.worksmobile.Assignment.Mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.NodePtrDTO;

public class BoardTempMapperTest {

    @Autowired
    private BoardTempMapper boardTempMapper;
    
    private final NodePtrDTO defaultNodePtr = new NodePtrDTO(1, 1, 1);
    private BoardDTO defaultBoardDTO;
    
    @Before
    public void makeDefaultBoardDTO() {
    	defaultBoardDTO = new BoardDTO();
    	defaultBoardDTO.setBoard_id(defaultNodePtr.getBoard_id());
    	defaultBoardDTO.setSubject("testSub");
    	defaultBoardDTO.setContent("testCont");
    }
    
    @Test
    public void testSelect() {
    	BoardDTO vo = null;
    	vo = boardTempMapper.viewDetail(defaultNodePtr.toMap());
    	
    	assertNotNull(vo);
    	assertEquals(defaultNodePtr, vo);
	}

    @Test
    public void testInsert() throws Exception{
    	
    	BoardDTO check = boardTempMapper.viewDetail(defaultBoardDTO.toMap());
    	if(check != null)
    	{
    		boardTempMapper.boardDelete(defaultBoardDTO.toMap());
    	}
		boardTempMapper.boardCreate(defaultBoardDTO);
			
		BoardDTO insertedVO = null;
		insertedVO = boardTempMapper.viewDetail(defaultBoardDTO.toMap());
		assertEquals(defaultBoardDTO, insertedVO);
	}
    
    @Test
    public void testUpdate() throws Exception{
    	BoardDTO beforeVO = new BoardDTO();
    	beforeVO.setBoard_id(defaultNodePtr.getBoard_id());
    	beforeVO.setVersion(defaultNodePtr.getVersion());
    	beforeVO.setBranch(defaultNodePtr.getBranch());
    	beforeVO.setSubject("test1111");
    	beforeVO.setContent("beforeVal");
    	if(null != boardTempMapper.viewDetail(beforeVO.toMap()))
    	{
    		boardTempMapper.boardDelete(beforeVO.toMap());
    	}
    	boardTempMapper.boardCreate(beforeVO);
    	

    	BoardDTO afterVO = new BoardDTO();
    	afterVO.setBoard_id(defaultNodePtr.getBoard_id());
    	afterVO.setVersion(defaultNodePtr.getVersion());
    	afterVO.setBranch(defaultNodePtr.getBranch());
    	afterVO.setSubject("after sub");
    	afterVO.setContent("after con");
    	boardTempMapper.boardUpdate(afterVO);
    	
    	BoardDTO updatedVO = null;
    	updatedVO = boardTempMapper.viewDetail(afterVO.toMap());
    	assertEquals(afterVO, updatedVO);
	}
    
    @Test
    public void testDelete() throws Exception{
    	BoardDTO vo = new BoardDTO();
    	vo.setBoard_id(defaultNodePtr.getBoard_id());
    	vo.setVersion(defaultNodePtr.getVersion());
    	vo.setBranch(defaultNodePtr.getBranch());
    	vo.setSubject("delete sub");
    	vo.setContent("delete con");
    	boardTempMapper.boardCreate(vo);
    	boardTempMapper.boardDelete(vo.toMap());
    	
    	BoardDTO deletedVO = null;
    	deletedVO = boardTempMapper.viewDetail(vo.toMap());
    	assertNull(deletedVO);
	}
    
}
