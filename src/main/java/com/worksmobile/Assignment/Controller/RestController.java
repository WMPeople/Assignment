package com.worksmobile.Assignment.Controller;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Mapper.BoardMapper;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    @Autowired
    private BoardMapper boardMapper;
    
    //게시판 리스트 조회
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView boardList() throws Exception{
        List<BoardDTO> boardList = boardMapper.boardList();
        return new ModelAndView("boardList","list",boardList);
    }
    
//	@RequestMapping(value = "/api/boards", method = RequestMethod.GET)
//	@ResponseBody
//	public List<BoardDTO> getArticles() throws Exception {
//		return boardMapper.boaradList();
//	}
	//게시물 상세 조회
	@RequestMapping(value = "/boards/{board_id}", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView show(@PathVariable(value = "board_id") int board_id) {
		BoardDTO board = boardMapper.viewDetail(board_id);
		board.setAttachment(null);
		return new ModelAndView("boardDetail","board",board);
	}
	//게시물 상세 조회 -> 다운로드
	@RequestMapping(value = "/boards/download/{board_id}/{request}/{response}", method = RequestMethod.GET)
	public void boardFileDownload(@PathVariable(value = "board_id") int board_id, @PathVariable(value = "request") HttpServletRequest request,@PathVariable(value = "response") HttpServletResponse response) throws Exception {
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
	
	//게시물 생성
	
	@RequestMapping(value = "/boards", method = RequestMethod.GET)
	public ModelAndView writeForm() throws Exception {

		return new ModelAndView("boardCreate");
	}
	
	
	@RequestMapping(value = "/boards", method = RequestMethod.POST)
	@ResponseBody
	public int create(BoardDTO board, MultipartHttpServletRequest attachment) {
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
	
//	@RequestMapping(value = "/api/articles/{id}", method = RequestMethod.PATCH)
//	@ResponseBody
//	public BoardDTO patch(@PathVariable(value = "id") int id) {
//		return user;
//	}
	//게시판 수정
	@RequestMapping(value = "/boards", method = RequestMethod.PUT)
	public ModelAndView updateForm() throws Exception {
		return new ModelAndView("boardUpdate");
	}
	
	@RequestMapping(value = "/boards/{board}", method = RequestMethod.PUT)
	@ResponseBody
	public int update(@PathVariable(value = "board") BoardDTO board) {
		return boardMapper.boardUpdate(board);
	}
	
	//게시판 삭제
	@RequestMapping(value = "/boards/{board_id}", method = RequestMethod.DELETE)
	@ResponseBody
	public String destroy(@PathVariable(value = "board_id") int board_id) throws Exception {
		boardMapper.boardDelete(board_id);
		return "success";
	}
}
