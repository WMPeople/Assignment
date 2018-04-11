package com.worksmobile.assignment.bo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;


/***
 * Board, BoardHistory 삭제할 경우 이 서비스를 통해 삭제 됩니다.
 * 현재는 DAO 같은 역할을 하고 있습니다.
 * @author KHH
 *
 */
@Service
class BoardService{
	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;
	
	@Autowired
	FileService fileService;
	
	public void deleteBoardAndAutoSave(NodePtr deleteNodePtr) {
		List<Board> boardList = boardMapper.getBoardList(deleteNodePtr);
		Set<Integer> fileIdSet = new HashSet<>();
		for(Board ele : boardList) {
			fileIdSet.add(ele.getFile_id());
		}
		boardMapper.deleteBoardAndAutoSave(deleteNodePtr.toMap());

		fileService.deleteNoMoreUsingFile(fileIdSet);
	}
	
	// TODO : deleteNoMoreUsingFile메소드가 Set을 매개변수로 취하고 있으나, 한개 만을 넘기는 경우가 있어
	// 이를 개선할 방안이 있으면 좋을것 같습니다.
	/***
	 * 만약, 존재하지 않는 게시글이라면 삭제 되지 않습니다.
	 * @param deleteParams 삭제할 board의 board_id, version, cookie_id를 사용합니다.
	 */
	public void deleteBoardWithCookieId(HashMap<String, Object> deleteParams) {
		Board dbBoard = boardMapper.viewDetail(deleteParams);
		if(dbBoard == null) {
			return;
		}
		Set<Integer> fileIdSet = new HashSet<>();
		fileIdSet.add(dbBoard.getFile_id());
		
		int deletedCnt = boardMapper.deleteBoardWithCookieId(deleteParams);
		if(deletedCnt != 1) {
			String json = JsonUtils.jsonStringIfExceptionToString(deleteParams);
			throw new RuntimeException("Board 테이블의 게시글 삭제 중 오류가 발생했습니다. (특정 쿠키 값을 가진 Board삭제) \n" +
			"삭제된 개수 : " + deletedCnt + " deleteParams : " + json);
		}
		fileService.deleteNoMoreUsingFile(fileIdSet);
	}
	
	public void deleteBoardHistory(NodePtr deleteNodePtr) {
		BoardHistory history = boardHistoryMapper.selectHistory(deleteNodePtr);
		Set<Integer> fileIdSet = new HashSet<>(1);
		fileIdSet.add(history.getFile_id());
		
		int deletedCnt = boardHistoryMapper.deleteHistory(deleteNodePtr);
		if(deletedCnt != 1) {
			String json = JsonUtils.jsonStringIfExceptionToString(deleteNodePtr);
			throw new RuntimeException("이력 삭제 실패  삭제 개수 : " + deletedCnt + " deletePtr : " + json);
		}
		
		fileService.deleteNoMoreUsingFile(fileIdSet);
	}
	
	public void deleteBoardHistoryAndAttachment(NodePtr nodePtr) {
		List<Board> boardList = boardMapper.getBoardList(nodePtr);
		Set<Integer> fileIdSet = new HashSet<>();
		for(int i=0;i<boardList.size();i++) {
			fileIdSet.add(boardList.get(i).getFile_id());
		}
		boardMapper.deleteBoardAndAutoSave(nodePtr.toMap());

		fileService.deleteNoMoreUsingFile(fileIdSet);	
	}
	
	public Set<Integer> deleteBoardAndReturnfileIdSet(NodePtr leafPtr) {
		List<Board> boardList = boardMapper.getBoardList(leafPtr);
		Set<Integer> fileIdSet = new HashSet<>();
		for(int i=0;i<boardList.size();i++) {
			fileIdSet.add(boardList.get(i).getFile_id());
		}
		boardMapper.deleteBoardAndAutoSave(leafPtr.toMap());
		return fileIdSet;
	}
	
	public int deleteBoardHistoryAndReturnfileId(NodePtr leafPtr) {
		BoardHistory boardHistory = boardHistoryMapper.selectHistory(leafPtr);

		boardHistoryMapper.deleteHistory(leafPtr);
		
		return boardHistory.getFile_id();
	}

	Map<Map.Entry<Integer, Integer>, BoardHistory> getHistoryMap(int root_board_id) {
		List<BoardHistory> historyList = boardHistoryMapper.selectHistoryByRootBoardId(root_board_id);
		Map<Map.Entry<Integer, Integer>, BoardHistory> historyMap = new HashMap<>();
		for(BoardHistory ele : historyList) {
			historyMap.put(ele.toBoardIdAndVersionEntry(), ele);
		}
		return historyMap;
	}

	public boolean isLeaf(final NodePtr nodePtr) {
		Board board = boardMapper.viewDetail(nodePtr.toMap());
		
		if(board != null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
}