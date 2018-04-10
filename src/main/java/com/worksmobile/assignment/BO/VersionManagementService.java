package com.worksmobile.assignment.BO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worksmobile.assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.assignment.Mapper.BoardMapper;
import com.worksmobile.assignment.Mapper.FileMapper;
import com.worksmobile.assignment.Model.Board;
import com.worksmobile.assignment.Model.BoardHistory;
import com.worksmobile.assignment.Model.NodePtr;
import com.worksmobile.assignment.Util.Utils;

@Service
public class VersionManagementService {

	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;
	
	@Autowired
	FileMapper fileMapper;
	
	private Map<Map.Entry<Integer, Integer>, BoardHistory> getHistoryMap(int root_board_id) {
		List<BoardHistory> historyList = boardHistoryMapper.getHistoryByRootBoardId(root_board_id);
		Map<Map.Entry<Integer, Integer>, BoardHistory> historyMap = new HashMap<>();
		for(BoardHistory ele : historyList) {
			historyMap.put(ele.toBoardIdAndVersionEntry(), ele);
		}
		return historyMap;
	}
	
	/***
	 * 한 게시글과 연관된 모든 게시글 이력을 반환합니다.
	 * @param leafPtr 가져올 리프 노드 포인터.(board_id, version만 사용)
	 * @return 연관된 게시글 히스토리들
	 * @throws NotLeafNodeException 리프 노드가 아닌 것을 삭제할때 발생됩.
	 */
	public List<BoardHistory> getRelatedHistory(NodePtr leafPtr) throws NotLeafNodeException{
		Board board = boardMapper.viewDetail(leafPtr.toMap());
		
		if(board == null) {
			String leafPtrJson = Utils.jsonStringIfExceptionToString(leafPtr);
			throw new NotLeafNodeException("leaf node 정보" + leafPtrJson);
		}
		Map<Map.Entry<Integer, Integer>, BoardHistory> boardHisotryMap = getHistoryMap(board.getRoot_board_id());
		List<BoardHistory> relatedHistoryList = new ArrayList<>(boardHisotryMap.size());
		
		NodePtr curPosPtr = leafPtr;
		BoardHistory leafHistory;
		do {
			leafHistory = boardHisotryMap.get(curPosPtr .toBoardIdAndVersionEntry());
			if(leafHistory == null) {
				String curPosJson = Utils.jsonStringIfExceptionToString(curPosPtr);
				String historyListJson = Utils.jsonStringIfExceptionToString(boardHisotryMap);
				throw new RuntimeException("getRelatedHistory에서 노드 포인트가 history에 존재하지 않음" + curPosJson  + "\n" +
											"listCnt : " + relatedHistoryList.size() + ", content : " + historyListJson);
			}
			relatedHistoryList.add(leafHistory);
			curPosPtr = leafHistory.getParentPtrAndRoot();
		}
		while(leafHistory.getParent_version() != 0);
		return relatedHistoryList;
	}
	
	/***
	 * 최초 기록 게시글을 등록합니다. 
	 * @param article 게시글에 대한 정보.
	 * @return 새로운 게시글의 이력
	 */
	@Transactional
	public BoardHistory createArticle(Board article) {
		// 버전 0은 안보이는 루트를 위함. 보이는 루트는 1로 지정.
		article.setBoard_id(NodePtr.ISSUE_NEW_BOARD_ID);
		return createArticleAndHistory(article, 0, BoardHistory.STATUS_CREATED, new NodePtr());
	}

	/**
	 *  압축은 충돌영역에서 제외하기위함.
	 * @param article 등록할 게시물의 제목, 내용, 첨부파일이 사용됩니다.
	 * @param version 새로운 게시물의 버전
	 * @param status 게시물 이력의 상태에 들어갈 내용
	 * @param parentNodePtr 게시물의 부모 노드 포인터
	 * @return 새롭게 생성된 게시글 이력 
	 */
	private BoardHistory createArticleAndHistory(Board article, int version, final String status, final NodePtr parentNodePtr) {
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
	 * @return 새롭게 생성된 게시글 이력 
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
		BoardHistory boardHistory;
		if(version == 0) {	// 루트 노드일 경우
			// 안보이는 루트를 먼저 만듦.
			BoardHistory rootHistory = new BoardHistory(new Board(), newNodePtr, BoardHistory.STATUS_ROOT);
			int insertedRowCnt = boardHistoryMapper.createHistory(rootHistory);
			if(insertedRowCnt != 1) {
				String json = Utils.jsonStringIfExceptionToString(rootHistory);
				throw new RuntimeException("createArticleAndHistory메소드에서 게시글 이력 추가 에러 insertedRowCnt : " + insertedRowCnt + "\nrootHistory : " + json);
			}
			
			// 보이는 루트를 만듦.
			boardHistory = new BoardHistory(article, newNodePtr, status);
			boardHistory.setHistory_content(compressedContent);
			boardHistory.setParentNodePtrAndRoot(rootHistory);
			boardHistory.setVersion(1);
			boardHistory.setRoot_board_id(newNodePtr.getBoard_id());
		}
		else {
			boardHistory = new BoardHistory(article, newNodePtr, status);
			boardHistory.setHistory_content(compressedContent);
			boardHistory.setParentNodePtrAndRoot(parentNodePtr);
		}
		int insertedRowCnt = boardHistoryMapper.createHistory(boardHistory);
		if(insertedRowCnt != 1) {
			String json = Utils.jsonStringIfExceptionToString(boardHistory);
			throw new RuntimeException("createArticleAndHistory메소드에서 게시글 이력 추가 에러 insertedRowCnt : " + insertedRowCnt + "\nrootHistory : " + json);
		}
		
		article.setNodePtr(boardHistory);
		article.setCreated(boardHistory.getCreated());
		
		insertedRowCnt = boardMapper.boardCreate(article);
		if(insertedRowCnt != 1) {
			throw new RuntimeException("createArticle메소드에서 boardCreate error" + boardHistory);
		}
		
		return boardHistory;
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
	public NodePtr recoverVersion(final NodePtr recoverPtr, final NodePtr leafPtr)
	{
		BoardHistory recoverHistory = boardHistoryMapper.getHistory(recoverPtr);
		BoardHistory leafHistory = boardHistoryMapper.getHistory(leafPtr);
		if(leafHistory == null || recoverHistory == null)
		{
			String json = Utils.jsonStringIfExceptionToString(leafHistory);
			json += "\n";
			json += Utils.jsonStringIfExceptionToString(recoverHistory);
			throw new RuntimeException("recoverVersion에서 복구할 게시글 이력이 존재하지 않습니다. \nleafHistory : " + json);
		}
		
		Board recoveredBoard = new Board(recoverHistory);
		try {
			recoveredBoard.setContent(Compress.deCompress(recoverHistory.getHistory_content()));
		} catch (IOException e) {
			e.printStackTrace();
			String json = Utils.jsonStringIfExceptionToString(recoveredBoard);
			throw new RuntimeException("recoverVersion에서 게시글 내용을 압축해제 중 에러 발생. \nrecoveredBoard : " + json);
		}
		String status = String.format("%s(%s)", BoardHistory.STATUS_RECOVERED, recoverPtr.toString());
		return createVersionWithBranch(recoveredBoard, leafPtr, status);
	}
	
	/***
	 * 부모 버전이 있을 때 새로운 버전을 등록합니다.
	 * 자동 저장 게시글은 삭제되지 않습니다.
	 * @param modifiedBoard 새롭게 등록될 게시글에 대한 정보.
	 * @param parentPtr 부모를 가리키는 노드 포인터.
	 * @return 새롭게 생성된 리프 노드를 가리킵니다.
	 */
	@Transactional
	public NodePtr modifyVersion(Board modifiedBoard, NodePtr parentPtr) {
		return createVersionWithBranch(modifiedBoard, parentPtr, BoardHistory.STATUS_MODIFIED);
	}
	
	/***
	 * 리프 노드일시 자동저장 게시글은 삭제 되지 않습니다.
	 * @param board 새로운 버전의 게시글의 내용
	 * @param parentPtr 부모가 될 노드의 포인터
	 * @param status 게시글 이력에 남길 상태
	 * @return 생성된 게시글의 포인터
	 */
	synchronized private NodePtr createVersionWithBranch(Board board, NodePtr parentPtr, final String status) {
		NodePtrDTO dbParentPtr = boardHistoryMapper.getHistory(parentPtrDTO); // 클라이언트에서 root_board_id를 주지 않았을때를 위함.(또는
																				// 존재하지 않는 값을 줬을때)
		List<BoardHistory> childrenList= boardHistoryMapper.getChildren(dbParentPtr);
		if(childrenList.size() == 0) {
			int deletedCnt = boardMapper.boardDeleteWithCookieId(dbParentPtr.toMap());
			if(deletedCnt != 1) {
				throw new RuntimeException("delete cnt expected  but " + deletedCnt);
			}
			parentPtr = dbBoard;
		} else {
			board.setBoard_id(NodePtr.ISSUE_NEW_BOARD_ID);
		}
		
		return createArticleAndHistory(board, dbParentPtr.getVersion() + 1, status, dbParentPtr);
	}
	
	private boolean isLeaf(final NodePtr nodePtr) {
		Board board = boardMapper.viewDetail(nodePtr.toMap());
		
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
	 * @param deletePtr 삭제할 버전에 대한 정보.
	 * @return 새로운 리프 노드의 주소. 새로운 리프노드를 생성하지 않았으면 null을 반환함.
	 */
	@Transactional
	public NodePtr deleteVersion(final NodePtr deletePtr) {
		BoardHistory deleteHistory = boardHistoryMapper.getHistory(deletePtr);
		NodePtr parentPtr = deleteHistory.getParentPtrAndRoot();
		
		List<BoardHistory> deleteNodeChildren = boardHistoryMapper.getChildren(deletePtr);

		int deletedCnt = boardMapper.boardDelete(deletePtr.toMap());		// 임시 저장 게시글이 존재 할 수 도 있으므로 history를 지우기 위해서는 필요
		deletedCnt = boardHistoryMapper.deleteHistory(deletePtr);
		if(deletedCnt != 1) {
			String json = Utils.jsonStringIfExceptionToString(deletePtr);
			throw new RuntimeException("deleteVersion메소드에서 게시글이력 테이블 삭제 에러 deletedCnt : " + deletedCnt + "\ndeletePtr : " + json);
		}

		if(deleteNodeChildren.size() == 0) {	// 리프 노드라면
			BoardHistory parentHistory = boardHistoryMapper.getHistory(parentPtr);
			
			Board parent = new Board(parentHistory);
			List<BoardHistory> parentChildren = boardHistoryMapper.getChildren(parent);
			if(parentChildren.size() == 0 && parentHistory.isRoot()) {// 루트만 존재하는 경우에는 루트를 지워줍니다.
				deletedCnt = boardHistoryMapper.deleteHistory(parentHistory);
				if(deletedCnt != 1) {
					String json = Utils.jsonStringIfExceptionToString(parentHistory);
					throw new RuntimeException("deleteVersion메소드에서 게시글이력 테이블 삭제 에러 deletedCnt : " + deletedCnt + "\ndeletePtr : " + json);
				}
			}
			else if(parentChildren.size() == 0 && !parentHistory.isRoot()){	// 루트가 아닌 리프 노드는 게시물 게시판에 존재해야 함.
				try {
					String content = Compress.deCompress(parentHistory.getHistory_content());
					parent.setContent(content);
				} catch(IOException e) {
					e.printStackTrace();
					String history = Utils.jsonStringIfExceptionToString(parentHistory);
					throw new RuntimeException("deleteVersion메소드에서 압축 해제 실패 \nhistory : " + history);
				}
				int createdCnt = boardMapper.boardCreate(parent);
				if(createdCnt == 0) {
					throw new RuntimeException("deleteVersion메소드에서 DB의 board테이블 리프 노드를 갱신(board에서)시 발생" +
							"deleteRowCnt : " + deletedCnt + " createdCnt : " + createdCnt);
				}
				return parent;
			}
		}	// 리프 노드일때 끝
		else if(deleteHistory.isRoot()) {
			throw new RuntimeException("루트는 삭제할 수 없습니다.");
			// TODO : 한꺼번에 업데이트 하는 방법?
		}
		else {// 중간노드 일 경우
			for(BoardHistory childHistory : deleteNodeChildren) {
				childHistory.setParentNodePtrAndRoot(parentPtr);
				int updatedCnt = boardHistoryMapper.updateHistoryParentAndRoot(childHistory);
				if(updatedCnt != 1) {
					String json = Utils.jsonStringIfExceptionToString(childHistory);
					throw new RuntimeException("updateRowCnt expected 1 but : " + updatedCnt + "\n" +
												"in " + json);
				}
			}
		}
		return null;
	}
	
	public NodePtr findAncestorHaveAnotherChild(NodePtr curPtr) {
		List<BoardHistory> children;
		do {
			BoardHistory boardHistory = boardHistoryMapper.getHistory(curPtr);
			curPtr = boardHistory.getParentPtrAndRoot();
			children = boardHistoryMapper.getChildren(curPtr);
		} while(children.size() != 1);
		
		return curPtr;
	}
	
	/**
	 * 특정 게시글을 삭제합니다. 부모를 모두 삭제합니다. (단, 형제 노드가 존재 할때까지)
	 * 자동 저장 게시글이 있다면 전부 삭제됩니다.!!!!
	 * @param leafPtr 리프 노드만 주어야합니다.
	 * @throws NotLeafNodeException 리프 노드가 아닌 것을 삭제할때 발생됨.
	 */
	@Transactional
	public void deleteArticle(NodePtr leafPtr) throws NotLeafNodeException {
		boolean deleteFileBoolean = false;
		if(!isLeaf(leafPtr)) {
			String leafPtrJson = Utils.jsonStringIfExceptionToString(leafPtr);
			throw new NotLeafNodeException("node 정보" + leafPtrJson);
		}
		int deletedCnt = boardMapper.boardDelete(leafPtr.toMap());
		if(deletedCnt == 0) {
			String leafPtrJson = Utils.jsonStringIfExceptionToString(leafPtr);
			throw new RuntimeException("deleteArticle에서 게시글 삭제 실패 leafPtrJson : " + leafPtrJson);
		}
		
		while(true) {
			BoardHistory deleteHistory = boardHistoryMapper.getHistory(leafPtr);
			if(deleteHistory == null) {
				break;
			}
			NodePtr parentPtr = deleteHistory.getParentPtrAndRoot();
			int file_id = deleteHistory.getFile_id();
			if(file_id != 0) {
				int fileCount = boardHistoryMapper.getFileCount(file_id);
				if(fileCount ==1) {
					deleteFileBoolean=true;
				}
			}
			deletedCnt = boardMapper.boardDelete(leafPtr.toMap());
			deletedCnt = boardHistoryMapper.deleteHistory(leafPtr);
			if(deletedCnt == 0) {
				throw new RuntimeException("deleteArticle메소드에서 게시글이력 테이블 삭제 에러 deletedCnt : " + deletedCnt);
			}
			if(deleteFileBoolean) {
				deletedCnt = fileMapper.deleteFile(file_id);
				if(deletedCnt != 1) {
					throw new RuntimeException("파일 삭제 에러");
				};
			}
			List<BoardHistory> children = boardHistoryMapper.getChildren(parentPtr);
			
			if(children.size() >= 1) {
				break;
			}
			leafPtr = parentPtr;
		}
	}
	
	// TODO : 임시 게시글 만들기.
	@Transactional
	public void createTempArticleOverwrite(Board tempArticle) {
		boolean deleteFileBoolean = false;
		tempArticle.setRoot_board_id(tempArticle.getBoard_id());			// getHistoryByRootId에서 검색이 가능하도록

		Board dbTempArticle = boardMapper.viewDetail(tempArticle.toMap());
		
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
