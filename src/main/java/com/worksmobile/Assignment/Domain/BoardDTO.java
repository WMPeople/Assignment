package com.worksmobile.Assignment.Domain;

import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.Setter;

public class BoardDTO {
	@Getter @Setter private int id;
	@Getter @Setter private String subject;
	@Getter @Setter private String content;
	@Getter @Setter private String created;
	@Getter @Setter private MultipartFile attachment;
}
