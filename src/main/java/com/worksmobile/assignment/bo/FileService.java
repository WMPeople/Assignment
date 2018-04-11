package com.worksmobile.assignment.bo;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.mapper.FileMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.File;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

/***
 * @author rws
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
	 * 파일 다운로드시 호출 되는 메쏘드입니다.
	 * @param file_id
	 * @param req
	 * @param res
	 * @throws IOException
	 */
	public void downloadFile(int file_id,
			HttpServletRequest req, 
			HttpServletResponse res) throws IOException {
		
		File file = fileMapper.getFile(file_id);
		
		byte fileByte[] = file.getFile_data();
    	res.setContentType("application/octet-stream");
        res.setContentLength(fileByte.length);
        res.setHeader("Content-Disposition", "attachment; fileName=\"" + URLEncoder.encode(file.getFile_name(),"UTF-8")+"\";");
        res.setHeader("Content-Transfer-Encoding", "binary");
        res.getOutputStream().write(fileByte);
        res.getOutputStream().flush();
        res.getOutputStream().close();

	}
	
	/***
	 * 임시저장 게시글의 첨부 파일이 수정 됐을 때 기존에 있던 첨부파일을 삭제하기 위한 메쏘드 입니다.
	 * @param tempArticle 새로 만들어질 임시저장 게시글
	 * @param dbTempArticle DB 안에 있는 임시저장 게시글
	 */
	public void deleteFile (Board tempArticle, Board dbTempArticle) {
		
		boolean deleteFileBoolean = false;
		
		if(dbTempArticle != null) {
			int curFile_id = dbTempArticle.getFile_id();
			int afterFile_id = tempArticle.getFile_id();
			if(curFile_id != 0 && curFile_id != afterFile_id ) {
				int boardFileCount = boardMapper.getFileCount(curFile_id);
				int boardHistoryFileCount = boardHistoryMapper.selectFileCount(curFile_id);
				if((boardFileCount + boardHistoryFileCount)  == 1) {
					deleteFileBoolean=true;
				}
			}

			if(deleteFileBoolean) {
				fileMapper.deleteFile(curFile_id);
			}
		}
	}
	
	/***
	 * 더이상 사용하지 않는 파일을 삭제합니다.
	 * @param fileIdSet fileId 집합이 들어 있습니다.
	 */
	public void deleteNoMoreUsingFile(Set<Integer> fileIdSet) {
		List<Integer> fileIdList = new ArrayList<Integer>();
		fileIdList.addAll(fileIdSet);
		HashMap<String, List<Integer>> param = new HashMap<>();
		param.put("fileIdList",fileIdList);
//		int count = fileMapper.count(param);
//		System.out.println(count);
//	
		fileMapper.deleteNoMoreUsingFile(param);
	}

	
}
