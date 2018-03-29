package com.worksmobile.Assignment.Domain;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

public class NodePtrDTO {
	@Getter @Setter protected Integer board_id = null;
	@Getter @Setter protected Integer version = null;
	@Getter @Setter protected Integer branch = null;
	
	public NodePtrDTO() { }
	public NodePtrDTO(Integer board_id, Integer version, Integer branch) {
		this.board_id = board_id;
		this.version = version;
		this.branch = branch;
	}

	public HashMap<String, Integer> toMap() {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("board_id", board_id);
		map.put("version", version);
		map.put("branch", branch);
		return map;
	}
	
	@Override
	public boolean equals(Object arg0) {
		NodePtrDTO dto = (NodePtrDTO)arg0;
		return	board_id == dto.board_id &&
				version == dto.version &&
				branch == dto.branch;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("board_id : ").append(board_id)
				.append("version : ").append(version)
				.append("branch : ").append(branch);
		return builder.toString();
	}
}
