package com.worksmobile.Assignment.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.BoardHistoryDTO;
import com.worksmobile.Assignment.Domain.RecentVersionDTO;
import com.worksmobile.Assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.Assignment.Mapper.BoardMapper;
import com.worksmobile.Assignment.Mapper.RecnetVersionMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VersionManagementTest {

	@Autowired
	VersionManagementService versionManagementService;
	
	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;

	@Autowired
	RecnetVersionMapper recnetVersionMapper;
	
	private boolean compareBoardDTO(BoardDTO lrs, BoardDTO rhs) {
		return	lrs.getSubject().equals(rhs.getSubject()) &&
				lrs.getContent().equals(rhs.getContent()) &&
				lrs.getAttachment().equals(rhs.getAttachment()) &&
				lrs.getFileName().equals(rhs.getFileName()) &&
				lrs.getFileSize() == rhs.getFileSize() &&
				lrs.getBoard_id() == rhs.getBoard_id();
	}
	
	@Test
	public void canCreate() {
		versionManagementService = new VersionManagementService();
	}
	
	@Test
	public void createArticleTest() throws Exception {
		BoardDTO article = new BoardDTO();
		article.setSubject("versionTestSub");
		article.setContent("versionTestCont");
		
		int board_id = versionManagementService.createArticle(article);
		article.setBoard_id(board_id);
		
		BoardHistoryDTO expectHistoryDTO = new BoardHistoryDTO(article);
		RecentVersionDTO expectRecentVerDTO = new RecentVersionDTO(expectHistoryDTO);
		
		BoardDTO dbBoardDTO = boardMapper.viewDetail(board_id);
		BoardHistoryDTO dbHistoryDTO = boardHistoryMapper.getHistoryBySpecificOne(
				expectHistoryDTO.getBoard_history_id(), 
				expectHistoryDTO.getVersion(),
				expectHistoryDTO.getBranch_id());
		RecentVersionDTO dbRecentVerDTO = recnetVersionMapper.getRecentVersion(board_id);
		
		assertTrue(compareBoardDTO(dbBoardDTO, article));
		assertEquals(expectHistoryDTO, dbHistoryDTO);
		assertEquals(expectRecentVerDTO, dbRecentVerDTO);
	}
	
}
