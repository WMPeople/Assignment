package com.worksmobile.Assignment.Service;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.BoardHistoryDTO;
import com.worksmobile.Assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.Assignment.Mapper.BoardMapper;

public class VersionManagementService {

	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;
	
	// TODO : Transaction확인
	public void createArticle(BoardDTO article) {
		BoardHistoryDTO boardHistoryDTO = new BoardHistoryDTO(article);
		
		boardMapper.boardCreate(article);
		boardHistoryMapper.createHistory(boardHistoryDTO);
	}
	
	// DB : 이력 id(그냥 num), 게시글 id, 버전, 
	public void recoverVersion(int boardHistoryId, int version, int branchId)
	{
		BoardDTO recoverArticleDTO = null;
		BoardHistoryDTO boardHistoryDTO = null;
		boardHistoryDTO = boardHistoryMapper.getHistoryBySpecificOne(boardHistoryId, version, branchId);
		if(boardHistoryDTO == null)
		{
			throw new RuntimeException("게시글 이력이 존재하지 않습니다.");
		}
		
		//boardHistoryDTO.setBoard_id(recoverArticleId);
		boardHistoryDTO.setStatus("RecoveredFrom" + boardHistoryId);
	}

	public void updateVersion(int boardHistoryId, int beforeArticleId, BoardDTO article)
	{
		BoardHistoryDTO boardHistoryDTO = null;
		//boardHistoryDTO = boardHistoryMapper.getHistoryByHistoryId(boardHistoryId);
		if(boardHistoryDTO == null)
		{
			throw new RuntimeException("게시글 이력이 존재하지 않습니다.");
		}
		boardHistoryDTO.setBoard_id(article.getBoard_id());
		// TODO : 충돌관리
		// 이전 버전에서 복원 하는 것과는 다름.
		boardHistoryDTO.setStatus("Modified");
	}
	
	public void deleteVersion(int boardHistoryId)
	{
		
	}
}
