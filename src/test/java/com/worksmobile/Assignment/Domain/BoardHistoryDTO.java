package com.worksmobile.Assignment.Domain;

import lombok.Getter;
import lombok.Setter;

public class BoardHistoryDTO {
	@Getter @Setter private int board_history_id;
	@Getter @Setter private int version;
	@Getter @Setter private int branch_id;
	@Getter @Setter private String created;
	@Getter @Setter private String status;
	@Getter @Setter private int board_id;
	
	public BoardHistoryDTO() {};
	public BoardHistoryDTO(BoardDTO article) {
		board_id = article.getBoard_id();
		status = "Created";
	}
}
