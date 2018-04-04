package com.worksmobile.Assignment.Domain;

import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
public class NodePtrDTO {
	@Getter @Setter protected Integer board_id = null;
	@Getter @Setter protected Integer version = null;
	@Getter @Setter protected int root_board_id;
	
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
	
	
	@Override
	public String toString() {
		return String.format("%d-%d", board_id, version);
	}
	
	public boolean isRoot() {
		return (Integer)root_board_id == board_id;
	}
}

