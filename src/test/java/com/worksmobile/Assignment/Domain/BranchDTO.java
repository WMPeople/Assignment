package com.worksmobile.Assignment.Domain;

import lombok.Getter;
import lombok.Setter;

public class BranchDTO {
	@Getter @Setter int branch_id;
	@Getter @Setter int before_version;
	@Getter @Setter int cur_version;
	@Getter @Setter int next_version;
	@Getter @Setter int history_id;
}
