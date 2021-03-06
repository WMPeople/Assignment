package com.worksmobile.assignment.model;

import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class Board extends NodePtr{
	private String subject;
	private String content;
	private String created_time;
	private int file_id;

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
