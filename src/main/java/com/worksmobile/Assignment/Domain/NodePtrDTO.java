package com.worksmobile.Assignment.Domain;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
public class NodePtrDTO {
	@Getter @Setter protected Integer board_id = null;
	@Getter @Setter protected Integer version = null;
	@Getter @Setter protected int root_board_id = ISSUE_NEW_BOARD_ID;
	
	public static final int LEAF_NODE_COOKIE_ID = 0;
	public static final Integer ISSUE_NEW_BOARD_ID = -2;
	public static final int ROOT_BOARD_ID = -1;
	
	public NodePtrDTO() { }
	public NodePtrDTO(Integer board_id, Integer version) {
		this.board_id = board_id;
		this.version = version;
	}

	public HashMap<String, Integer> toMap() {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("board_id", board_id);
		map.put("version", version);
		map.put("cookie_id", LEAF_NODE_COOKIE_ID);
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

