package com.worksmobile.assignment.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

/***
 * 
 * @author KHH
 *
 */
@Service
public class VersionManagementService {

	@Autowired
	private BoardMapper boardMapper;
	
	@Autowired
	private BoardHistoryMapper boardHistoryMapper;
	
	@Autowired
	private BoardService boardService;
	
	@Autowired
	private BoardHistoryService boardHistoryService;
	
	/***
	 * 한 게시글과 연관된 모든 게시글 이력을 반환합니다.
	 * @param leafPtr 가져올 리프 노드 포인터.(board_id, version만 사용)
	 * @return 연관된 게시글 히스토리들
	 * @throws NotLeafNodeException 리프 노드가 아닌 것을 삭제할때 발생됩.
	 */
	public List<BoardHistory> getRelatedHistory(NodePtr leafPtr) throws NotLeafNodeException{
		Board board = boardMapper.viewDetail(leafPtr.toMap());
		if(board == null) {
			String leafPtrJson = JsonUtils.jsonStringIfExceptionToString(leafPtr);
			throw new NotLeafNodeException("leaf node 정보" + leafPtrJson);
		}

		Map<Map.Entry<Integer, Integer>, BoardHistory> boardHisotryMap = boardHistoryService.getHistoryMap(board.getRoot_board_id());
		List<BoardHistory> relatedHistoryList = new ArrayList<>(boardHisotryMap.size());
		
		NodePtr curPosPtr = leafPtr;
		BoardHistory leafHistory;
		do {
			leafHistory = boardHisotryMap.get(curPosPtr.toBoardIdAndVersionEntry());
			if(leafHistory == null) {
				String curPosJson = JsonUtils.jsonStringIfExceptionToString(curPosPtr);
				String historyListJson = JsonUtils.jsonStringIfExceptionToString(boardHisotryMap);
				throw new RuntimeException("getRelatedHistory에서 노드 포인트가 history에 존재하지 않음" + curPosJson  + "\n" +
											"listCnt : " + relatedHistoryList.size() + ", content : " + historyListJson);
			}
			relatedHistoryList.add(leafHistory);
			curPosPtr = leafHistory.getParentPtrAndRoot();
		}
		while(leafHistory.getParent_version() != NodePtr.INVISIBLE_ROOT_VERSION);

		return relatedHistoryList;
	}
	
	/***
	 * 최초 기록 게시글을 등록합니다. 
	 * @param article 게시글에 대한 정보.
	 * @return 새로운 게시글의 이력
	 */
	@Transactional
	public BoardHistory createArticle(Board article) {
		article.setBoard_id(NodePtr.ISSUE_NEW_BOARD_ID);
		return createArticleAndHistory(article, NodePtr.INVISIBLE_ROOT_VERSION, BoardHistory.STATUS_CREATED, new NodePtr());
	}

	/**
	 *  게시판 DB 와 이력 DB 에 둘다 등록합니다.
	 *  루트를 만들거나, 충돌 관리시에 게시글 번호가 새로 발급됩니다. (DB의 자동 증가를 통해 증가)
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
		if(article.getSubject().length() == 0) {
			throw new RuntimeException("제목이 비어있을 수 없습니다.");
		}
		if(article.getContent() == null || article.getContent().length() == 0) {
			throw new RuntimeException("내용이 null 이거나 비어 있습니다. content : " + article.getContent());
		}

		BoardHistory createdHistory;
		if(version == 0) {	// 루트 노드일 경우
			BoardHistory rootHistory = boardHistoryService.createInvisibleRoot();
			
			createdHistory = boardHistoryService.createVisibleRoot(article, rootHistory, status);
		} else {// 루트가 아닌 리프 노드일 경우 (중간 노드일 경우는 없음)
			createdHistory = boardHistoryService.createLeafHistory(article, version, status, parentNodePtr);
		}
		
		article.setNodePtr(createdHistory);
		article.setCreated_time(createdHistory.getCreated_time());
		
		int insertedRowCnt = boardMapper.boardCreate(article);
		if(insertedRowCnt != 1) {
			throw new RuntimeException("createArticle메소드에서 boardCreate error" + createdHistory);
		}
		
		return createdHistory;
	}
	
	/***
	 * 버전 복구 기능입니다. board DB및  boardHistory 둘다 등록 됩니다.
	 * @param recoverPtr 복구할 버전에 대한 포인터.
	 * @param leafPtr 복구 후 부모가 될 리프 포인터.
	 * @return 새롭게 등록된 버전에 대한 포인터.
	 */
	@Transactional
	public NodePtr recoverVersion(final NodePtr recoverPtr, final NodePtr leafPtr)
	{
		BoardHistory recoverHistory = boardHistoryMapper.selectHistory(recoverPtr);
		BoardHistory leafHistory = boardHistoryMapper.selectHistory(leafPtr);
		if(leafHistory == null || recoverHistory == null)
		{
			String json = JsonUtils.jsonStringIfExceptionToString(leafHistory);
			json += "\n";
			json += JsonUtils.jsonStringIfExceptionToString(recoverHistory);
			throw new RuntimeException("recoverVersion에서 복구할 게시글 이력이 존재하지 않습니다. \nleafHistory : " + json);
		}
		
		Board recoveredBoard = new Board(recoverHistory);
		recoveredBoard.setContent(Compress.deCompressHistoryContent(recoverHistory));
		String status = String.format("%s(%s)", BoardHistory.STATUS_RECOVERED, recoverPtr.toString());
		return createVersionWithBranch(recoveredBoard, leafPtr, status);
	}
	
	/***
	 * 새로운 버전을 등록합니다. 충돌 관리가 적용되어 있습니다.
	 * @param modifiedBoard 새롭게 등록될 게시글에 대한 정보. cookie_id가 존재하면 자동 저장 내역이 삭제 됩니다.
	 * @param parentPtr 부모를 가리키는 노드 포인터.
	 * @param cookieId 자신의 쿠키 id, 자동 저장에서 삭제 됩니다.
	 * @return 새롭게 생성된 리프 노드를 가리킵니다.
	 */
	@Transactional
	public NodePtr modifyVersion(Board modifiedBoard, NodePtr parentPtr, String cookieId) {
		if(!Board.LEAF_NODE_COOKIE_ID.equals(cookieId)) {
			HashMap<String, Object> deleteParams = modifiedBoard.toMap();
			deleteParams.put("cookie_id", cookieId);
			boardService.deleteBoardWithCookieId(deleteParams);
		}
		if(modifiedBoard.getContent() == null || modifiedBoard.getContent().length() == 0) {
			throw new RuntimeException("내용이 null 이거나 비어 있습니다. content : " + modifiedBoard.getContent());
		}
		return createVersionWithBranch(modifiedBoard, parentPtr, BoardHistory.STATUS_MODIFIED);
	}
	
	/***
	 * 새로운 게시글 번호로 할당할지를 판단 하여 생성합니다.
	 * 새로운 리프를 등록하기 때문에 리프 노드 경우엔 삭제 됩니다. 단, 자동저장 게시글은 삭제 되지 않습니다.
	 * @param board 새로운 버전의 게시글의 내용
	 * @param parentPtr 부모가 될 노드의 포인터
	 * @param status 게시글 이력에 남길 상태
	 * @return 생성된 게시글의 포인터
	 */
	private NodePtr createVersionWithBranch(Board board, NodePtr parentPtr, final String status) {
		NodePtr dbParentPtr = boardHistoryMapper.selectHistory(parentPtr); // 클라이언트에서 root_board_id를 주지 않았을때를 위함.(또는 존재하지 않는 값을 줬을때)
		List<BoardHistory> childrenList= boardHistoryMapper.selectChildren(dbParentPtr);
		if(childrenList.size() == 0) {
			boardService.deleteBoardWithCookieId(dbParentPtr.toMap());
			board.setBoard_id(dbParentPtr.getBoard_id());
		} else {
			board.setBoard_id(NodePtr.ISSUE_NEW_BOARD_ID);
		}
		
		return createArticleAndHistory(board, dbParentPtr.getVersion() + 1, status, dbParentPtr);
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
		BoardHistory deleteHistory = boardHistoryMapper.selectHistory(deletePtr);
		NodePtr parentPtr = deleteHistory.getParentPtrAndRoot();
		NodePtr rtnNewLeafPtr = null;
		
		List<BoardHistory> deleteNodeChildren = boardHistoryMapper.selectChildren(deletePtr);
		boardService.deleteBoardAndAutoSave(deletePtr);
		boardService.deleteBoardHistory(deletePtr);

		if(deleteNodeChildren.size() == 0) {	// 리프 노드라면
			// 이력 및 게시글을 지웁니다.
			BoardHistory parentHistory = boardHistoryMapper.selectHistory(parentPtr);
			
			Board parent = new Board(parentHistory);
			List<BoardHistory> brothers = boardHistoryMapper.selectChildren(parent);
			
			if(brothers.size() == 0) {
				if(parentHistory.isInvisibleRoot()) {// 부모가 안보이는 루트만 존재있으면 삭제합니다.
					boardService.deleteBoardHistory(parentHistory);
				} else {	// 부모가 안보이는 루트가 아닌 노드는 board테이블에 존재해야 함.
					parent.setContent(Compress.deCompressHistoryContent(parentHistory));
					int createdCnt = boardMapper.boardCreate(parent);
					if(createdCnt == 0) {
						throw new RuntimeException("deleteVersion메소드에서 DB의 board테이블 리프 노드를 갱신(board에서)시 발생" +
								" createdCnt : " + createdCnt);
					}
					rtnNewLeafPtr = parent;
				}
			}
		}	// 리프 노드일때 끝
		else if(deleteHistory.isInvisibleRoot()) {
			throw new RuntimeException("루트는 삭제할 수 없습니다.");
		}
		else {// 중간노드 일 경우
			for(BoardHistory childHistory : deleteNodeChildren) {
				childHistory.setParentNodePtrAndRoot(parentPtr);
				int updatedCnt = boardHistoryMapper.updateHistoryParentAndRoot(childHistory);
				if(updatedCnt != 1) {
					String json = JsonUtils.jsonStringIfExceptionToString(childHistory);
					throw new RuntimeException("updateRowCnt expected 1 but : " + updatedCnt + "\n" +
												"in " + json);
				}
			}
		}
		return rtnNewLeafPtr;
	}
	
	/**
	 * 특정 게시글을 삭제합니다. 부모를 모두 삭제합니다. (단, 형제 노드가 존재 할때까지)
	 * 자동 저장 게시글이 있다면 전부 삭제됩니다.!!!!
	 * @param leafPtr 리프 노드만 주어야합니다.
	 * @throws NotLeafNodeException 리프 노드가 아닌 것을 삭제할때 발생됨.
	 */
	@Transactional
	public void deleteArticle(NodePtr leafPtr) throws NotLeafNodeException {
		
		if(!boardService.isLeaf(leafPtr)) {
			String leafPtrJson = JsonUtils.jsonStringIfExceptionToString(leafPtr);
			throw new NotLeafNodeException("node 정보" + leafPtrJson);
		}
		
		boardService.deleteBoardAndAutoSave(leafPtr);
		
		while(true) {
			BoardHistory deleteHistory = boardHistoryMapper.selectHistory(leafPtr);
			if(deleteHistory == null) {
				break;
			}
			NodePtr parentPtr = deleteHistory.getParentPtrAndRoot();
			
			boardService.deleteBoardAndAutoSave(leafPtr);
			boardService.deleteBoardHistory(leafPtr);

			List<BoardHistory> brothers = boardHistoryMapper.selectChildren(parentPtr);
			
			if(brothers.size() != 0) {
				break;
			}
			leafPtr = parentPtr;
		}
	}

	@Transactional
	public Board createTempArticleOverwrite(Board tempArticle, String type) {
		tempArticle.setRoot_board_id(tempArticle.getBoard_id());			// getHistoryByRootId에서 검색이 가능하도록
		
		Board dbTempArticle = boardMapper.viewDetail(tempArticle.toMap());
		if(dbTempArticle != null) {
			if (("withfile").equals(type)) {
				int articleUpdatedCnt = boardMapper.boardUpdate(tempArticle);
				if(articleUpdatedCnt != 1 ) {
					String json = JsonUtils.jsonStringIfExceptionToString(tempArticle);
					throw new RuntimeException("createTempArticleOverwrite메소드에서 임시 게시글 수정 에러 tempArticle : " + json + "\n" +
					"articleUpdatedCnt : " + articleUpdatedCnt);
				}
				
			} else {
				int articleUpdatedCnt = boardMapper.boardUpdateWithoutFile(tempArticle);
				if(articleUpdatedCnt != 1 ) {
					String json = JsonUtils.jsonStringIfExceptionToString(tempArticle);
					throw new RuntimeException("createTempArticleOverwrite메소드에서 임시 게시글 수정 에러 tempArticle : " + json + "\n" +
					"articleUpdatedCnt : " + articleUpdatedCnt);
				}
			}
		}
		else {
			boardService.copyBoardAndCreateTempBoard(tempArticle);
		}
		return tempArticle;
	}

	
}
