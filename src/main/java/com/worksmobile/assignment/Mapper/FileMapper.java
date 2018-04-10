package com.worksmobile.assignment.Mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.worksmobile.assignment.Model.File;

@Mapper
public interface FileMapper {
	
	public List<File> getAllFile();
	
	public File getFile(int file_id);
	
	public int createFile(File file);
	
	public int deleteFile(int file_id);
	
	
}
