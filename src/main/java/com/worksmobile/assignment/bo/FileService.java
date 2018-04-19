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
import com.worksmobile.assignment.model.File;

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
	public File multiFileToFile(MultipartHttpServletRequest attachment) {

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
		res.setHeader("Content-Disposition",
			"attachment; fileName=\"" + URLEncoder.encode(file.getFile_name(), "UTF-8") + "\";");
		res.setHeader("Content-Transfer-Encoding", "binary");
		res.getOutputStream().write(fileByte);
		res.getOutputStream().flush();
		res.getOutputStream().close();

	}

	/***
	 * 더이상 사용하지 않는 파일을 삭제합니다.
	 * @param fileIdSet fileId 집합이 들어 있습니다.
	 */
	public void deleteNoMoreUsingFile(Set<Integer> fileIdSet) {
		List<Integer> fileIdList = new ArrayList<Integer>();
		fileIdList.addAll(fileIdSet);
		HashMap<String, List<Integer>> param = new HashMap<>();
		param.put("fileIdList", fileIdList);

		fileMapper.deleteNoMoreUsingFile(param);
	}

}
