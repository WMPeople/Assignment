package com.worksmobile.assignment.Model;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

public class Board extends NodePtr{
	@Setter @Getter private String subject;
	@Setter @Getter private String content;
	@Setter @Getter private String created;
	@Setter @Getter private int file_id;
	@Setter @Getter private String cookie_id = LEAF_NODE_COOKIE_ID;
	
	public final static String LEAF_NODE_COOKIE_ID = "LEAF_NODE_COOKIE_ID";
	
	public Board() { }
	/***
	 * BoardHistory의 내용제외하고 공통된 내용을 전부 가져옵니다.
	 * 중요! : 내용을 압축 해제 역할은 담당하지 않습니다.
	 * @param boardHistory
	 */
	public Board(BoardHistory boardHistory) {
		setNodePtr(boardHistory);

		subject = boardHistory.getHistory_subject();
		content = null;
		created = boardHistory.getCreated();

		file_id = boardHistory.getFile_id();
	}

	public void setNodePtr(NodePtr nodePtr) {
		board_id = nodePtr.getBoard_id();
		version = nodePtr.getVersion();
		root_board_id = nodePtr.getRoot_board_id();
	}
	@Override
	public HashMap<String, Object> toMap() {
		HashMap<String, Object> map = new HashMap<>();
		map.put("board_id", board_id);
		map.put("version", version);
		map.put("cookie_id",cookie_id);
		return map;
	}
	
}
