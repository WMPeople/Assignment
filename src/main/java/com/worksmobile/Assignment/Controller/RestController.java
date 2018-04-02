package com.worksmobile.Assignment.Controller;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import com.worksmobile.Assignment.Domain.BoardHistoryDTO;
import com.worksmobile.Assignment.Domain.NodePtrDTO;
import com.worksmobile.Assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.Assignment.Mapper.BoardMapper;
import com.worksmobile.Assignment.Service.Compress;
import com.worksmobile.Assignment.Service.Paging;
import com.worksmobile.Assignment.Service.VersionManagementService;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    @Autowired
    private BoardMapper boardMapper;
    
    @Autowired
	private VersionManagementService versionManagementService;
    
    @Autowired
    private BoardHistoryMapper boardHistoryMapper;
    
	/***
	 * 첫 화면으로, 사용자가 요청한 페이지에 해당하는 게시물을 보여줍니다.
	 * @param req pages 파라미터에 사용자가 요청한 페이지 번호가 있습니다.
	 */	
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
	public ModelAndView boardList(HttpServletRequest req) throws Exception{
		
    	int currentPageNo = 1; // /(localhost:8080)페이지로 오면 처음에 표시할 페이지 (1 = 첫번째 페이지)
		int maxPost = 10;	// 페이지당 표시될 게시물  최대 갯수
		
		if(req.getParameter("pages") != null)								//게시물이 1개도없으면(=페이지가 생성이 안되었으면)이 아니라면 == 페이징이 생성되었다면							 
			currentPageNo = Integer.parseInt(req.getParameter("pages")); 	//pages에있는 string 타입 변수를 int형으로 바꾸어서 currentPageNo에 담는다.
		
		Paging paging = new Paging(currentPageNo, maxPost); //Paging.java에있는 currentPageNo, maxPost를 paging변수에 담는다.
		
		int offset = (paging.getCurrentPageNo() -1) * paging.getmaxPost(); 
		// 현재 3페이지 이고, 그 페이지에 게시물이 10개가 있다면 offset값은 (3-1) * 10 = 20이 된다. 
		
		ArrayList<BoardDTO> board = new ArrayList<BoardDTO>(); // BoardDTO에 있는 변수들을 ArrayList 타입의 배열로 둔 다음 이를 page라는 변수에 담는다.
		
		HashMap<String, Integer> params = new HashMap<String, Integer>(); 
		params.put("offset", offset); 
		params.put("noOfRecords", paging.getmaxPost()); 
		
		board = (ArrayList<BoardDTO>) boardMapper.articleList(params); 
		//writeService.java에 있는 articleList 함수를 이용하여 offset값과 maxPost값을 ArrayList 타입의 배열로 담고, 이 배열 자체를 page 변수에 담는다.																							
		
		paging.setNumberOfRecords(boardMapper.articleGetCount()); // 페이지를 표시하기 위해 전체 게시물 수를 파악하기 위한것
		paging.makePaging(); 
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("board", board);
		modelAndView.addObject("paging", paging);
		modelAndView.setViewName("boardList");
		
		return modelAndView;
	}

    
	/***
	 * 게시물 상세보기 입니다.
	 * @param board_id 상세 조회 할 게시물의 board_id
	 * @param version 상세 조회 할 게시물의 version
	 * @param branch 상세 조회 할 게시물의 branch
	 * @return 상세보기 화면과 게시물 내용이 맵 형태로 리턴됩니다.
	 */
	@RequestMapping(value = "/boards/{board_id}/{version}/{branch}", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView show(@PathVariable(value = "board_id") int board_id, 
			@PathVariable(value = "version") int version, 
			@PathVariable(value = "branch") int branch) {
		
		HashMap<String, Integer> params = new HashMap<String, Integer>();
		params.put("board_id", board_id);
		params.put("version", version);
		params.put("branch", branch);
		
		BoardDTO board = boardMapper.viewDetail(params);
		
		return new ModelAndView("boardDetail","board",board);
	}
	
	/***
	 * 파일 다온로드 함수 입니다. 게시물 상세보기 기능에서 사용됩니다.
	 * @param board_id 첨부파일이 있는 게시물의 board_id
	 * @param version 첨부파일이 있는 게시물의 version
	 * @param branch 첨부파일이 있는 게시물의 branch
	 * @param request 요청
	 * @param response 응답
	 */
	@RequestMapping(value = "/boards/download/{board_id}/{version}/{branch}", method = RequestMethod.GET)
	public void boardFileDownload(@PathVariable(value = "board_id") int board_id, 
			@PathVariable(value = "version") int version, 
			@PathVariable(value = "branch") int branch,
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {

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
	
	/***
	 * 게시물 작성입니다. 글쓰기 폼 페이지로 이동합니다.
	 */
	@RequestMapping(value = "/boards", method = RequestMethod.GET)
	public ModelAndView writeForm() throws Exception {
		return new ModelAndView("boardCreate");
	}
	
	/***
	 * 글 생성을 합니다. VersionManagementService의 createArticle 함수를 호출하여 board_histry 테이블과 board 테이블에 데이터를 삽입합니다.
	 * @param board 사용자가 작성한 board 데이터를 받습니다.
	 * @param attachment 첨부파일 데이터를 받습니다.
	 */
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
	
	/***
	 * 게시물 수정입니다. 글수정 폼 페이지로 이동합니다.
	 */
	@RequestMapping(value = "/boards/update", method = RequestMethod.POST)
	public ModelAndView updateForm() throws Exception {
		return new ModelAndView("boardUpdate");
	}
	
	/***
	 * 게시물에 첨부파일을 유지하고 싶은 경우를 제외하고는 글수정 데이터를 받아 DB에 등록합니다.
	 * @param board 사용자가 수정한 board 데이터를 받습니다.
	 * @param attachment 첨부파일 데이터를 받습니다.
	 */
	@RequestMapping(value = "/boards/update2", method = RequestMethod.POST)
	@ResponseBody
	public int update2(BoardDTO board, MultipartHttpServletRequest attachment) 
	{
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
			System.out.println(board.getFile_name());
			NodePtrDTO leapPtrDTO = new NodePtrDTO(board.getBoard_id(),board.getVersion(),board.getBranch());
			NodePtrDTO newNode = versionManagementService.modifyVersion(board, leapPtrDTO);	
			if(newNode== null)
				return 0;
			else return 1;

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("에러");
		}
		return 0;

	}
	
	/***
	 * 게시물에 첨부파일을 유지하고 싶은 경우를 글수정 데이터를 받아 DB에 등록합니다.
	 * @param board 사용자가 수정한 board 데이터를 받습니다.
	 * 글 수정 전 자신의 첨부파일을 DB에서 가져와 새로운 게시물에 등록합니다.
	 */
	@RequestMapping(value = "/boards/update3", method = RequestMethod.POST)
	@ResponseBody
	public int update3(BoardDTO board) {
		
		BoardDTO pastBoard = new BoardDTO();
		
		HashMap<String, Integer> params = new HashMap<String, Integer>();
		params.put("board_id", board.getBoard_id());
		params.put("version", board.getVersion());
		params.put("branch", board.getBranch());
		
		pastBoard = boardMapper.viewDetail(params);
		
		board.setFile_name(pastBoard.getFile_name());
		board.setFile_data(pastBoard.getFile_data());
		board.setFile_size(pastBoard.getFile_size());
		
		NodePtrDTO leapPtrDTO = new NodePtrDTO(board.getBoard_id(),board.getVersion(),board.getBranch());
		NodePtrDTO newNode = versionManagementService.modifyVersion(board, leapPtrDTO);	
		if(newNode== null)
			return 0;
		else return 1;
	}
	
	/***
	 * 게시물 삭제시 호출되는 함수입니다.
	 * @param board_id 삭제를 원하는 게시물의 board_id
	 * @param version 삭제를 원하는 게시물의 version
	 * @param branch 삭제를 원하는 게시물의 branch
	 */
	@RequestMapping(value = "/boards/{board_id}/{version}/{branch}", method = RequestMethod.DELETE)
	@ResponseBody
	public String destroy(@PathVariable(value = "board_id") int board_id,
			@PathVariable(value = "version") int version,
			@PathVariable(value = "branch") int branch) throws Exception {

		NodePtrDTO leapPtrDTO = new NodePtrDTO(board_id,version,branch);
		versionManagementService.deleteArticle(leapPtrDTO);
		return "success";
	}
	
	//버전삭제
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
	public ModelAndView diff(int board_id1, 
			 int version1,
			 int branch1 ,
			 int board_id2, 
			 int version2, 
			 int branch2) throws Exception {
		
		NodePtrDTO left= new NodePtrDTO(board_id1,version1,branch1);
		NodePtrDTO right= new NodePtrDTO(board_id2,version2,branch2);
		
		String leftContent = Compress.deCompress(boardHistoryMapper.getHistory(left).getHistory_content());
		String rightContent = Compress.deCompress(boardHistoryMapper.getHistory(right).getHistory_content());
		
		//압출 해결 후 리턴 , 맵으로 리턴
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("leftContent", leftContent);
		modelAndView.addObject("rightContent", rightContent);
		modelAndView.setViewName("diff");
		
		return modelAndView;
		

	}
	
	//버전 관리 
	@RequestMapping(value = "/boards/management/{board_id}/{version}/{branch}", method = RequestMethod.GET)
	public ModelAndView versionManagement(@PathVariable(value = "board_id") int board_id, 
			@PathVariable(value = "version") int version, 
			@PathVariable(value = "branch") int branch) throws Exception {
		
		NodePtrDTO leapPtrDTO = new NodePtrDTO(board_id,version,branch);
		List<BoardHistoryDTO> boardHistory = versionManagementService.getRelatedHistory(leapPtrDTO);
		
		return new ModelAndView("versionManagement","list",boardHistory);
	}
	
	//버전 복원
	@RequestMapping(value = "/boards/recover/{board_id}/{version}/{branch}/{leafBoard_id}/{leafVersion}/{leafBranch}", method = RequestMethod.GET)
	public HashMap<String,Object> versionRecover(@PathVariable(value = "board_id") int board_id, 
			@PathVariable(value = "version") int version, 
			@PathVariable(value = "branch") int branch,
			@PathVariable(value = "leafBoard_id") int leafBoard_id, 
			@PathVariable(value = "leafVersion") int leafVersion, 
			@PathVariable(value = "leafBranch") int leafBranch) throws Exception {
		
		System.out.println(board_id);
		
		NodePtrDTO recoverPtr = new NodePtrDTO(board_id,version,branch);	
		NodePtrDTO leapNodePtr = new NodePtrDTO();

		leapNodePtr.setBoard_id(leafBoard_id);
		leapNodePtr.setVersion(leafVersion);
		leapNodePtr.setBranch(leafBranch);
		
		NodePtrDTO newLeapNode =versionManagementService.recoverVersion(recoverPtr, leapNodePtr);
		
		HashMap<String,Object> map = new HashMap<String,Object>();
		
		map.put("result","success");
		map.put("board_id", newLeapNode.getBoard_id());
		map.put("version", newLeapNode.getVersion());
		map.put("branch", newLeapNode.getBranch());
		
		return map;
	}
	
	
}
