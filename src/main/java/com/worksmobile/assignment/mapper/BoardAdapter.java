package com.worksmobile.assignment.mapper;

import java.io.IOException;

import com.worksmobile.assignment.bo.Compress;
import com.worksmobile.assignment.bo.CompressMaker;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

public class BoardAdapter {
	private static Compress compress = CompressMaker.getCompress();
	
	public static Board from(BoardHistory boardHistory) {
		Board board = new Board();
		board.setSubject(boardHistory.getHistory_subject());
		try {
			board.setContent(compress.deCompress(boardHistory.getHistory_content()));
		} catch (IOException e) {
			String json = JsonUtils.jsonStringIfExceptionToString(boardHistory);
			throw new RuntimeException("게시글 내용을 압축 해제에 실패하였습니다. \n게시글 : " + json);
		}
		board.setNodePtr((NodePtr)boardHistory);
		board.setFile_id(boardHistory.getFile_id());
		board.setCreated_time(boardHistory.getCreated_time());
		
		return board;
	}
	
	public static BoardHistory from(Board board) {
		BoardHistory boardHistory = new BoardHistory();
		boardHistory.setHistory_subject(board.getSubject());
		try {
			boardHistory.setHistory_content(compress.compress(board.getContent()));
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
