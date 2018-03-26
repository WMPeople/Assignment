package com.worksmobile.Assignment.Domain;

import lombok.Getter;
import lombok.Setter;

public class BoardDTO {
	@Getter @Setter private int board_id;
	@Getter @Setter private String subject;
	@Getter @Setter private String content;
	@Getter @Setter private String created;
	@Getter @Setter private byte[] attachment;
}
