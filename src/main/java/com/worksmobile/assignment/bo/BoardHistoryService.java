package com.worksmobile.assignment.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.worksmobile.assignment.mapper.BoardAdapter;
import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

/**
 * 
 * @author khh
 *
 */
@Service
public class BoardHistoryService {

	@Autowired
	private BoardHistoryMapper boardHistoryMapper;

	private BoardHistory createInvisibleRoot(int boardId, String createdTime) {
		NodePtr nodePtr = new NodePtr(boardId, NodePtr.INVISIBLE_ROOT_VERSION, NodePtr.INVISIALBE_ROOT_BOARD_ID);
		BoardHistory rootHistory = new BoardHistory();
		rootHistory.setNodePtr(nodePtr);
		rootHistory.setStatus(BoardHistory.STATUS_INVISIBLE_ROOT);
		rootHistory.setHistory_subject("invisibleRoot");
		rootHistory.setHistory_content("".getBytes());
		rootHistory.set_content_compressed(false);
		rootHistory.setCreated_time(createdTime);
		int insertedRowCnt = boardHistoryMapper.createHistory(rootHistory);
		if (insertedRowCnt != 1) {
			String json = JsonUtils.jsonStringIfExceptionToString(rootHistory);
			throw new RuntimeException("createArticleAndHistory메소드에서 게시글 이력 추가 에러 insertedRowCnt : " + insertedRowCnt
				+ "\nrootHistory : " + json);
		}
		return rootHistory;
	}

	private BoardHistory createVisibleRoot(Board article, NodePtr invisibleRoot, String status) {
		BoardHistory boardHistory = BoardAdapter.from(article);
		boardHistory.setParentNodePtrAndRoot(invisibleRoot);
		boardHistory.setStatus(status);
		boardHistory.setVersion(NodePtr.VISIBLE_ROOT_VERSION);
		boardHistory.setRoot_board_id(invisibleRoot.getBoard_id());

		int insertedRowCnt = boardHistoryMapper.createHistory(boardHistory);
		if (insertedRowCnt != 1) {
			String json = JsonUtils.jsonStringIfExceptionToString(boardHistory);
			throw new RuntimeException("createArticleAndHistory메소드에서 게시글 이력 추가 에러 insertedRowCnt : " + insertedRowCnt
				+ "\nrootHistory : " + json);
		}
		return boardHistory;
	}

	private BoardHistory createLeafHistory(Board article, String status, final NodePtr parentNodePtr) {
		BoardHistory boardHistory = BoardAdapter.from(article);
		boardHistory.setParentNodePtrAndRoot(parentNodePtr);
		boardHistory.setStatus(status);

		int insertedRowCnt = boardHistoryMapper.createHistory(boardHistory);
		if (insertedRowCnt != 1) {
			String json = JsonUtils.jsonStringIfExceptionToString(boardHistory);
			throw new RuntimeException("createArticleAndHistory메소드에서 게시글 이력 추가 에러 insertedRowCnt : " + insertedRowCnt
				+ "\nrootHistory : " + json);
		}
		return boardHistory;
	}

	public Map<Map.Entry<Integer, Integer>, BoardHistory> getHistoryMap(int root_board_id) {
		List<BoardHistory> historyList = boardHistoryMapper.selectHistoryByRootBoardId(root_board_id);
		Map<Map.Entry<Integer, Integer>, BoardHistory> historyMap = new HashMap<>();
		for (BoardHistory ele : historyList) {
			historyMap.put(ele.toBoardIdAndVersionEntry(), ele);
		}
		return historyMap;
	}
	
	public BoardHistory createHistory(Board article, final String status, final NodePtr parentNodePtr) {
		BoardHistory createdHistory;
		if (article.getVersion() == NodePtr.VISIBLE_ROOT_VERSION) { // 루트 노드일 경우
			BoardHistory rootHistory = createInvisibleRoot(article.getBoard_id(), article.getCreated_time());
			
			createdHistory = createVisibleRoot(article, rootHistory, status);
		} else {// 루트가 아닌 리프 노드일 경우 (중간 노드일 경우는 없음)
			createdHistory = createLeafHistory(article, status, parentNodePtr);
		}
		return createdHistory;
	}
	
	public BoardHistory selectHistory(final NodePtr nodePtr) throws NotExistNodePtrException{
		BoardHistory boardHistory = boardHistoryMapper.selectHistory(nodePtr);
		if (boardHistory == null) {
			throw new NotExistNodePtrException("존재하지 않는 이력 포인터 입니다. nodePtr : " + nodePtr);
		}
		return boardHistory;
	}
	
	public void updateHistoryLock(NodePtr nodePtr, boolean oldIsLock, boolean newIsLock) {
		int updatedCnt = boardHistoryMapper.updateHistoryLock(nodePtr, oldIsLock, newIsLock);
		if(updatedCnt != 1) {
			throw new NotExistNodePtrException("존재 하지 않는 이력 포인터 입니다. nodePtr : " + nodePtr + " locked : " + oldIsLock);
		}
	}
	
	public void changeParent(List<BoardHistory> childrenHistoryList, NodePtr parentPtr) {
		for (BoardHistory childHistory : childrenHistoryList) {
			childHistory.setParentNodePtrAndRoot(parentPtr);
			int updatedCnt = boardHistoryMapper.updateHistoryParentAndRoot(childHistory);
			if (updatedCnt != 1) {
				String json = JsonUtils.jsonStringIfExceptionToString(childHistory);
				throw new RuntimeException("updateRowCnt expected 1 but : " + updatedCnt + "\n" +
					"in " + json);
			}
		}
	}
	
	public List<BoardHistory> selectChildren(NodePtr nodePtr) {
		return boardHistoryMapper.selectChildren(nodePtr);
	}

	public void deleteBoardHistory(NodePtr leafPtr) {
		int deletedCnt = boardHistoryMapper.deleteHistory(leafPtr);
		if(deletedCnt != 1) {
			throw new RuntimeException("deletedCnt expected 1 but : " + deletedCnt + "nodePtr : " + leafPtr);
		}
	}
}
