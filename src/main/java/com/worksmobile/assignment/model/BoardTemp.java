package com.worksmobile.assignment.model;

import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * BoardTemp Model
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper=true)
@AllArgsConstructor
public class BoardTemp extends NodePtr {
	private String subject;
	private String content;
	private String created_time;
	private int file_id;
	private String cookie_id;
	
	public BoardTemp() {}

	public BoardTemp(Board board) {
		setNodePtr(board);
	}

	public void setNodePtr(NodePtr nodePtr) {
		board_id = nodePtr.getBoard_id();
		version = nodePtr.getVersion();
	}

	@Override
	public HashMap<String, Object> toMap() {
		HashMap<String, Object> map = new HashMap<>();
		map.put("board_id", board_id);
		map.put("version", version);
		map.put("cookie_id", cookie_id);
		return map;
	}

	public HashMap<String, Object> toBoardKeyMap() {
		HashMap<String, Object> map = new HashMap<>();
		map.put("board_id", board_id);
		map.put("version", version);
		return map;
	}

}
