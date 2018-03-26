package com.worksmobile.Assignment.Domain;

import lombok.Getter;
import lombok.Setter;

public class RecentVersionDTO {
	@Getter @Setter int board_history_id;
	@Getter @Setter int version;
	@Getter @Setter int branch_id;
	
	@Override
	public boolean equals(Object arg0) {
		RecentVersionDTO dto = (RecentVersionDTO)arg0;

		return  board_history_id == dto.board_history_id &&
				version == dto.version &&
				branch_id == dto.branch_id;
	}
}
