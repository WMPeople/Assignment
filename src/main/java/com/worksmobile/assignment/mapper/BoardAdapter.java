package com.worksmobile.assignment.mapper;

import java.io.IOException;

import com.worksmobile.assignment.bo.BoardHistoryCompress;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

public class BoardAdapter {
	
	public static Board from(BoardHistory boardHistory) {
		Board board = new Board();
		board.setSubject(boardHistory.getHistory_subject());
		board.setNodePtr((NodePtr)boardHistory);
		board.setFile_id(boardHistory.getFile_id());
		board.setCreated_time(boardHistory.getCreated_time());
		
		try {
			board.setContent(BoardHistoryCompress.getDeCompressedContent(boardHistory));
		} catch (IOException e) {
			String json = JsonUtils.jsonStringIfExceptionToString(boardHistory);
			throw new RuntimeException("게시글 내용을 압축 해제에 실패하였습니다. \n게시글 : " + json);
		}
		
		return board;
	}
	
	public static BoardHistory from(Board board) {
		BoardHistory boardHistory = new BoardHistory();
		boardHistory.setHistory_subject(board.getSubject());
		try {
			boardHistory = BoardHistoryCompress.setContent(boardHistory, board.getContent());
		} catch (IOException e) {
			String json = JsonUtils.jsonStringIfExceptionToString(board);
			throw new RuntimeException("게시글 내용을 압축에 실패하였습니다. \n게시글 : " + json);
		}
		boardHistory.setNodePtr(board);
		boardHistory.setFile_id(board.getFile_id());
		boardHistory.setCreated_time(board.getCreated_time());
		
		return boardHistory;
	}
}
