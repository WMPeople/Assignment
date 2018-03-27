package com.worksmobile.Assignment.Controller;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Mapper.BoardMapper;

@RestController
public class BoardController {

	@Autowired
	private BoardMapper boardMapper;

	@RequestMapping("/")
	public ModelAndView start() throws Exception {
		return boardList();
	}
	@RequestMapping("/boardTemp")
	public ModelAndView boardTemp() throws Exception {
	
		return new ModelAndView("boardTemp", "boardTemp", "asd");
	}

	// 게시판 조회
	@RequestMapping("/boardList")
	public ModelAndView boardList() throws Exception {
		List<BoardDTO> boardList = boardMapper.boardList();
		System.out.println("test");
		return new ModelAndView("boardList", "list", boardList);
	}



// 사용하지 않음
//	@RequestMapping(value = "/fileUpload", method = RequestMethod.GET)
//	public ModelAndView uploadForm() throws Exception {
//		return new ModelAndView("fileUpload");
//	}

// 사용하지 않음
//	@RequestMapping(path = "/fileUpload", method = RequestMethod.POST)
//	public ModelAndView fileUpload(@RequestParam("file") MultipartFile file) {
//		String uploadPath = "C:\\upload";
//		ModelAndView mav = new ModelAndView();
//		String filename = file.getOriginalFilename();
//		// 실제 파일을 업로드하기 위한 파일 객체 생성
//		File f = new File(uploadPath,filename);
//		// 한번에 한해서 동일한 파일이 존재하면 이름에 (1) ,
//		// (나중에)2번째에도 검사해서 이름을 편해보고, 확장자 앞에 다른 이름을 추가하도록 해보자
//
//		try {
//			file.transferTo(f);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return mav;
//	}
	
	// 게시판 생성
	@RequestMapping(value = "/boardCreate", method = RequestMethod.GET)
	public ModelAndView writeForm() throws Exception {

		return new ModelAndView("boardCreate");
	}
	
	@RequestMapping(path = "/boardCreate", method = RequestMethod.POST)
	public int boardCreate(BoardDTO board, MultipartHttpServletRequest attachment) throws Exception {
		System.out.println("0");
		System.out.println(attachment);
		System.out.println("1");
		
		MultipartFile mFile = null;
		boolean isSuccess = false;
		Iterator<String> iter = attachment.getFileNames();
		while(iter.hasNext()) {
			String uploadFileName = iter.next();
			mFile = attachment.getFile(uploadFileName);
			String originalFileName = mFile.getOriginalFilename();
			String saveFileName = originalFileName;
			if(saveFileName != null && !saveFileName.equals("")) {
				try {
					isSuccess = true;				
				} catch (IllegalStateException e) {
					e.printStackTrace();
					isSuccess = false;}
			}
		}
		try {
			// 에러 핸들링 전용
			// HashMap<String, Object> map = new HashMap<>();
			// map.put("abcaa", "ddeeed");
			// return map;
			
			if(mFile!=null) {
				board.setAttachment(mFile.getBytes());
				board.setFileName(mFile.getOriginalFilename());
				board.setFileSize(mFile.getSize());
			}
			else {
				board.setAttachment(null);
				board.setFileName(null);
				board.setFileSize(0);
			}
			return boardMapper.boardCreate(board);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("에러");
		}
		return 0;
	}
	
	// 게시판 삭제
	@RequestMapping(value = "/boardDelete" , method = RequestMethod.GET)
	public ModelAndView boardDelete(int board_id) throws Exception {
		try {
			System.out.println(board_id);
			boardMapper.boardDelete(board_id);
		} catch (Exception e) {
			// TODO: handle exception
		}
		// 에러 핸들링 예정
		return boardList();
	}

	// 게시판 수정
	@RequestMapping(value = "/boardUpdate", method = RequestMethod.POST)
	public ModelAndView updateForm() throws Exception {
		return new ModelAndView("boardUpdate");
	}

	// 업데이트 버그 수정
//	@RequestMapping(path = "/boardUpdate2", method = RequestMethod.POST)
//	public int boardUpdate(BoardDTO board, MultipartHttpServletRequest attachment) throws Exception {
//		
//		MultipartFile mFile = null;
//		boolean isSuccess = false;
//		Iterator<String> iter = attachment.getFileNames();
//		while(iter.hasNext()) {
//			String uploadFileName = iter.next();
//			mFile = attachment.getFile(uploadFileName);
//			String originalFileName = mFile.getOriginalFilename();
//			String saveFileName = originalFileName;
//			if(saveFileName != null && !saveFileName.equals("")) {
//				try {
//					isSuccess = true;				
//				} catch (IllegalStateException e) {
//					e.printStackTrace();
//					isSuccess = false;}
//			}
//		}
//		try {
//			// 에러 핸들링 전용
//			// HashMap<String, Object> map = new HashMap<>();
//			// map.put("abcaa", "ddeeed");
//			// return map;
//			if(mFile!=null) {
//				board.setAttachment(mFile.getBytes());
//				board.setFileName(mFile.getOriginalFilename());
//				board.setFileSize(mFile.getSize());
//			}
//			else {
//				board.setAttachment(null);
//				board.setFileName(null);
//				board.setFileSize(0);
//			}
//			
//			return boardMapper.boardUpdate(board);
//		
//		} catch (Exception e) {
//			System.out.println("error");
//			// TODO: handle exception
//		}
//		return 0;
//	}
	
	@RequestMapping(path = "/boardUpdate2", method = RequestMethod.POST)
	public int boardUpdate(BoardDTO board) throws Exception {
		
		
			return boardMapper.boardUpdate(board);
		
		// TODO: handle exception

	}
	
	//게시물 상세 조회
	@RequestMapping(path = "/boardDetail", method = RequestMethod.GET)
	public ModelAndView viewDetail(int board_id) throws Exception {
		BoardDTO board = boardMapper.viewDetail(board_id);
		board.setAttachment(null);
		return new ModelAndView("boardDetail","board",board);
		
	}
	
	//게시물 상세 조회
	@RequestMapping(path = "/boardFileDownload", method = RequestMethod.GET)
	public void boardFileDownload(int board_id,  HttpServletRequest request, HttpServletResponse response) throws Exception {
		BoardDTO board = boardMapper.boardFileDownload(board_id);
    	byte fileByte[] = board.getAttachment();
    	
    	response.setContentType("application/octet-stream");
        response.setContentLength(fileByte.length);
        response.setHeader("Content-Disposition", "attachment; fileName=\"" + URLEncoder.encode(board.getFileName(),"UTF-8")+"\";");
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.getOutputStream().write(fileByte);
         
        response.getOutputStream().flush();
        response.getOutputStream().close();
        
			
	}
	
}