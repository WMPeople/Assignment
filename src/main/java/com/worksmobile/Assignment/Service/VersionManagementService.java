package com.worksmobile.Assignment.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.BoardHistoryDTO;
import com.worksmobile.Assignment.Domain.NodePtrDTO;
import com.worksmobile.Assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.Assignment.Mapper.BoardMapper;
import com.worksmobile.Assignment.Mapper.FileMapper;
import com.worksmobile.Assignment.util.Utils;

@Service
public class VersionManagementService {

	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;
	
	@Autowired
	FileMapper fileMapper;
	
	private Map<Map.Entry<Integer, Integer>, BoardHistoryDTO> getHistoryMap(int root_board_id) {
		List<BoardHistoryDTO> historyList = boardHistoryMapper.getHistoryByRootBoardId(root_board_id);
		Map<Map.Entry<Integer, Integer>, BoardHistoryDTO> historyMap = new HashMap<>();
		for(BoardHistoryDTO ele : historyList) {
			historyMap.put(ele.toBoardIdAndVersionEntry(), ele);
		}
		return historyMap;
	}
	
	/***
	 * 한 게시글과 연관된 모든 게시글 이력을 반환합니다.
	 * @param leafPtrDTO 가져올 리프 노드 포인터.(board_id, version만 사용)
	 * @return 연관된 게시글 히스토리들
	 * @throws NotLeafNodeException 리프 노드가 아닌 것을 삭제할때 발생됩.
	 */
	public List<BoardHistoryDTO> getRelatedHistory(NodePtrDTO leafPtrDTO) throws NotLeafNodeException{
		BoardDTO board = boardMapper.viewDetail(leafPtrDTO.toMap());
		
		if(board == null) {
			String leafPtrJson = Utils.jsonStringIfExceptionToString(leafPtrDTO);
			throw new NotLeafNodeException("leaf node 정보" + leafPtrJson);
		}
		Map<Map.Entry<Integer, Integer>, BoardHistoryDTO> boardHisotryMap = getHistoryMap(board.getRoot_board_id());
		List<BoardHistoryDTO> relatedHistoryList = new ArrayList<>(boardHisotryMap.size());
		
		NodePtrDTO curPosPtrDTO = leafPtrDTO;
		BoardHistoryDTO leafHistoryDTO;
		do {
			leafHistoryDTO = boardHisotryMap.get(curPosPtrDTO .toBoardIdAndVersionEntry());
			if(leafHistoryDTO == null) {
				String curPosJson = Utils.jsonStringIfExceptionToString(curPosPtrDTO);
				String historyListJson = Utils.jsonStringIfExceptionToString(boardHisotryMap);
				throw new RuntimeException("getRelatedHistory에서 노드 포인트가 history에 존재하지 않음" + curPosJson  + "\n" +
											"listCnt : " + relatedHistoryList.size() + ", content : " + historyListJson);
			}
			relatedHistoryList.add(leafHistoryDTO);
			curPosPtrDTO = leafHistoryDTO.getParentPtrAndRoot();
		}
		while(leafHistoryDTO.getParent_version() != 0);
		return relatedHistoryList;
	}
	
	/***
	 * 최초 기록 게시글을 등록합니다. 
	 * @param article 게시글에 대한 정보.
	 * @return 새로운 게시글의 이력
	 */
	@Transactional
	public BoardHistoryDTO createArticle(BoardDTO article) {
		// 버전 0은 안보이는 루트를 위함. 보이는 루트는 1로 지정.
		article.setBoard_id(NodePtrDTO.ISSUE_NEW_BOARD_ID);
		return createArticleAndHistory(article, 0, BoardHistoryDTO.STATUS_CREATED, new NodePtrDTO());
	}

	/**
	 *  압축은 충돌영역에서 제외하기위함.
	 * @param article 등록할 게시물의 제목, 내용, 첨부파일이 사용됩니다.
	 * @param version 새로운 게시물의 버전
	 * @param status 게시물 이력의 상태에 들어갈 내용
	 * @param parentNodePtr 게시물의 부모 노드 포인터
	 * @return 새롭게 생성된 게시글 이력 DTO
	 */
	private BoardHistoryDTO createArticleAndHistory(BoardDTO article, int version, final String status, final NodePtrDTO parentNodePtr) {
		if(parentNodePtr == null) {
			throw new RuntimeException("createArticleAndHistory메소드 parentNodePtr은 null 이 될 수 없습니다. ");
		}
		byte[] compressedContent = Compress.compressArticleContent(article);

		return createArticleAndHistory(article, version, status, compressedContent, parentNodePtr);
	}
	
	/***
	 *  충돌 영역! 게시판 DB 와 이력 DB 에 둘다 등록합니다.
	 *  article의 board_id가 -2이면 1증가 시켜 작성합니다.
	 * @param article 등록할 게시물의 제목, 내용, 첨부파일이 사용됩니다.
	 * @param version 새로운 게시물의 버전
	 * @param status 게시물 이력의 상태에 들어갈 내용
	 * @param compressedContent 압축된 내용
	 * @param parentNodePtr 게시물의 부모 노드 포인터
	 * @return 새롭게 생성된 게시글 이력 DTO
	 */
	synchronized private BoardHistoryDTO createArticleAndHistory(BoardDTO article, int version, final String status, final byte[] compressedContent, NodePtrDTO parentNodePtr) {
		NodePtrDTO newNodePtrDTO;
		if(article.getBoard_id() == NodePtrDTO.ISSUE_NEW_BOARD_ID) {	// 새로 발급하는 경우..? 새로운 게시글, 충돌 관리 일때
			int last_board_id = boardMapper.getLeapNodeMaxBoardId() + 1;
			newNodePtrDTO = new NodePtrDTO(last_board_id, version, NodePtrDTO.ROOT_BOARD_ID);
			if(parentNodePtr.getBoard_id() == null) {	// 새로운 게시글
				parentNodePtr.setRoot_board_id(last_board_id);
			}
		} else {
			newNodePtrDTO = new NodePtrDTO(parentNodePtr.getBoard_id(), version, parentNodePtr.getRoot_board_id());
		}
		BoardHistoryDTO boardHistoryDTO;
		if(version == 0) {	// 루트 노드일 경우
			// 안보이는 루트를 먼저 만듦.
			BoardHistoryDTO rootHistoryDTO = new BoardHistoryDTO(new BoardDTO(), newNodePtrDTO, BoardHistoryDTO.STATUS_ROOT);
			int insertedRowCnt = boardHistoryMapper.createHistory(rootHistoryDTO);
			if(insertedRowCnt != 1) {
				String json = Utils.jsonStringIfExceptionToString(rootHistoryDTO);
				throw new RuntimeException("createArticleAndHistory메소드에서 게시글 이력 추가 에러 insertedRowCnt : " + insertedRowCnt + "\nrootHistoryDTO : " + json);
			}
			
			// 보이는 루트를 만듦.
			boardHistoryDTO = new BoardHistoryDTO(article, newNodePtrDTO, status);
			boardHistoryDTO.setHistory_content(compressedContent);
			boardHistoryDTO.setParentNodePtrAndRoot(rootHistoryDTO);
			boardHistoryDTO.setVersion(1);
			boardHistoryDTO.setRoot_board_id(newNodePtrDTO.getBoard_id());
		}
		else {
			boardHistoryDTO = new BoardHistoryDTO(article, newNodePtrDTO, status);
			boardHistoryDTO.setHistory_content(compressedContent);
			boardHistoryDTO.setParentNodePtrAndRoot(parentNodePtr);
		}
		int insertedRowCnt = boardHistoryMapper.createHistory(boardHistoryDTO);
		if(insertedRowCnt != 1) {
			String json = Utils.jsonStringIfExceptionToString(boardHistoryDTO);
			throw new RuntimeException("createArticleAndHistory메소드에서 게시글 이력 추가 에러 insertedRowCnt : " + insertedRowCnt + "\nrootHistoryDTO : " + json);
		}
		
		article.setNodePtrDTO(boardHistoryDTO);
		article.setCreated(boardHistoryDTO.getCreated());
		
		insertedRowCnt = boardMapper.boardCreate(article);
		if(insertedRowCnt != 1) {
			throw new RuntimeException("createArticle메소드에서 boardCreate error" + boardHistoryDTO);
		}
		
		return boardHistoryDTO;
	}
	
	// TODO : 생성시에 잎 노드 인지 보장을 하지 않음.
	// TODO : 충돌 관리?? 만약 잎 노드가 아닌경우 새로운 게시물 번호로 만들어짐을 유의할것.
	/***
	 * 버전 복구 기능입니다. board DB및  boardHistory 둘다 등록 됩니다.
	 * @param recoverPtr 복구할 버전에 대한 포인터.
	 * @param leafPtr 복구 후 부모가 될 리프 포인터.
	 * @return 새롭게 등록된 버전에 대한 포인터.
	 */
	@Transactional
	public NodePtrDTO recoverVersion(final NodePtrDTO recoverPtr, final NodePtrDTO leafPtr)
	{
		BoardHistoryDTO recoverHistoryDTO = boardHistoryMapper.getHistory(recoverPtr);
		BoardHistoryDTO leafHistoryDTO = boardHistoryMapper.getHistory(leafPtr);
		if(leafHistoryDTO == null || recoverHistoryDTO == null)
		{
			String json = Utils.jsonStringIfExceptionToString(leafHistoryDTO);
			json += "\n";
			json += Utils.jsonStringIfExceptionToString(recoverHistoryDTO);
			throw new RuntimeException("recoverVersion에서 복구할 게시글 이력이 존재하지 않습니다. \nleafHistoryDTO : " + json);
		}
		
		BoardDTO recoveredBoardDTO = new BoardDTO(recoverHistoryDTO);
		try {
			recoveredBoardDTO.setContent(Compress.deCompress(recoverHistoryDTO.getHistory_content()));
		} catch (IOException e) {
			e.printStackTrace();
			String json = Utils.jsonStringIfExceptionToString(recoveredBoardDTO);
			throw new RuntimeException("recoverVersion에서 게시글 내용을 압축해제 중 에러 발생. \nrecoveredBoardDTO : " + json);
		}
		String status = String.format("%s(%s)", BoardHistoryDTO.STATUS_RECOVERED, recoverPtr.toString());
		return createVersionWithBranch(recoveredBoardDTO, leafPtr, status);
	}
	
	/***
	 * 부모 버전이 있을 때 새로운 버전을 등록합니다.
	 * 자동 저장 게시글은 삭제되지 않습니다.
	 * @param modifiedBoard 새롭게 등록될 게시글에 대한 정보.
	 * @param parentPtrDTO 부모를 가리키는 노드 포인터.
	 * @return 새롭게 생성된 리프 노드를 가리킵니다.
	 */
	@Transactional
	public NodePtrDTO modifyVersion(BoardDTO modifiedBoard, NodePtrDTO parentPtrDTO) {
		return createVersionWithBranch(modifiedBoard, parentPtrDTO, BoardHistoryDTO.STATUS_MODIFIED);
	}
	
	/***
	 * 리프 노드일시 자동저장 게시글은 삭제 되지 않습니다.
	 * @param boardDTO 새로운 버전의 게시글의 내용
	 * @param parentPtrDTO 부모가 될 노드의 포인터
	 * @param status 게시글 이력에 남길 상태
	 * @return 생성된 게시글의 포인터
	 */
	synchronized private NodePtrDTO createVersionWithBranch(BoardDTO boardDTO, NodePtrDTO parentPtrDTO, final String status) {
		NodePtrDTO dbParentPtr = boardHistoryMapper.getHistory(parentPtrDTO); // 클라이언트에서 root_board_id를 주지 않았을때를 위함.(또는
																				// 존재하지 않는 값을 줬을때)
		List<BoardHistoryDTO> childrenList= boardHistoryMapper.getChildren(dbParentPtr);
		if(childrenList.size() == 0) {
			int deletedCnt = boardMapper.boardDeleteWithCookieId(dbParentPtr.toMap());
			if(deletedCnt != 1) {
				throw new RuntimeException("delete cnt expected  but " + deletedCnt);
			}
		} else {
			boardDTO.setBoard_id(NodePtrDTO.ISSUE_NEW_BOARD_ID);
		}
		
		return createArticleAndHistory(boardDTO, dbParentPtr.getVersion() + 1, status, dbParentPtr);
	}
	
	private boolean isLeaf(final NodePtrDTO nodePtrDTO) {
		BoardDTO board = boardMapper.viewDetail(nodePtrDTO.toMap());
		
		if(board != null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/***
	 * 특정 버전에 대한 이력 1개를 삭제합니다. leaf노드이면 게시글도 삭제 됩니다. 
	 * 부모의 이력은 삭제되지 않음을 유의 해야 합니다.
	 * 자동 저장 게시글도 함께 삭제 됩니다.!!!!
	 * @param deletePtrDTO 삭제할 버전에 대한 정보.
	 * @return 새로운 리프 노드의 주소. 새로운 리프노드를 생성하지 않았으면 null을 반환함.
	 */
	@Transactional
	public NodePtrDTO deleteVersion(final NodePtrDTO deletePtrDTO) {
		BoardHistoryDTO deleteHistoryDTO = boardHistoryMapper.getHistory(deletePtrDTO);
		NodePtrDTO parentPtrDTO = deleteHistoryDTO.getParentPtrAndRoot();
		
		List<BoardHistoryDTO> deleteNodeChildren = boardHistoryMapper.getChildren(deletePtrDTO);

		int deletedCnt = boardMapper.boardDelete(deletePtrDTO.toMap());		// 임시 저장 게시글이 존재 할 수 도 있으므로 history를 지우기 위해서는 필요
		deletedCnt = boardHistoryMapper.deleteHistory(deletePtrDTO);
		if(deletedCnt != 1) {
			String json = Utils.jsonStringIfExceptionToString(deletePtrDTO);
			throw new RuntimeException("deleteVersion메소드에서 게시글이력 테이블 삭제 에러 deletedCnt : " + deletedCnt + "\ndeletePtrDTO : " + json);
		}

		if(deleteNodeChildren.size() == 0) {	// 리프 노드라면
			BoardHistoryDTO parentHistoryDTO = boardHistoryMapper.getHistory(parentPtrDTO);
			
			BoardDTO parentDTO = new BoardDTO(parentHistoryDTO);
			List<BoardHistoryDTO> parentChildren = boardHistoryMapper.getChildren(parentDTO);
			if(parentChildren.size() == 0 && parentHistoryDTO.isRoot()) {// 루트만 존재하는 경우에는 루트를 지워줍니다.
				deletedCnt = boardHistoryMapper.deleteHistory(parentHistoryDTO);
				if(deletedCnt != 1) {
					String json = Utils.jsonStringIfExceptionToString(parentHistoryDTO);
					throw new RuntimeException("deleteVersion메소드에서 게시글이력 테이블 삭제 에러 deletedCnt : " + deletedCnt + "\ndeletePtrDTO : " + json);
				}
			}
			else if(parentChildren.size() == 0 && !parentHistoryDTO.isRoot()){	// 루트가 아닌 리프 노드는 게시물 게시판에 존재해야 함.
				try {
					String content = Compress.deCompress(parentHistoryDTO.getHistory_content());
					parentDTO.setContent(content);
				} catch(IOException e) {
					e.printStackTrace();
					String history = Utils.jsonStringIfExceptionToString(parentHistoryDTO);
					throw new RuntimeException("deleteVersion메소드에서 압축 해제 실패 \nhistoryDTO : " + history);
				}
				int createdCnt = boardMapper.boardCreate(parentDTO);
				if(createdCnt == 0) {
					throw new RuntimeException("deleteVersion메소드에서 DB의 board테이블 리프 노드를 갱신(board에서)시 발생" +
							"deleteRowCnt : " + deletedCnt + " createdCnt : " + createdCnt);
				}
				return parentDTO;
			}
		}	// 리프 노드일때 끝
		else if(deleteHistoryDTO.isRoot()) {
			throw new RuntimeException("루트는 삭제할 수 없습니다.");
			// TODO : 한꺼번에 업데이트 하는 방법?
		}
		else {// 중간노드 일 경우
			for(BoardHistoryDTO childHistoryDTO : deleteNodeChildren) {
				childHistoryDTO.setParentNodePtrAndRoot(parentPtrDTO);
				int updatedCnt = boardHistoryMapper.updateHistoryParentAndRoot(childHistoryDTO);
				if(updatedCnt != 1) {
					String json = Utils.jsonStringIfExceptionToString(childHistoryDTO);
					throw new RuntimeException("updateRowCnt expected 1 but : " + updatedCnt + "\n" +
												"in " + json);
				}
			}
		}
		return null;
	}
	
	public NodePtrDTO findAncestorHaveAnotherChild(NodePtrDTO curPtrDTO) {
		List<BoardHistoryDTO> children;
		do {
			BoardHistoryDTO boardHistoryDTO = boardHistoryMapper.getHistory(curPtrDTO);
			curPtrDTO = boardHistoryDTO.getParentPtrAndRoot();
			children = boardHistoryMapper.getChildren(curPtrDTO);
		} while(children.size() != 1);
		
		return curPtrDTO;
	}
	
	/**
	 * 특정 게시글을 삭제합니다. 부모를 모두 삭제합니다. (단, 형제 노드가 존재 할때까지)
	 * 자동 저장 게시글이 있다면 전부 삭제됩니다.!!!!
	 * @param leafPtrDTO 리프 노드만 주어야합니다.
	 * @throws NotLeafNodeException 리프 노드가 아닌 것을 삭제할때 발생됨.
	 */
	@Transactional
	public void deleteArticle(NodePtrDTO leafPtrDTO) throws NotLeafNodeException {
		boolean deleteFileBoolean = false;
		if(!isLeaf(leafPtrDTO)) {
			String leafPtrJson = Utils.jsonStringIfExceptionToString(leafPtrDTO);
			throw new NotLeafNodeException("node 정보" + leafPtrJson);
		}
		int deletedCnt = boardMapper.boardDelete(leafPtrDTO.toMap());
		if(deletedCnt == 0) {
			String leafPtrJson = Utils.jsonStringIfExceptionToString(leafPtrDTO);
			throw new RuntimeException("deleteArticle에서 게시글 삭제 실패 leafPtrJson : " + leafPtrJson);
		}
		
		while(true) {
			BoardHistoryDTO deleteHistoryDTO = boardHistoryMapper.getHistory(leafPtrDTO);
			if(deleteHistoryDTO == null) {
				break;
			}
			NodePtrDTO parentPtrDTO = deleteHistoryDTO.getParentPtrAndRoot();
			int file_id = deleteHistoryDTO.getFile_id();
			if(file_id != 0) {
				int fileCount = boardHistoryMapper.getFileCount(file_id);
				if(fileCount ==1) {
					deleteFileBoolean=true;
				}
			}
			deletedCnt = boardMapper.boardDelete(leafPtrDTO.toMap());
			deletedCnt = boardHistoryMapper.deleteHistory(leafPtrDTO);
			if(deletedCnt == 0) {
				throw new RuntimeException("deleteArticle메소드에서 게시글이력 테이블 삭제 에러 deletedCnt : " + deletedCnt);
			}
			if(deleteFileBoolean) {
				deletedCnt = fileMapper.deleteFile(file_id);
				if(deletedCnt != 1) {
					throw new RuntimeException("파일 삭제 에러");
				};
			}
			List<BoardHistoryDTO> children = boardHistoryMapper.getChildren(parentPtrDTO);
			
			if(children.size() >= 1) {
				break;
			}
			leafPtrDTO = parentPtrDTO;
		}
	}
	
	// TODO : 임시 게시글 만들기.
	@Transactional
	public void createTempArticleOverwrite(BoardDTO tempArticle) {
		Logger.getLogger("VersionManagementService").info("cookie value = " + tempArticle.getCookie_id());
		Logger.getLogger("VersionManagementService").info(tempArticle.getCookie_id() +" /" + "version : " +tempArticle.getVersion());
		boolean deleteFileBoolean = false;
		tempArticle.setRoot_board_id(tempArticle.getBoard_id());			// getHistoryByRootId에서 검색이 가능하도록

		BoardDTO dbTempArticle = boardMapper.viewDetail(tempArticle.toMap());
		
		if(dbTempArticle != null) {
			int curFile_id = dbTempArticle.getFile_id();
			int afterFile_id = tempArticle.getFile_id();
			if(curFile_id != 0 && curFile_id != afterFile_id ) {
				int fileCount = boardMapper.getFileCount(curFile_id);
				if(fileCount ==1) {
					deleteFileBoolean=true;
				}
			}
			
			int articleUpdatedCnt = boardMapper.boardUpdate(tempArticle);
			if(articleUpdatedCnt != 1 ) {
				String json = Utils.jsonStringIfExceptionToString(tempArticle);
				throw new RuntimeException("createTempArticleOverwrite메소드에서 임시 게시글 수정 에러 tempArticle : " + json + "\n" +
				"articleUpdatedCnt : " + articleUpdatedCnt);
			}
			if(deleteFileBoolean) {
				int deletedCnt = fileMapper.deleteFile(curFile_id);
				if(deletedCnt != 1) {
					throw new RuntimeException("파일 삭제 에러");
				};
			}
		}
		else {
			int createdCnt = boardMapper.boardCreate(tempArticle);
			if(createdCnt != 1) {
				String json = Utils.jsonStringIfExceptionToString(tempArticle);
				throw new RuntimeException("createTempArticle에서 게시글 생성 실패 : " + json);
			}
			
		}
	}
}
