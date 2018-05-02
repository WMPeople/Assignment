package com.worksmobile.assignment.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	public BoardHistory createInvisibleRoot() {
		NodePtr nodePtr = new NodePtr(NodePtr.ISSUE_NEW_BOARD_ID, 0, NodePtr.ROOT_BOARD_ID);
		BoardHistory rootHistory = new BoardHistory(new Board(), nodePtr, BoardHistory.STATUS_ROOT);
		rootHistory.setHistory_subject("RootSub");
		rootHistory.setHistory_content(BoardHistory.EMPTY_BYTE_ARRAY);
		int insertedRowCnt = boardHistoryMapper.createHistory(rootHistory);
		if (insertedRowCnt != 1) {
			String json = JsonUtils.jsonStringIfExceptionToString(rootHistory);
			throw new RuntimeException("createArticleAndHistory메소드에서 게시글 이력 추가 에러 insertedRowCnt : " + insertedRowCnt
				+ "\nrootHistory : " + json);
		}
		return rootHistory;
	}

	public BoardHistory createVisibleRoot(Board article, BoardHistory rootHistory, String status) {
		BoardHistory boardHistory = new BoardHistory(article, rootHistory, status);
		boardHistory.setParentNodePtrAndRoot(rootHistory);
		boardHistory.setVersion(NodePtr.VISIBLE_ROOT_VERSION);
		boardHistory.setRoot_board_id(rootHistory.getBoard_id());

		byte[] compressedContent = Compress.compressArticleContent(article);
		boardHistory.setHistory_content(compressedContent);
		int insertedRowCnt = boardHistoryMapper.createHistory(boardHistory);
		if (insertedRowCnt != 1) {
			String json = JsonUtils.jsonStringIfExceptionToString(boardHistory);
			throw new RuntimeException("createArticleAndHistory메소드에서 게시글 이력 추가 에러 insertedRowCnt : " + insertedRowCnt
				+ "\nrootHistory : " + json);
		}
		return boardHistory;
	}

	public BoardHistory createLeafHistory(Board article, int version, String status, final NodePtr parentNodePtr) {
		NodePtr createdNodePtr;
		if (article.getBoard_id() == NodePtr.ISSUE_NEW_BOARD_ID) {
			createdNodePtr = new NodePtr(NodePtr.ISSUE_NEW_BOARD_ID, version, NodePtr.ROOT_BOARD_ID);
		} else {
			createdNodePtr = new NodePtr(parentNodePtr.getBoard_id(), version, parentNodePtr.getRoot_board_id());
		}

		BoardHistory boardHistory = new BoardHistory(article, createdNodePtr, status);
		boardHistory.setParentNodePtrAndRoot(parentNodePtr);

		byte[] compressedContent = Compress.compressArticleContent(article);
		boardHistory.setHistory_content(compressedContent);
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
}
