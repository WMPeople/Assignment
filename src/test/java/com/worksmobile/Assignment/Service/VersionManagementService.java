package com.worksmobile.Assignment.Service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.BoardHistoryDTO;
import com.worksmobile.Assignment.Domain.RecentVersionDTO;
import com.worksmobile.Assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.Assignment.Mapper.BoardMapper;
import com.worksmobile.Assignment.Mapper.BranchMapper;
import com.worksmobile.Assignment.Mapper.RecnetVersionMapper;

/***************History Id 을 어떻게 부여 해야 할지?************/
// start transactional
// 1. Group By 로 묶어서 id 숫자를 판단
//		group by 연산이 비쌈.

// 2. recent article 의 count로 판단
//		tarnsaction 처리를 recentVersion 생성 까지 묶어야 함.

// 3. hash 함수를 사용하여 key 값을 부여.
//		해시 충돌시에 비용이 증가.

// 4. 메모리에 idx 값을 보관 하여 transactional 하게 접근하기.
//		어딘가에 idx 를 보관하고 불러오고 저장이 가능하여야 함.

// 5. recent table을 des order 하여 맨위의 값을 뽑기.

@Service
public class VersionManagementService {

	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;
	
	@Autowired
	RecnetVersionMapper recnetVersionMapper;
	
	@Autowired
	BranchMapper branchMapper;
	
	private static int last_board_history_id;
	
	public VersionManagementService() {
	}
	
	@PostConstruct
	private void init() {
		last_board_history_id = recnetVersionMapper.getLastVersionHistoryId();
	}
	
	// TODO : throws Exception 처리할것..
	@Transactional
	public int createArticle(BoardDTO article) throws Exception{
		
		int board_id = boardMapper.boardCreate(article);
		article = boardMapper.viewDetail(board_id);
		
		BoardHistoryDTO boardHistoryDTO = new BoardHistoryDTO(article);
		boardHistoryDTO.setBoard_id(article.getBoard_id());
		boardHistoryDTO.setBoard_history_id(last_board_history_id);
		boardHistoryMapper.createHistory(boardHistoryDTO);

		RecentVersionDTO recentVersionDTO = new RecentVersionDTO(boardHistoryDTO);
		recnetVersionMapper.createRecentVersion(recentVersionDTO);
		
		last_board_history_id++;
		return board_id;
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
