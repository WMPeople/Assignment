package com.worksmobile.assignment.model;

import lombok.Data;

/***
 * 파일 업로드 , 다운로드를 위한 모델입니다.
 * @author rws
 *
 */
@Data
public class File {
	private int file_id;
	private String file_name;
	private byte[] file_data;
	private long file_size;
	
}
