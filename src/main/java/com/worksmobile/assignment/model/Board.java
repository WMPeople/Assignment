package com.worksmobile.assignment.model;

import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Board extends NodePtr{
	@Setter @Getter private String subject;
	@Setter @Getter private String content;
	@Setter @Getter private String created_time;
	@Setter @Getter private int file_id;

	public Board() {}

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
		return map;
	}

}
