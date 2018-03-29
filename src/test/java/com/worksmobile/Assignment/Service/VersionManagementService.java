package com.worksmobile.Assignment.Service;

import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.BoardHistoryDTO;
import com.worksmobile.Assignment.Domain.NodePtrDTO;
import com.worksmobile.Assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.Assignment.Mapper.BoardMapper;

/***************History Id 을 어떻게 부여 해야 할지?************/
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
	
	public VersionManagementService() {
	}
	
	@PostConstruct
	private void init() {
	}
	
	// TODO : last_board_id 가 스레드 세이프 한지 확인할 것.
	/*
	 * @Return : 실패시 null 반환
	 */
	@Async
	@Transactional
	public Future<BoardHistoryDTO> createArticle(BoardDTO article) {
		int last_board_id = boardMapper.getMaxBoardId();
		last_board_id++;

		BoardHistoryDTO boardHistoryDTO = new BoardHistoryDTO(article, new NodePtrDTO(last_board_id, 1, 1));
		int insertedRowCnt = boardHistoryMapper.createHistory(boardHistoryDTO);
		if(insertedRowCnt == 0) {
			return null;
		}
		
		NodePtrDTO nodePtr = new NodePtrDTO(boardHistoryDTO);
		boardHistoryDTO = boardHistoryMapper.getHistory(nodePtr);

		article.setNodePtrDTO(nodePtr);
		article.setCreated(boardHistoryDTO.getCreated());
		
		insertedRowCnt = boardMapper.boardCreate(article);
		if(insertedRowCnt == 0) {
			return null;
		}
		
		last_board_id++;
		return new AsyncResult<BoardHistoryDTO> (boardHistoryDTO);
	}
	
	public NodePtrDTO recoverVersion(NodePtrDTO recoverPtr, NodePtrDTO leapPtr)
	{
		BoardHistoryDTO recoverHistoryDTO = null;
		BoardHistoryDTO leapHistoryDTO = null;
		recoverHistoryDTO = boardHistoryMapper.getHistory(recoverPtr);
		leapHistoryDTO = boardHistoryMapper.getHistory(leapPtr);
		if(leapHistoryDTO == null || recoverHistoryDTO == null)
		{
			throw new RuntimeException("게시글 이력이 존재하지 않습니다.");
		}
		
		BoardDTO recoveredBoardDTO = new BoardDTO(recoverHistoryDTO);
			
		return createVersionWithBranch(recoveredBoardDTO, leapPtr, "recovered");
	}
	
	public NodePtrDTO modifyVersion(BoardDTO modifiedBoard, NodePtrDTO parentPtrDTO) {
		return createVersionWithBranch(modifiedBoard, parentPtrDTO, "Modified");
	}
	
	private NodePtrDTO createVersionWithBranch(BoardDTO boardDTO, NodePtrDTO parentPtrDTO, String status) {
		BoardHistoryDTO newBranchHistoryDTO = new BoardHistoryDTO();
		newBranchHistoryDTO.setBoard_id(parentPtrDTO.getBoard_id());
		newBranchHistoryDTO.setVersion(parentPtrDTO.getVersion() + 1);
		newBranchHistoryDTO.setStatus(status);
		newBranchHistoryDTO.setHistory_subject(boardDTO.getSubject());
		// TODO : zipping
		newBranchHistoryDTO.setHistory_content(null);
		newBranchHistoryDTO.setFile_name(boardDTO.getFile_name());
		newBranchHistoryDTO.setFile_data(boardDTO.getFile_data());
		newBranchHistoryDTO.setFile_size(boardDTO.getFile_size());
		newBranchHistoryDTO.setParentNodePtr(parentPtrDTO);
		newBranchHistoryDTO = createHistoryWithLastBranch(newBranchHistoryDTO, 
				newBranchHistoryDTO.getBoard_id(), newBranchHistoryDTO.getVersion());

		NodePtrDTO newLeapPtr = new NodePtrDTO(newBranchHistoryDTO);
		newBranchHistoryDTO = boardHistoryMapper.getHistory(newLeapPtr);

		boardDTO.setNodePtrDTO(newLeapPtr);
		boardDTO.setCreated(newBranchHistoryDTO.getCreated());
		
		if(isLeap(parentPtrDTO)) {
			boardMapper.boardDelete(parentPtrDTO.toMap());
		}
		boardMapper.boardCreate(boardDTO);
		
		return newLeapPtr;
	}
	
	
	private boolean isLeap(NodePtrDTO nodePtrDTO) {
		List<BoardHistoryDTO> children = boardHistoryMapper.getChildren(nodePtrDTO);
		
		if(children.size() == 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// TODO : 여기만 동기화가 필요함.
		// @Async (for branch)
	private BoardHistoryDTO createHistoryWithLastBranch(BoardHistoryDTO historyDTO, int board_id, int version) {
		int last_branch = boardHistoryMapper.getLastbranch(board_id, version);
		historyDTO.setBranch(last_branch + 1);
		int insertedCnt = boardHistoryMapper.createHistory(historyDTO);
		if(insertedCnt == 0) {
			return null;
		}
		else {
			return historyDTO;
		}
	}

	// TODO : 루트 삭제는 고려하지 않음.
	public void deleteVersion(NodePtrDTO deletePtrDTO) {
		BoardHistoryDTO deleteHistoryDTO = boardHistoryMapper.getHistory(deletePtrDTO);

		NodePtrDTO parentPtrDTO = deleteHistoryDTO.getParentPtrDTO();

		List<BoardHistoryDTO> children = boardHistoryMapper.getChildren(parentPtrDTO);
		if(children.size() == 0) {
			BoardHistoryDTO parentHistoryDTO = boardHistoryMapper.getHistory(parentPtrDTO);
			BoardDTO parentDTO = new BoardDTO(parentHistoryDTO);
			
			boardMapper.boardDelete(deletePtrDTO.toMap());
			boardMapper.boardCreate(parentDTO);
		}
		else {
			for(BoardHistoryDTO childHistoryDTO : children) {
				childHistoryDTO.setParentNodePtr(parentPtrDTO);
			}
		}
	}
	
	public NodePtrDTO findAncestorHaveAnotherChild(NodePtrDTO curPtrDTO) {
		List<BoardHistoryDTO> children;
		do {
			BoardHistoryDTO boardHistoryDTO = boardHistoryMapper.getHistory(curPtrDTO);
			curPtrDTO = boardHistoryDTO.getParentPtrDTO();
			children = boardHistoryMapper.getChildren(curPtrDTO);
		} while(children.size() != 1);
		
		return curPtrDTO;
	}
	
	public void deleteUntilHasChild(NodePtrDTO deletePtrDTO) {
		boolean isDeleteLeap = false;
		List<BoardHistoryDTO> children;
		BoardDTO newLeapDTO = null;
		
		while(true) {
			BoardHistoryDTO deleteHistoryDTO = boardHistoryMapper.getHistory(deletePtrDTO);
			NodePtrDTO parentPtrDTO = deleteHistoryDTO.getParentPtrDTO();
			children = boardHistoryMapper.getChildren(parentPtrDTO);
			
			if(children.size() > 1) {
				if(isDeleteLeap) {
					newLeapDTO = new BoardDTO(deleteHistoryDTO);
					boardMapper.boardCreate(newLeapDTO);
				}
				break;
			}

			children = boardHistoryMapper.getChildren(deletePtrDTO);
			if(children.size() == 0) {
				isDeleteLeap = true;
				
				boardMapper.boardDelete(deletePtrDTO.toMap());
			}
			else {
				for(BoardHistoryDTO childHistoryDTO : children) {
					childHistoryDTO.setParentNodePtr(parentPtrDTO);
				}
			}
			int deletedCnt = boardHistoryMapper.deleteHistory(deletePtrDTO);
			if(deletedCnt == 0) {
				throw new RuntimeException("not deleted error");
			}
		}
	}
}
