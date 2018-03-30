package com.worksmobile.Assignment.Controller;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.BoardHistoryDTO;
import com.worksmobile.Assignment.Domain.DiffNodePtrDTO;
import com.worksmobile.Assignment.Domain.NodePtrDTO;
import com.worksmobile.Assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.Assignment.Mapper.BoardMapper;
import com.worksmobile.Assignment.Service.VersionManagementService;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    @Autowired
    private BoardMapper boardMapper;
    
    @Autowired
	private VersionManagementService versionManagementService;
    
    @Autowired
    private BoardHistoryMapper boardHistoryMapper;
    
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
		
		HashMap<String, Integer> params = new HashMap<String, Integer>();
		params.put("board_id", board_id);
		params.put("version", version);
		params.put("branch", branch);
		BoardDTO board = boardMapper.boardFileDownload(params);
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
	public ModelAndView writeForm() throws Exception {
		System.out.println("들어오긴했다");
		return new ModelAndView("boardCreate");
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
			versionManagementService.createArticle(board);
			return 1;
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
	public int update(@RequestBody Map<String,Object> params) {
		int board_id = (int)params.get("board_id");
		int version = (int)params.get("version");
		int branch = (int)params.get("branch");
		String subject = (String) params.get("subject");
		String content = (String) params.get("content");
		NodePtrDTO leapPtrDTO = new NodePtrDTO(board_id,version,branch);
		
		BoardDTO board = new BoardDTO();
		board.setBoard_id(board_id);
		board.setVersion(version);
		board.setBranch(branch);
		board.setSubject(subject);
		board.setContent(content);
		
		NodePtrDTO newNode = versionManagementService.modifyVersion(board, leapPtrDTO);
		if(newNode== null)
			return 0;
		else return 1;
	}
	
	//게시판 삭제
	@RequestMapping(value = "/boards/{board_id}/{version}/{branch}", method = RequestMethod.DELETE)
	@ResponseBody
	public String destroy(@PathVariable(value = "board_id") int board_id,
			@PathVariable(value = "version") int version,
			@PathVariable(value = "branch") int branch) throws Exception {

		NodePtrDTO leapPtrDTO = new NodePtrDTO(board_id,version,branch);
			versionManagementService.deleteArticle(leapPtrDTO);
		return "success";
	}
	
	//버전 삭제
	@RequestMapping(value = "/boards/version/{board_id}/{version}/{branch}", method = RequestMethod.DELETE)
	@ResponseBody
	public String versionDestory(@PathVariable(value = "board_id") int board_id,
			@PathVariable(value = "version") int version,
			@PathVariable(value = "branch") int branch) throws Exception {

		NodePtrDTO deletePtrDTO = new NodePtrDTO(board_id,version,branch);
			versionManagementService.deleteVersion(deletePtrDTO);
		return "success";
	}
	
	//버전 비교
	@RequestMapping(value = "/boards/diff", method = RequestMethod.POST)
	public ModelAndView diff(String board_id1, 
			 String version1,
			 String branch1 ,
			 String board_id2, 
			 String version2, 
			 String branch2) throws Exception {
		
		int number1 = Integer.parseInt(board_id1);
		int number2 = Integer.parseInt(version1);
		int number3 = Integer.parseInt(branch1);
		int number4 = Integer.parseInt(board_id2);
		int number5 = Integer.parseInt(version2);
		int number6 = Integer.parseInt(branch2);
		
		NodePtrDTO left= new NodePtrDTO(number1,number2,number3);
		NodePtrDTO right= new NodePtrDTO(number4,number5,number6);
		
		boardHistoryMapper.getHistory(left);
		boardHistoryMapper.getHistory(right);
		
		//압출 해결 후 리턴 , 맵으로 리턴
		

		return new ModelAndView("diff","list","수정요망");
	}
	
	//버전 관리 
	@RequestMapping(value = "/boards/management/{board_id}/{version}/{branch}", method = RequestMethod.GET)
	public ModelAndView versionManagement(@PathVariable(value = "board_id") int board_id, 
			@PathVariable(value = "version") int version, 
			@PathVariable(value = "branch") int branch) throws Exception {
		
		NodePtrDTO leapPtrDTO = new NodePtrDTO(board_id,version,branch);
		List<BoardHistoryDTO> boardHistoryList = versionManagementService.getRelatedHistory(leapPtrDTO);
		return new ModelAndView("versionManagement","list","boardHistory");
	}
	
	//버전 복원
	@RequestMapping(value = "/boards/recover/{board_id}/{version}/{branch}", method = RequestMethod.GET)
	public ModelAndView versionRecover(@PathVariable(value = "board_id") int board_id, 
			@PathVariable(value = "version") int version, 
			@PathVariable(value = "branch") int branch) throws Exception {
		
		System.out.println(board_id);
		
		NodePtrDTO recoverPtr = new NodePtrDTO(board_id,version,branch);
		List<BoardHistoryDTO> boardHistoryList =versionManagementService.getRelatedHistory(recoverPtr);
		
		NodePtrDTO leapNodePtr = new NodePtrDTO();
		leapNodePtr.setBoard_id(1);
		for(BoardHistoryDTO boardHistroy : boardHistoryList) {
			if(boardHistroy.getBoard_id()>=leapNodePtr.getBoard_id()) {
				leapNodePtr.setBoard_id(boardHistroy.getBoard_id());
				leapNodePtr.setVersion(boardHistroy.getVersion());
				leapNodePtr.setBranch(boardHistroy.getBranch());
			}	
		}
		
		NodePtrDTO newLeapNode =versionManagementService.recoverVersion(recoverPtr, leapNodePtr);
		
		return versionManagement(newLeapNode.getBoard_id(),newLeapNode.getBranch(),newLeapNode.getVersion());
	}
	
	
}
