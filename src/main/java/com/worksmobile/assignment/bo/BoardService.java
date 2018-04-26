﻿package com.worksmobile.assignment.bo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.mapper.BoardTempMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.BoardTemp;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

/***
 * Board, BoardHistory 삭제할 경우 이 서비스를 통해 삭제 됩니다.
 * @author khh
 * @author rws
 *
 */
@Service
public class BoardService {
	@Autowired
	private BoardMapper boardMapper;

	@Autowired
	private BoardHistoryMapper boardHistoryMapper;

	@Autowired
	private BoardTempMapper boardTempMapper;

	@Autowired
	private FileService fileService;

	/***
	 * 리프 게시글과 관련된 자동 저장 게시글을 함께 삭제합니다.
	 * @param deleteNodePtr 삭제할 노드의 포인터
	 * @return 삭제된 개수
	 */
	public int deleteBoardAndAutoSave(NodePtr deleteNodePtr) {
		List<Board> boardList = boardMapper.getBoardList(deleteNodePtr);
		List<BoardTemp> boardTempList = boardTempMapper.getBoardTempList(deleteNodePtr);
		if (boardList.size() == 0) {
			return 0;
		}
		Set<Integer> fileIdSet = new HashSet<>();
		for (Board ele : boardList) {
			fileIdSet.add(ele.getFile_id());
		}

		for (BoardTemp ele : boardTempList) {
			fileIdSet.add(ele.getFile_id());
		}
		int deletedCnt = boardTempMapper.deleteBoardTemp(deleteNodePtr.toMap());
		deletedCnt += boardMapper.deleteBoard(deleteNodePtr.toMap());

		fileService.deleteNoMoreUsingFile(fileIdSet);

		return deletedCnt;
	}

	/***
	 * 만약, 존재하지 않는 게시글이라면 삭제 되지 않습니다.
	 * @param deleteParams 삭제할 board의 board_id, version, cookie_id를 사용합니다.
	 * @return 삭제 되었는지 여부
	 */
	public boolean deleteBoard(HashMap<String, Object> deleteParams) {
		Board dbBoard = boardMapper.viewDetail(deleteParams);
		if (dbBoard == null) {
			return false;
		}
		Set<Integer> fileIdSet = new HashSet<>();
		fileIdSet.add(dbBoard.getFile_id());

		int deletedCnt = boardMapper.deleteBoard(deleteParams);
		if (deletedCnt != 1) {
			String json = JsonUtils.jsonStringIfExceptionToString(deleteParams);
			throw new RuntimeException("Board 테이블의 게시글 삭제 중 오류가 발생했습니다. (특정 쿠키 값을 가진 Board삭제) \n" +
				"삭제된 개수 : " + deletedCnt + " deleteParams : " + json);
		}
		fileService.deleteNoMoreUsingFile(fileIdSet);

		return true;
	}

	public void deleteBoardHistory(NodePtr deleteNodePtr) {
		BoardHistory history = boardHistoryMapper.selectHistory(deleteNodePtr);
		Set<Integer> fileIdSet = new HashSet<>(1);
		fileIdSet.add(history.getFile_id());

		int deletedCnt = boardHistoryMapper.deleteHistory(deleteNodePtr);
		if (deletedCnt != 1) {
			String json = JsonUtils.jsonStringIfExceptionToString(deleteNodePtr);
			throw new RuntimeException("이력 삭제 실패  삭제 개수 : " + deletedCnt + " deletePtr : " + json);
		}

		fileService.deleteNoMoreUsingFile(fileIdSet);
	}

	public void deleteBoardHistoryAndAttachment(NodePtr nodePtr) {
		List<Board> boardList = boardMapper.getBoardList(nodePtr);
		Set<Integer> fileIdSet = new HashSet<>();
		for (int i = 0; i < boardList.size(); i++) {
			fileIdSet.add(boardList.get(i).getFile_id());
		}
		boardMapper.deleteBoard(nodePtr.toMap());
		boardTempMapper.deleteBoardTemp(nodePtr.toMap());

		fileService.deleteNoMoreUsingFile(fileIdSet);
	}

	public Set<Integer> deleteBoardAndReturnfileIdSet(NodePtr leafPtr) {
		List<Board> boardList = boardMapper.getBoardList(leafPtr);
		Set<Integer> fileIdSet = new HashSet<>();
		for (int i = 0; i < boardList.size(); i++) {
			fileIdSet.add(boardList.get(i).getFile_id());
		}
		boardMapper.deleteBoard(leafPtr.toMap());
		boardTempMapper.deleteBoardTemp(leafPtr.toMap());
		return fileIdSet;
	}

	public int deleteBoardHistoryAndReturnfileId(NodePtr leafPtr) {
		BoardHistory boardHistory = boardHistoryMapper.selectHistory(leafPtr);

		boardHistoryMapper.deleteHistory(leafPtr);

		return boardHistory.getFile_id();
	}

	public boolean isLeaf(final NodePtr nodePtr) {
		Board board = boardMapper.viewDetail(nodePtr.toMap());

		if (board != null) {
			return true;
		} else {
			return false;
		}
	}

}