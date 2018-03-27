package com.worksmobile.Assignment.Mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.worksmobile.Assignment.AssignmentApplication;
import com.worksmobile.Assignment.Domain.BoardDTO;

 
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AssignmentApplication.class)
@WebAppConfiguration
@Transactional
public class BoardMapperTest {
	
    @Autowired
    private BoardMapper boardMapper;
    
    private static final int defaultId = 0;
    
    @Test
    @Ignore("미완성.")
    public void testMapper() throws Exception{
    	BoardDTO vo = null;
    	vo = boardMapper.viewDetail(defaultId);
    	
    	assertNotNull(vo);
    	assertEquals(defaultId, vo.getBoard_id());
	}
    
    @Test
    @Ignore("미완성.")
    public void testUpdate() throws Exception{
    	BoardDTO beforeVO = new BoardDTO();
    	beforeVO.setBoard_id(0);
    	beforeVO.setSubject("test1111");
    	beforeVO.setContent("beforeVAl");
    	if(null != boardMapper.viewDetail(beforeVO.getBoard_id()))
    	{
    		boardMapper.boardDelete(beforeVO.getBoard_id());
    	}
    	boardMapper.boardCreate(beforeVO);
    	
    	BoardDTO afterVO = new BoardDTO();
    	afterVO.setBoard_id(0);
    	afterVO.setSubject("after subject");
    	afterVO.setContent("test2222");
    	boardMapper.boardUpdate(afterVO);
    	
    	BoardDTO updatedVO = null;
    	updatedVO = boardMapper.viewDetail(afterVO.getBoard_id());
    	assertEquals(afterVO, updatedVO);
	}
    
    // TODO : testUpdate 와 동일 하다는 결론이면 삭제할것.
    @Test
    @Ignore("미완성.")
    public void testPatch() throws Exception{
    	testUpdate();
	}
    
    @Test
    @Ignore("미완성.")
    public void testInsert() throws Exception{
    	BoardDTO vo = new BoardDTO();
    	vo.setBoard_id(0);
    	vo.setSubject("testInsert");
    	vo.setContent("testContent");
    	
    	BoardDTO check = boardMapper.viewDetail(vo.getBoard_id());
    	if(check != null)
    	{
    		boardMapper.boardDelete(vo.getBoard_id());
    	}
		boardMapper.boardCreate(vo);
			
		BoardDTO insertedVO = null;
		insertedVO = boardMapper.viewDetail(vo.getBoard_id());
		assertEquals(vo, insertedVO);
	}
    
    @Test
    @Ignore("미완성.")
    public void testDelete() throws Exception{
    	BoardDTO vo = new BoardDTO();
    	vo.setSubject("deleteSubject");
    	vo.setContent("deleteContent");
    	boardMapper.boardCreate(vo);
    	boardMapper.boardDelete(vo.getBoard_id());
    	
    	BoardDTO deletedVO = null;
    	deletedVO = boardMapper.viewDetail(vo.getBoard_id());
    	assertNull(deletedVO);
	}
}