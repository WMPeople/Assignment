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
import com.worksmobile.Assignment.Domain.BoardDTO;

 
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AssignmentApplication.class)
@WebAppConfiguration
@Transactional
public class boardMapperTest {
	
    @Autowired
    private BoardMapper boardMapper;
    
    private static final int defaultId = 0;
    
    @Test
    public void testMapper() throws Exception{
    	BoardDTO vo = null;
    	vo = boardMapper.getArticle(defaultId);
    	
    	assertNotNull(vo);
    	assertEquals(defaultId, vo.getId());
	}
    
    @Test
    public void testUpdate() throws Exception{
    	BoardDTO beforeVO = new BoardDTO();
    	beforeVO.setId(0);
    	beforeVO.setSubject("test1111");
    	beforeVO.setContent("beforeVAl");
    	boardMapper.boardCreate(beforeVO);
    	
    	BoardDTO afterVO = new BoardDTO();
    	afterVO.setId(0);
    	afterVO.setSubject("after subject");
    	afterVO.setContent("test2222");
    	boardMapper.boardUpdate(afterVO);
    	
    	BoardDTO updatedVO = null;
    	updatedVO = boardMapper.getArticle(afterVO.getId());
    	assertEquals(afterVO, updatedVO);
	}
    
    // TODO : testUpdate 와 동일 하다는 결론이면 삭제할것.
    @Test
    public void testPatch() throws Exception{
    	testUpdate();
	}
    
    @Test
    public void testInsert() throws Exception{
    	BoardDTO vo = new BoardDTO();
    	vo.setId(0);
    	vo.setSubject("testInsert");
    	vo.setContent("testContent");
    	
    	BoardDTO check = boardMapper.getArticle(vo.getId());
    	if(check != null)
    	{
    		boardMapper.boardDelete(vo.getId());
    	}
		boardMapper.boardCreate(vo);
			
		BoardDTO insertedVO = null;
		insertedVO = boardMapper.getArticle(vo.getId());
		assertEquals(vo, insertedVO);
	}
    
    @Test
    public void testDelete() throws Exception{
    	BoardDTO vo = new BoardDTO();
    	vo.setSubject("deleteSubject");
    	vo.setContent("deleteContent");
    	boardMapper.boardCreate(vo);
    	boardMapper.boardDelete(vo.getId());
    	
    	BoardDTO deletedVO = null;
    	deletedVO = boardMapper.getArticle(vo.getId());
    	assertNull(deletedVO);
	}
}