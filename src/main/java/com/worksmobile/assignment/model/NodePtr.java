package com.worksmobile.assignment.model;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NodePtr {
	protected Integer board_id = null;
	protected Integer version = null;
	protected int root_board_id;

	public static final int INVISIBLE_ROOT_VERSION = 0;
	public static final int VISIBLE_ROOT_VERSION = INVISIBLE_ROOT_VERSION + 1;
	public static final int INVISIALBE_ROOT_BOARD_ID = 0;
	public static final Integer ISSUE_NEW_BOARD_ID = null;

	public NodePtr() {}

	public NodePtr(Integer board_id, Integer version) {
		this.board_id = board_id;
		this.version = version;
	}
	
	public NodePtr(NodePtr nodePtr) {
		this.board_id = nodePtr.getBoard_id();
		this.version = nodePtr.getVersion();
		this.root_board_id = nodePtr.getRoot_board_id();
	}

	/***
	 * 
	 * @return cookie_id은 리프 쿠키 아이디로 설정됩니다.
	 */
	public HashMap<String, Object> toMap() {
		HashMap<String, Object> map = new HashMap<>();
		map.put("board_id", board_id);
		map.put("version", version);
		return map;
	}

	@Override
	public String toString() {
		return String.format("%d-%d", board_id, version);
	}

	public Map.Entry<Integer, Integer> toBoardIdAndVersionEntry() {
		return new AbstractMap.SimpleEntry<>(board_id, version);
	}
}
