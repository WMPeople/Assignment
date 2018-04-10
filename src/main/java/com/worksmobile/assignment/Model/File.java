package com.worksmobile.assignment.Model;

import lombok.Getter;
import lombok.Setter;

public class File {
	@Setter @Getter private int file_id;
	@Setter @Getter private String file_name;
	@Setter @Getter private byte[] file_data;
	@Setter @Getter private long file_size;
	
}
