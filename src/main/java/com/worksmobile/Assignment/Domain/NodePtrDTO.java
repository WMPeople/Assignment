package com.worksmobile.Assignment.Domain;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

public class NodePtrDTO {
	@Getter @Setter protected Integer board_id = null;
	@Getter @Setter protected Integer version = null;
	
	public static final NodePtrDTO DEFAULT_NULL_NODE_PTR = new NodePtrDTO();
	
	public NodePtrDTO() { }
	public NodePtrDTO(Integer board_id, Integer version) {
		this.board_id = board_id;
		this.version = version;
	}

	public HashMap<String, Integer> toMap() {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("board_id", board_id);
		map.put("version", version);
		return map;
	}
	
	private static boolean checkEquals(Integer lhs, Integer rhs) {
		return lhs == null ? (rhs == null ? true : false) : lhs.equals(rhs);
	}
	
	@Override
	public boolean equals(Object arg0) {
		NodePtrDTO dto = (NodePtrDTO)arg0;
		return	checkEquals(board_id, dto.board_id) &&
				checkEquals(version, dto.version);
	}
	
	@Override
	public String toString() {
		return String.format("%d-%d", board_id, version);
	}
}

