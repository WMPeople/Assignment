package com.worksmobile.Assignment.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.BoardHistoryDTO;
import com.worksmobile.Assignment.Domain.NodePtrDTO;
import com.worksmobile.Assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.Assignment.Mapper.BoardMapper;
import com.worksmobile.Assignment.util.Utils;

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
	
	private BoardHistoryDTO getBoardHistory(List<BoardHistoryDTO> historyList, NodePtrDTO nodePtrDTO) {
		for(BoardHistoryDTO eleHistoryDTO : historyList) {
			NodePtrDTO elePtrDTO = eleHistoryDTO;
			if(elePtrDTO.equals(nodePtrDTO)) {
				return eleHistoryDTO;
			}
		}
		return null;
	}
	
	/***
	 * 한 게시글과 연관된 모든 게시글 이력을 반환합니다.
	 * @param leapPtrDTO 가져올 리프 노드 포인터.
	 * @return 연관된 게시글 히스토리들
	 * @throws NotLeapNodeException 리프 노드가 아닌 것을 삭제할때 발생됩.
	 */
	public List<BoardHistoryDTO> getRelatedHistory(final NodePtrDTO leapPtrDTO) throws NotLeapNodeException{
		if(!isLeap(leapPtrDTO)) {
			String leapPtrJson = Utils.jsonStringIfExceptionToString(leapPtrDTO);
			throw new NotLeapNodeException("leap node 정보" + leapPtrJson);
		}
		List<BoardHistoryDTO> boardHistoryList = null;
		boardHistoryList = boardHistoryMapper.getHistoryByBoardId(leapPtrDTO.getBoard_id());
		List<BoardHistoryDTO> relatedHistoryList = new ArrayList<>(boardHistoryList.size());
		
		BoardHistoryDTO leapHistoryDTO = getBoardHistory(boardHistoryList, leapPtrDTO);
		if(leapHistoryDTO == null) {
			String historyListJson = Utils.jsonStringIfExceptionToString(boardHistoryList);
			throw new RuntimeException("getRelatedHistory에서 노드 포인트가 history에 존재하지 않음" + leapPtrDTO + "\n" +
										"listCnt : " + boardHistoryList.size() + "content : " + historyListJson);
		}

		while(leapHistoryDTO != null) {
			relatedHistoryList.add(leapHistoryDTO);
			NodePtrDTO parentPtrDTO = leapHistoryDTO.getParentPtrDTO();
			BoardHistoryDTO parentHistoryDTO = getBoardHistory(boardHistoryList, parentPtrDTO);
			leapHistoryDTO = parentHistoryDTO;
		}
		
		return relatedHistoryList;
	}
	
	// TODO : last_board_id 가 스레드 세이프 한지 확인할 것.
	/***
	 * 최초 기록 게시글을 등록합니다. 
	 * @param article 게시글에 대한 정보.
	 * @return 새로운 게시글을 가리키는 포인터.
	 */
	@Transactional
	public BoardHistoryDTO createArticle(BoardDTO article) {
		int last_board_id = boardMapper.getMaxBoardId();
		NodePtrDTO newNodePtrDTO = new NodePtrDTO(last_board_id + 1, 1, 1);
		BoardHistoryDTO boardHistoryDTO = new BoardHistoryDTO(article, newNodePtrDTO, "Created");
		try {
			boardHistoryDTO.setHistory_content(Compress.compress(article.getContent()));
		} catch(IOException e) {
			e.printStackTrace();
			String json = Utils.jsonStringIfExceptionToString(article);
			throw new RuntimeException("createArticle메소드에서 게시글 내용을 압축에 실패하였습니다. \n게시글 : " + json);
		}
		int insertedRowCnt = boardHistoryMapper.createHistory(boardHistoryDTO);
		if(insertedRowCnt == 0) {
			throw new RuntimeException("createArticle메소드에서 createHistory error" + boardHistoryDTO);
		}
		
		NodePtrDTO nodePtr = boardHistoryDTO;
		boardHistoryDTO = boardHistoryMapper.getHistory(nodePtr);

		article.setNodePtrDTO(nodePtr);
		article.setCreated(boardHistoryDTO.getCreated());
		
		insertedRowCnt = boardMapper.boardCreate(article);
		if(insertedRowCnt == 0) {
			throw new RuntimeException("createArticle메소드에서 boardCreate error" + boardHistoryDTO);
		}
		
		return boardHistoryDTO;
	}
	
	/***
	 * 버전 복구 기능입니다. board DB및  boardHistory 둘다 등록 됩니다.
	 * @param recoverPtr 복구할 버전에 대한 포인터.
	 * @param leapPtr 복구 후 부모가 될 리프 포인터.
	 * @return 새롭게 등록된 버전에 대한 포인터.
	 */
	public NodePtrDTO recoverVersion(final NodePtrDTO recoverPtr, final NodePtrDTO leapPtr)
	{
		BoardHistoryDTO recoverHistoryDTO = null;
		BoardHistoryDTO leapHistoryDTO = null;
		recoverHistoryDTO = boardHistoryMapper.getHistory(recoverPtr);
		leapHistoryDTO = boardHistoryMapper.getHistory(leapPtr);
		if(leapHistoryDTO == null || recoverHistoryDTO == null)
		{
			String json = "";
			try {
				json = Utils.jsonStringFromObject(leapHistoryDTO);
				json.concat(Utils.jsonStringFromObject(recoverHistoryDTO));
			} catch(JsonProcessingException e) {
				e.printStackTrace();
				json = leapHistoryDTO.toString();
				json += recoverHistoryDTO.toString();
			}
			throw new RuntimeException("recoverVersion에서 복구할 게시글 이력이 존재하지 않습니다. \nleapHistoryDTO : " + json);
		}
		
		BoardDTO recoveredBoardDTO = new BoardDTO(recoverHistoryDTO);
		try {
			recoveredBoardDTO.setContent(Compress.deCompress(recoverHistoryDTO.getHistory_content()));
		} catch (IOException e) {
			e.printStackTrace();
			String json = Utils.jsonStringIfExceptionToString(recoveredBoardDTO);
			throw new RuntimeException("recoverVersion에서 게시글 내용을 압축해제 중 에러 발생. \nrecoveredBoardDTO : " + json);
		}
			
		return createVersionWithBranch(recoveredBoardDTO, leapPtr, "recovered");
	}
	
	/***
	 * 부모 버전이 있을 때 새로운 버전을 등록합니다.
	 * @param modifiedBoard 새롭게 등록될 게시글에 대한 정보.
	 * @param parentPtrDTO 부모를 가리키는 노드 포인터.
	 * @return 새롭게 생성된 리프 노드를 가리킵니다.
	 */
	public NodePtrDTO modifyVersion(BoardDTO modifiedBoard, NodePtrDTO parentPtrDTO) {
		return createVersionWithBranch(modifiedBoard, parentPtrDTO, "Modified");
	}
	
	private NodePtrDTO createVersionWithBranch(BoardDTO boardDTO, final NodePtrDTO parentPtrDTO, final String status) {
		byte[] compressedContent = null;
		try {
			compressedContent = Compress.compress(boardDTO.getContent());
		} catch(IOException e) {
			e.printStackTrace();
			String json = Utils.jsonStringIfExceptionToString(boardDTO);
			throw new RuntimeException("createVersionWithBranch메소드에서 게시글 내용을 압축에 실패하였습니다. \n게시글 : " + json);
		}
		
		if(isLeap(parentPtrDTO)) {
			int deletedCnt = boardMapper.boardDelete(parentPtrDTO.toMap());
			if(deletedCnt != 1) {
				String json = Utils.jsonStringIfExceptionToString(parentPtrDTO);
				throw new RuntimeException("delete cnt expected 1 but " + deletedCnt + "\n parentPtrDTO :" + json);
			}
		}
		
		BoardHistoryDTO newBranchHistoryDTO = new BoardHistoryDTO();
		newBranchHistoryDTO.setBoard_id(parentPtrDTO.getBoard_id());
		newBranchHistoryDTO.setVersion(parentPtrDTO.getVersion() + 1);
		newBranchHistoryDTO.setStatus(status);
		newBranchHistoryDTO.setHistory_subject(boardDTO.getSubject());
		newBranchHistoryDTO.setHistory_content(compressedContent);
		newBranchHistoryDTO.setFile_name(boardDTO.getFile_name());
		newBranchHistoryDTO.setFile_data(boardDTO.getFile_data());
		newBranchHistoryDTO.setFile_size(boardDTO.getFile_size());
		newBranchHistoryDTO.setParentNodePtr(parentPtrDTO);
		newBranchHistoryDTO = createHistoryWithLastBranch(newBranchHistoryDTO, 
				newBranchHistoryDTO.getBoard_id(), newBranchHistoryDTO.getVersion());

		NodePtrDTO newLeapPtr = newBranchHistoryDTO;
		newBranchHistoryDTO = boardHistoryMapper.getHistory(newLeapPtr);

		boardDTO.setNodePtrDTO(newLeapPtr);
		boardDTO.setCreated(newBranchHistoryDTO.getCreated());
		
		int createdCnt = boardMapper.boardCreate(boardDTO);
		if(createdCnt != 1) {
			throw new RuntimeException("created cnt expected 1 but " + createdCnt);
		}
		
		return newLeapPtr;
	}
	
	
	private boolean isLeap(final NodePtrDTO nodePtrDTO) {
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
	private BoardHistoryDTO createHistoryWithLastBranch(BoardHistoryDTO historyDTO, final int board_id, final int version) {
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
	
	private void updateHistoryParentPtr(BoardHistoryDTO boardHistoryDTO) {
		int updatedCnt = boardHistoryMapper.updateHistoryParent(boardHistoryDTO);
		if(updatedCnt != 1) {
			String json;
			try {
				json = Utils.jsonStringFromObject(boardHistoryDTO);
			} catch(Exception e) {
				json = " to json error";
			}
			throw new RuntimeException("updateRowCnt expected 1 but : " + updatedCnt + "\n" +
										"in " + json);
		}
	}

	// TODO : 루트 삭제는 고려하지 않음.
	/***
	 * 특정 버전에 대한 이력 1개를 삭제합니다. leap노드이면 board도 삭제 됩니다. 
	 * @param deletePtrDTO 삭제할 버전에 대한 정보.
	 */
	public void deleteVersion(final NodePtrDTO deletePtrDTO) {
		BoardHistoryDTO deleteHistoryDTO = boardHistoryMapper.getHistory(deletePtrDTO);
		NodePtrDTO parentPtrDTO = deleteHistoryDTO.getParentPtrDTO();

		List<BoardHistoryDTO> deleteNodeChildren = boardHistoryMapper.getChildren(deletePtrDTO);
		if(deleteNodeChildren.size() == 0) {
			BoardHistoryDTO parentHistoryDTO = boardHistoryMapper.getHistory(parentPtrDTO);
			BoardDTO parentDTO = new BoardDTO(parentHistoryDTO);
			try {
				String content = Compress.deCompress(parentHistoryDTO.getHistory_content());
				parentDTO.setContent(content);
			} catch(IOException e) {
				e.printStackTrace();
				String history = Utils.jsonStringIfExceptionToString(parentHistoryDTO);
				throw new RuntimeException("deleteVersion메소드에서 압축 해제 실패 \nhistoryDTO : " + history);
			}
			
			int deletedCnt = boardMapper.boardDelete(deletePtrDTO.toMap());
			int createdCnt = boardMapper.boardCreate(parentDTO);
			if(deletedCnt == 0 || createdCnt == 0) {
				throw new RuntimeException("deleteVersion메소드에서 DB의 board테이블 리프 노드를 갱신(board에서)시 발생" +
						"deleteRowCnt : " + deletedCnt + " createdCnt : " + createdCnt);
			}
		}
		else {
			for(BoardHistoryDTO childHistoryDTO : deleteNodeChildren) {
				childHistoryDTO.setParentNodePtr(parentPtrDTO);
				updateHistoryParentPtr(childHistoryDTO);
			}
		}
		int deletedCnt = boardHistoryMapper.deleteHistory(deletePtrDTO);
		if(deletedCnt != 1) {
			throw new RuntimeException("deleteVersion메소드에서 게시글이력 테이블 삭제 에러 deletedCnt : " + deletedCnt);
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
	
	/**
	 * 특정 게시글을 삭제합니다. 연관 브렌치를 모두 삭제합니다. (단, 형제 노드가 존재 할때까지)
	 * @param leapPtrDTO 리프 노드만 주어야합니다.
	 * @throws NotLeapNodeException 리프 노드가 아닌 것을 삭제할때 발생됩.
	 */
	public void deleteArticle(NodePtrDTO leapPtrDTO) throws NotLeapNodeException {
		int deletedCnt = boardMapper.boardDelete(leapPtrDTO.toMap());
		if(deletedCnt != 1) {
			String leapPtrJson;
			try {
				leapPtrJson = Utils.jsonStringFromObject(leapPtrDTO);
			} catch(JsonProcessingException e) {
				leapPtrJson = "NodePtrDTO convert to json failed" + "\n" + leapPtrDTO;
			}
			throw new NotLeapNodeException("node 정보" + leapPtrJson);
		}
		
		while(true) {
			BoardHistoryDTO deleteHistoryDTO = boardHistoryMapper.getHistory(leapPtrDTO);
			if(deleteHistoryDTO == null) {
				break;
			}
			NodePtrDTO parentPtrDTO = deleteHistoryDTO.getParentPtrDTO();

			deletedCnt = boardHistoryMapper.deleteHistory(leapPtrDTO);
			if(deletedCnt == 0) {
				throw new RuntimeException("deleteArticle메소드에서 게시글이력 테이블 삭제 에러 deletedCnt : " + deletedCnt);
			}
			
			List<BoardHistoryDTO> children = boardHistoryMapper.getChildren(parentPtrDTO);
			
			if(children.size() >= 1) {
				break;
			}
			leapPtrDTO = parentPtrDTO;
		}
	}
}
