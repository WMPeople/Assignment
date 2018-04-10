package com.worksmobile.assignment.BO;

import java.io.IOException;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.worksmobile.assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.assignment.Mapper.BoardMapper;
import com.worksmobile.assignment.Mapper.FileMapper;
import com.worksmobile.assignment.Model.BoardHistory;
import com.worksmobile.assignment.Model.File;
import com.worksmobile.assignment.Model.NodePtr;

/***
 * @author RWS
 */
@Service
public class FileService {
	
	@Autowired
    private BoardMapper boardMapper;
    
    @Autowired
    private BoardHistoryMapper boardHistoryMapper;
    
	@Autowired
	FileMapper fileMapper;
	
	/***
	 * 
	 * @param attachment 프론트 -> 컨트롤러 -> 서비스로 들어온 첨부파일
	 * @return file 객체
	 */
	public File uploadFile(MultipartHttpServletRequest attachment) {

		MultipartFile mFile = null;
		Iterator<String> iter = attachment.getFileNames();
		while (iter.hasNext()) {
			String uploadFile_name = iter.next();
			mFile = attachment.getFile(uploadFile_name);
		}
		try {
			if (mFile != null && !mFile.getOriginalFilename().equals("")) {
				File file = new File();
				file.setFile_name(mFile.getOriginalFilename());
				file.setFile_data(mFile.getBytes());
				file.setFile_size(mFile.getSize());
				return file;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	/***
	 * 
	 * @param deletePtr 삭제 할 노드의 포인터
	 */
	public void deleteFile (NodePtr deletePtr) {
		boolean deleteFileBoolean = false;
		
		BoardHistory deleteHistory = boardHistoryMapper.getHistory(deletePtr);
		int file_id = deleteHistory.getFile_id();
		if(file_id != 0) {
			int fileCount = boardHistoryMapper.getFileCount(file_id);
			if(fileCount ==1) {
				deleteFileBoolean=true;
			}
		}
		
		if(deleteFileBoolean) {
			int deletedCnt = fileMapper.deleteFile(file_id);
			if(deletedCnt != 1) {
				throw new RuntimeException("파일 삭제 에러");
			};
		}
		
	}
}
