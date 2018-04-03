package com.worksmobile.Assignment.Mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.worksmobile.Assignment.Domain.FileDTO;

@Mapper
public interface FileMapper {
	
	public List<FileDTO> getAllFile();
	
	public FileDTO getFile(int file_id);
	
	public int createFile(FileDTO file);
	
	public int deleteFile(int file_id);
}
