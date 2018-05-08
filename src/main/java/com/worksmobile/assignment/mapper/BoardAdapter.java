package com.worksmobile.assignment.mapper;

import java.io.IOException;

import com.worksmobile.assignment.bo.BoardHistoryCompression;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

public class BoardAdapter {
	
	public static Board from(BoardHistory boardHistory) {
		Board article = new Board();
		article.setSubject(boardHistory.getHistory_subject());
		article.setNodePtr((NodePtr)boardHistory);
		article.setFile_id(boardHistory.getFile_id());
		article.setCreated_time(boardHistory.getCreated_time());
		
		try {
			article.setContent(BoardHistoryCompression.getDeCompressedContent(boardHistory));
		} catch (IOException e) {
			String json = JsonUtils.jsonStringIfExceptionToString(boardHistory);
			throw new RuntimeException("게시글 내용을 압축 해제에 실패하였습니다. \n게시글 : " + json);
		}
		
		return article;
	}
	
	public static BoardHistory from(Board board) {
		BoardHistory boardHistory = new BoardHistory();
		boardHistory.setHistory_subject(board.getSubject());
		try {
			boardHistory = BoardHistoryCompression.setContent(boardHistory, board.getContent());
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
