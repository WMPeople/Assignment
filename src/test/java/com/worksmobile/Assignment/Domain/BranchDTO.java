package com.worksmobile.Assignment.Domain;

import lombok.Getter;
import lombok.Setter;

public class BranchDTO {
	@Getter @Setter int branch_id;
	@Getter @Setter int before_version;
	@Getter @Setter int cur_version;
	@Getter @Setter int next_version;
	@Getter @Setter int history_id;
	
	@Override
	public boolean equals(Object arg0) {
		BranchDTO dto = (BranchDTO)arg0;
		return	branch_id == dto.branch_id &&
				before_version == dto.before_version &&
				cur_version == dto.cur_version &&
				next_version == dto.next_version &&
				history_id == dto.history_id;
	}
}
