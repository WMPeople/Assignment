package com.worksmobile.assignment.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.worksmobile.assignment.model.File;

@Mapper
public interface FileMapper {

	public List<File> getAllFile();

	public File getFile(int file_id);

	public int createFile(File file);

	public int deleteFile(int file_id);

	public int count(HashMap<String, List<Integer>> fileIdList);

	public int deleteNoMoreUsingFile(HashMap<String, List<Integer>> fileIdList);

}
