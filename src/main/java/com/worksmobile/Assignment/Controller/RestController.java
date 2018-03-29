package com.worksmobile.Assignment.Controller;

import java.net.URLEncoder;
import java.util.HashMap;
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
    
	//게시물 상세 조회
	@RequestMapping(value = "/boards/{board_id}/{version}/{branch}", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView show(@PathVariable(value = "board_id") int board_id, 
			@PathVariable(value = "version") int version, 
			@PathVariable(value = "branch") int branch) {
		System.out.println(board_id );
		System.out.println(version );
		HashMap<String, Integer> params = new HashMap<String, Integer>();
		params.put("board_id", board_id);
		params.put("version", version);
		params.put("branch", branch);
		BoardDTO board = boardMapper.viewDetail(params);
		board.setFile_data(null);
		return new ModelAndView("boardDetail","board",board);
	}
	//게시물 상세 조회 -> 다운로드
	@RequestMapping(value = "/boards/download/{board_id}/{version}/{branch}", method = RequestMethod.GET)
	public void boardFileDownload(@PathVariable(value = "board_id") int board_id, 
			@PathVariable(value = "version") int version, 
			@PathVariable(value = "branch") int branch,
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		System.out.println("들어오긴했네");
		BoardDTO board = boardMapper.boardFileDownload(board_id,version,branch);
    	byte fileByte[] = board.getFile_data();
    	response.setContentType("application/octet-stream");
        response.setContentLength(fileByte.length);
        response.setHeader("Content-Disposition", "attachment; fileName=\"" + URLEncoder.encode(board.getFile_name(),"UTF-8")+"\";");
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.getOutputStream().write(fileByte);
        response.getOutputStream().flush();
        response.getOutputStream().close();
	}
	
	//게시물 생성
	@RequestMapping(value = "/boards", method = RequestMethod.GET)
	public String writeForm() throws Exception {
		System.out.println("들어오긴했다");
		return "boardCreate";
	}
	
	
	@RequestMapping(value = "/boards", method = RequestMethod.POST)
	@ResponseBody
	public int create(BoardDTO board, MultipartHttpServletRequest attachment) {
		MultipartFile mFile = null;
		boolean isSuccess = false;
		Iterator<String> iter = attachment.getFileNames();
		while(iter.hasNext()) {
			String uploadFile_name = iter.next();
			mFile = attachment.getFile(uploadFile_name);
			String originalFile_name = mFile.getOriginalFilename();
			String saveFile_name = originalFile_name;
			if(saveFile_name != null && !saveFile_name.equals("")) {
				try {
					isSuccess = true;				
				} catch (IllegalStateException e) {
					e.printStackTrace();
					isSuccess = false;}
			}
		}
		try {
			// 에러 핸들링 전용 예정
			
			if(mFile!=null) {
				board.setFile_data(mFile.getBytes());
				board.setFile_name(mFile.getOriginalFilename());
				board.setFile_size(mFile.getSize());
			}
			else {
				board.setFile_data(null);
				board.setFile_name(null);
				board.setFile_size(0);
			}
			return boardMapper.boardCreate(board);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("에러");
		}
		return 0;
	}
	
	//게시판 수정
	@RequestMapping(value = "/boards/update", method = RequestMethod.POST)
	public ModelAndView updateForm() throws Exception {
		return new ModelAndView("boardUpdate");
	}
	
	@RequestMapping(value = "/boards", method = RequestMethod.PUT)
	@ResponseBody
	public int update(BoardDTO board) {
		System.out.println("asdafgdfd");
		System.out.println(board.getBoard_id());
		return boardMapper.boardUpdate(board);
	}
	
	//게시판 삭제
	@RequestMapping(value = "/boards/{board_id}", method = RequestMethod.DELETE)
	@ResponseBody
	public String destroy(@PathVariable(value = "board_id") int board_id) throws Exception {
		boardMapper.boardDelete(board_id);
		return "success";
	}
	
	//버전 관리 test
	@RequestMapping(value = "/boards/temp", method = RequestMethod.GET)
	public String boardTemp() throws Exception {
	
		return "boardTemp";
	}
}
