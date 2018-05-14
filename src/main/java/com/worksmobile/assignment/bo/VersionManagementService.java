package com.worksmobile.assignment.bo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worksmobile.assignment.bo.event.ArticleCreatedEvent;
import com.worksmobile.assignment.bo.event.ArticleDeletedEvent;
import com.worksmobile.assignment.bo.event.ArticleModifiedEvent;
import com.worksmobile.assignment.bo.event.ArticleRevoeredEvent;
import com.worksmobile.assignment.bo.event.AutoSaveDeleteRequestEvent;
import com.worksmobile.assignment.mapper.BoardAdapter;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

/***
 * 
 * @author khh
 *
 */
@Service
public class VersionManagementService {
	@Autowired
	private BoardService boardService;

	@Autowired
	private BoardHistoryService boardHistoryService;

	@Autowired
	private ApplicationEventPublisher publisher;
	
	/***
	 * 최초 기록 게시글을 등록합니다. 
	 * @param article 게시글에 대한 정보.
	 * @return 새로운 게시글의 이력
	 */
	@Transactional
	public Board createArticle(Board article) {
		article.setVersion(NodePtr.VISIBLE_ROOT_VERSION);
		
		boardService.createNewArticle(article);
		
		publisher.publishEvent(new ArticleCreatedEvent(article));
		
		return article;
	}
	
	/***
	 * 새로운 버전을 등록합니다. 충돌 관리가 적용되어 있습니다.
	 * 자동저장이 없을 수도 있으므로 자동저장 삭제를 확인하지 않습니다.
	 * @param modifiedArticle 새롭게 등록될 게시글에 대한 정보. cookie_id가 존재하면 자동 저장 내역이 삭제 됩니다.
	 * @param parentPtr 부모를 가리키는 노드 포인터.
	 * @param cookieId 자신의 쿠키 id, 자동 저장에서 삭제 됩니다.
	 * @return 새롭게 생성된 리프 노드를 가리킵니다.
	 */
	@Transactional
	public NodePtr modifyVersion(Board modifiedArticle, NodePtr parentPtr, String cookieId) {
		NodePtr dbParentPtr = boardService.selectArticle(parentPtr); // 클라이언트에서 root_board_id를 주지 않았을때를 위함.(또는 존재하지 않는 값을 줬을때)
		if(dbParentPtr == null) {
			dbParentPtr = boardHistoryService.selectHistory(parentPtr);
		}
		
		NodePtr newPtr = boardService.modifyArticle(modifiedArticle, dbParentPtr);
		
		publisher.publishEvent(new ArticleModifiedEvent(modifiedArticle, dbParentPtr, cookieId));
		
		return newPtr;
	}

	/***
	 * 버전 복구 기능입니다. board DB및  boardHistory 둘다 등록 됩니다.
	 * @param recoverPtr 복구할 버전에 대한 포인터.
	 * @param leafPtr 복구 후 부모가 될 리프 포인터. 리프 게시글인지 검사하지 않습니다.
	 * @return 새롭게 등록된 버전에 대한 포인터.
	 */
	@Transactional
	public NodePtr recoverVersion(final NodePtr recoverPtr, final NodePtr leafPtr) throws NotExistHistoryException{
		BoardHistory recoverHistory = boardHistoryService.selectHistory(recoverPtr);
		NodePtr dbParentPtr = boardHistoryService.selectHistory(leafPtr); // 클라이언트에서 root_board_id를 주지 않았을때를 위함.(또는 존재하지 않는 값을 줬을때)
		
		Board recoveredBoard = BoardAdapter.from(recoverHistory);

		NodePtr newPtr = boardService.modifyArticle(recoveredBoard, dbParentPtr);
		
		publisher.publishEvent(new ArticleRevoeredEvent(recoveredBoard, recoverHistory, dbParentPtr));
		
		return newPtr;
	}
	
	/**
	 * 특정 게시글을 삭제합니다. 부모를 모두 삭제합니다. (단, 형제 노드가 존재 할때까지)
	 * 자동 저장 게시글이 있다면 전부 삭제됩니다.!!!!
	 * @param leafPtr 리프 노드만 주어야합니다.
	 * @throws NotLeafNodeException 리프 노드가 아닌 것을 삭제할때 발생됨.
	 */
	@Transactional
	public void deleteArticle(NodePtr leafPtr) throws NotLeafNodeException {
		if (!boardService.isLeaf(leafPtr)) {
			String leafPtrJson = JsonUtils.jsonStringIfExceptionToString(leafPtr);
			throw new NotLeafNodeException("node 정보" + leafPtrJson);
		}
		boardHistoryService.selectHistory(leafPtr);		// check null
		
		boardService.deleteBoard(leafPtr);
		
		publisher.publishEvent(new ArticleDeletedEvent(leafPtr));
	}
	
	/*========================== 게시판에 대한 CUD 끝 ========================
	 * 
	 * ========================= 이력에 대한 RD 시작 ========================
	 */
	
	/***
	 * 특정 버전에 대한 이력 1개를 삭제합니다. leaf노드이면 게시글도 삭제 됩니다. 
	 * 부모의 이력은 삭제되지 않음을 유의 해야 합니다.
	 * 자동 저장 게시글도 함께 삭제 됩니다.!!!!
	 * @param deletePtr 삭제할 버전에 대한 정보.
	 * @return 새로운 리프 노드의 주소. 새로운 리프노드를 생성하지 않았으면 null을 반환함.
	 */
	@Transactional
	public NodePtr deleteVersion(final NodePtr deletePtr) {
		BoardHistory deleteHistory = boardHistoryService.selectHistory(deletePtr);
		NodePtr parentPtr = deleteHistory.getParentPtrAndRoot();
		NodePtr rtnNewLeafPtr = null;
		
		List<BoardHistory> deleteNodeChildren = boardHistoryService.selectChildren(deleteHistory);

		Set<Integer> deletedFileIds = new HashSet<>();
		List<NodePtr> deleteHistoryNodePtrs = new ArrayList<>(2);	

		if (boardService.isLeaf(deleteHistory)) { // 리프 노드라면
			boardService.deleteBoard(deleteHistory);
			
			BoardHistory parentHistory = boardHistoryService.selectHistory(parentPtr);
			List<BoardHistory> brothers = boardHistoryService.selectChildren(parentHistory);
			
			// 자신 밖에 없고 부모가 안보이는 루트가 아니면
			if(brothers.size() == 1 && !parentHistory.isInvisibleRoot()) {
				Board parent = BoardAdapter.from(parentHistory);
				boardService.createNewArticle(parent);
				rtnNewLeafPtr = parent;
			}
			// 여기 부터 이력
			boardHistoryService.deleteBoardHistory(deleteHistory);

			deletedFileIds.add(deleteHistory.getFile_id());
			deleteHistoryNodePtrs.add(deleteHistory);

			if (brothers.size() == 1 && parentHistory.isInvisibleRoot()) {	// 자신 밖에 없고 부모가 안보이는 루트면
				boardHistoryService.deleteBoardHistory(parentHistory);

				deletedFileIds.add(parentHistory.getFile_id());
				deleteHistoryNodePtrs.add(parentHistory);
			}
			publisher.publishEvent(new AutoSaveDeleteRequestEvent(deleteHistoryNodePtrs, deletedFileIds));
		} // 리프 노드일때 끝
		else if (deleteHistory.isInvisibleRoot()) {
			String json = JsonUtils.jsonStringIfExceptionToString(deleteHistory);
			throw new RuntimeException("안보이는 루트는 삭제할 수 없습니다. delete : " + json);
		} else {// 중간노드 일 경우
			boardHistoryService.changeParent(deleteNodeChildren, parentPtr);
			boardHistoryService.deleteBoardHistory(deleteHistory);
			
			deleteHistoryNodePtrs.add(deleteHistory);
			deletedFileIds.add(deleteHistory.getFile_id());
			publisher.publishEvent(new AutoSaveDeleteRequestEvent(deleteHistoryNodePtrs, deletedFileIds));
		}
		return rtnNewLeafPtr;
	}
	
	/***
	 * 한 게시글과 연관된 모든 게시글 이력을 반환합니다.
	 * @param leafPtr 가져올 리프 노드 포인터.(board_id, version만 사용)
	 * @return 연관된 게시글 히스토리들
	 * @throws NotLeafNodeException 리프 노드가 아닌 것을 삭제할때 발생됩.
	 */
	public List<BoardHistory> getRelatedHistory(NodePtr leafPtr) throws NotLeafNodeException {
		Board board = boardService.selectArticle(leafPtr);
		if (board == null) {
			String leafPtrJson = JsonUtils.jsonStringIfExceptionToString(leafPtr);
			throw new NotLeafNodeException("leaf node 정보" + leafPtrJson);
		}

		Map<Map.Entry<Integer, Integer>, BoardHistory> boardHisotryMap = boardHistoryService
			.getHistoryMap(board.getRoot_board_id());
		List<BoardHistory> relatedHistoryList = new ArrayList<>(boardHisotryMap.size());

		NodePtr curPosPtr = board;
		BoardHistory leafHistory;
		do {
			leafHistory = boardHisotryMap.get(curPosPtr.toBoardIdAndVersionEntry());
			if (leafHistory == null) {
				String curPosJson = JsonUtils.jsonStringIfExceptionToString(curPosPtr);
				String historyListJson = JsonUtils.jsonStringIfExceptionToString(boardHisotryMap);
				throw new RuntimeException("getRelatedHistory에서 노드 포인트가 history에 존재하지 않음" + curPosJson + "\n" +
					"listCnt : " + relatedHistoryList.size() + ", content : " + historyListJson);
			}
			relatedHistoryList.add(leafHistory);
			curPosPtr = leafHistory.getParentPtrAndRoot();
		} while (leafHistory.getParent_version() != NodePtr.INVISIBLE_ROOT_VERSION);

		return relatedHistoryList;
	}
}
