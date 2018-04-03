package com.worksmobile.Assignment.Domain;

import lombok.Getter;
import lombok.Setter;

public class FileDTO {
	@Setter @Getter private int file_id;
	@Setter @Getter private String file_name;
	@Setter @Getter private byte[] file_data;
	@Setter @Getter private long file_size;
	
}
