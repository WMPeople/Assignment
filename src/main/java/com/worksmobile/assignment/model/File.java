package com.worksmobile.assignment.model;

import lombok.Getter;
import lombok.Setter;

/***
 * 파일 업로드 , 다운로드를 위한 모델입니다.
 * @author rws
 *
 */
public class File {
	@Setter @Getter private int file_id;
	@Setter @Getter private String file_name;
	@Setter @Getter private byte[] file_data;
	@Setter @Getter private long file_size;
	
}
