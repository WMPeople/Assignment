package com.worksmobile.Assignment.Domain;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

public class NodePtrDTO {
	@Getter @Setter private int board_id;
	@Getter @Setter private int version;
	@Getter @Setter private int branch;
	
	public NodePtrDTO() { }
	public NodePtrDTO(int board_id, int version, int branch) {
		this.board_id = board_id;
		this.version = version;
		this.branch = branch;
	}

	public NodePtrDTO(BoardHistoryDTO boardHistoryDTO) {
		board_id = boardHistoryDTO.getBoard_id();
		version = boardHistoryDTO.getVersion();
		branch = boardHistoryDTO.getBranch();
	}

	public HashMap<String, Integer> toMap() {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("board_id", board_id);
		map.put("version", version);
		map.put("branch", branch);
		return map;
	}
}
