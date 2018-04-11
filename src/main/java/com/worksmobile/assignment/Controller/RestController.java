﻿package com.worksmobile.assignment.Controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.nhncorp.lucy.security.xss.XssPreventer;
import com.worksmobile.assignment.BO.Compress;
import com.worksmobile.assignment.BO.FileService;
import com.worksmobile.assignment.BO.Paging;
import com.worksmobile.assignment.BO.VersionManagementService;
import com.worksmobile.assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.assignment.Mapper.BoardMapper;
import com.worksmobile.assignment.Mapper.FileMapper;
import com.worksmobile.assignment.Model.Board;
import com.worksmobile.assignment.Model.BoardHistory;
import com.worksmobile.assignment.Model.File;
import com.worksmobile.assignment.Model.NodePtr;
import com.worksmobile.assignment.Util.Utils;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    @Autowired
    private BoardMapper boardMapper;
    
    @Autowired
    private BoardHistoryMapper boardHistoryMapper;
    
    @Autowired
    private FileMapper fileMapper;
    
    @Autowired
	private VersionManagementService versionManagementService;
    
    @Autowired
    private FileService fileService;
    
    public final static String COOKIE_NAME = "cookieName";
    		  
    // TODO : 리팩터링
    public static Cookie getCookie(HttpServletRequest req) {
    	Cookie[] cookies =req.getCookies();
    	if(cookies == null) {
    		return null;
    	}
    	Cookie curCookie= null ;
		for(int i=0; i<cookies.length; i++){
			Cookie c = cookies[i];
			if(c.getName().equals(COOKIE_NAME)) {
				curCookie = c;
				break;
			}
		}
		return curCookie;
    }
    // TODO : 리팩터링    
     public Cookie creteCookie(HttpServletResponse res) {
    	String cookieId = UUID.randomUUID().toString().replace("-", "");
    	System.out.println(cookieId);
    	Cookie cookie = new Cookie(COOKIE_NAME,cookieId);
    	res.addCookie(cookie);
    	return cookie;
    }

	/***
	 * 첫 화면으로, 사용자가 요청한 페이지에 해당하는 게시물을 보여줍니다.
	 * @param req pages 파라미터에 사용자가 요청한 페이지 번호가 있습니다.
	 */	
    @RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView boardList(HttpServletRequest req, HttpServletResponse res) throws Exception{
    	if(getCookie(req) == null || req.getCookies() == null || req.getCookies().length == 0) {
    		creteCookie(res);
    	}

    	int currentPageNo = Paging.CURRENT_PAGE_NO; 
		
		
		if (req.getParameter("pages") != null) {											 
			currentPageNo = Integer.parseInt(req.getParameter("pages"));
		}
		
		Paging paging = new Paging(currentPageNo, Paging.MAX_POST); 
		
		int offset = (paging.getCurrentPageNo() -1) * paging.getMaxPost(); 
		
		ArrayList<Board> board = new ArrayList<Board>(); 
		
		HashMap<String, Integer> params = new HashMap<String, Integer>(); 
		params.put("offset", offset); 
		params.put("noOfRecords", paging.getMaxPost()); 
		
		board = (ArrayList<Board>) boardMapper.articleList(params); 
																			
		
		paging.setNumberOfRecords(boardMapper.articleGetCount()); 
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
	 * @return 상세보기 화면과 게시물 내용이 맵 형태로 리턴됩니다.
	 */
	@RequestMapping(value = "/boards/{board_id}/{version}/{cookie_id}", method = RequestMethod.GET)
	public ModelAndView show(@PathVariable(value = "board_id") int board_id, 
			@PathVariable(value = "version") int version,
			@PathVariable(value = "cookie_id") String cookie_id){
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("board_id", board_id);
		params.put("version", version);
		params.put("cookie_id",cookie_id);
		
		Board board = boardMapper.viewDetail(params);
		if(board == null) {
			String json = Utils.jsonStringIfExceptionToString(board);
			throw new RuntimeException("show 메소드에서 viewDetail 메소드 실행 에러" + json);
		}
		String dirty = board.getContent();
		String clean = XssPreventer.escape(dirty);
		board.setContent(clean);
		
		File file = fileMapper.getFile(board.getFile_id());
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("board", board);
		modelAndView.addObject("isHistory", 0);
		modelAndView.addObject("file", file);
		modelAndView.setViewName("boardDetail");
		return modelAndView;
	}
	
	/***
	 * 파일 다온로드 함수 입니다. 게시물 상세보기 기능에서 사용됩니다.
	 * @param board_id 첨부파일이 있는 게시물의 board_id
	 * @param version 첨부파일이 있는 게시물의 version
	 * @param request 요청
	 * @param response 응답
	 */
	@RequestMapping(value = "/boards/download/{file_id}", method = RequestMethod.GET)
	public void boardFileDownload(@PathVariable(value = "file_id") int file_id,
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {

		File file = fileMapper.getFile(file_id);
		
		byte fileByte[] = file.getFile_data();
    	response.setContentType("application/octet-stream");
        response.setContentLength(fileByte.length);
        response.setHeader("Content-Disposition", "attachment; fileName=\"" + URLEncoder.encode(file.getFile_name(),"UTF-8")+"\";");
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.getOutputStream().write(fileByte);
        response.getOutputStream().flush();
        response.getOutputStream().close();
	}
	
	/***
	 * 글 생성을 합니다. VersionManagementService의 createArticle 함수를 호출하여 board_histry 테이블과 board 테이블에 데이터를 삽입합니다.
	 * @param board 사용자가 작성한 board 데이터를 받습니다.
	 * @param attachment 첨부파일 데이터를 받습니다.
	 */
	@RequestMapping(value = "/boards", method = RequestMethod.POST)
	public Map<String,Object> create(Board board, MultipartHttpServletRequest attachment) {
		Map<String,Object> resultMap = new HashMap<>();
		
		File file = fileService.uploadFile(attachment);
		if (file == null) {
			board.setFile_id(0);
		}
		else {
			fileMapper.createFile(file);
			board.setFile_id(file.getFile_id());
		}
		versionManagementService.createArticle(board);
		resultMap.put("result", "success");

		return resultMap;
	}
	
	/***
	 * 게시물에 첨부파일을 유지하고 싶은 경우를 제외하고는 글수정 데이터를 받아 DB에 등록합니다.
	 * @param board 사용자가 수정한 board 데이터를 받습니다.
	 * @param attachment 첨부파일 데이터를 받습니다.
	 */
	@RequestMapping(value = "/boards/update2", method = RequestMethod.POST)
	public Map<String,Object> updateWithoutAttachment(Board board, MultipartHttpServletRequest attachment) 
	{
		Map<String,Object> resultMap = new HashMap<>();
		File file = fileService.uploadFile(attachment);
		
		if(file != null) {
			fileMapper.createFile(file);
			board.setFile_id(file.getFile_id());
		}
		
		NodePtr leapPtr = new NodePtr(board.getBoard_id(),board.getVersion());
		NodePtr newNode = versionManagementService.modifyVersion(board, leapPtr);	
		
		if(newNode == null) {
			resultMap.put("result", "수정 실패");
		}
		else {
			resultMap.put("result", "success");
		}

		return resultMap;
	}
	
	/***
	 * 게시물에 첨부파일을 유지하고 싶은 경우를 글수정 데이터를 받아 DB에 등록합니다.
	 * @param board 사용자가 수정한 board 데이터를 받습니다.
	 * 글 수정 전 자신의 첨부파일을 DB에서 가져와 새로운 게시물에 등록합니다.
	 */
	@RequestMapping(value = "/boards/update3", method = RequestMethod.POST)
	public Map<String,Object> updateMaintainAttachment(Board board) {
		
		Map<String,Object> resultMap = new HashMap<String,Object>();
		try {
			
			board.setCookie_id(Board.LEAF_NODE_COOKIE_ID);
			Board pastBoard = boardMapper.viewDetail(board.toMap());	
			if(pastBoard == null) {
				String json = Utils.jsonStringIfExceptionToString(pastBoard);
				throw new RuntimeException("update3 메소드에서 viewDetail 메소드 실행 에러" + json);
			}
			board.setFile_id(pastBoard.getFile_id());
			
			NodePtr leapPtr = new NodePtr(board.getBoard_id(),board.getVersion());
			NodePtr newNode = versionManagementService.modifyVersion(board, leapPtr);	
			if (newNode== null) {
				resultMap.put("result","버전 수정 실패");
			}
			else {
				resultMap.put("result","success");
			}
			
		}catch (Exception e) {
			resultMap.put("result",e.getMessage());
			return resultMap;
		}
		return resultMap;
	}
	
	/***
	 * 게시물 삭제시 호출되는 함수입니다.
	 * @param board_id 삭제를 원하는 게시물의 board_id
	 * @param version 삭제를 원하는 게시물의 version
	 */
	@RequestMapping(value = "/boards/{board_id}/{version}", method = RequestMethod.DELETE)
	public Map<String,Object> destroy(@PathVariable(value = "board_id") int board_id,
			@PathVariable(value = "version") int version) throws Exception {
		Map<String,Object> resultMap = new HashMap<>();
		try {
			NodePtr leapPtr = new NodePtr(board_id,version);
			versionManagementService.deleteArticle(leapPtr);

			resultMap.put("result","success");	
		} catch (Exception e) {
			resultMap.put("result",e.getMessage());
		}
		return resultMap;
	}
	
	/***
	 * 버전 삭제시 호출 되는 메쏘드 입니다.
	 * @param board_id 버전 삭제를 원하는 이력의 board_id
	 * @param version 버전 삭제를 원하는 이력의 version
	 * @return 성공 했는지 실패 했는지를 알려주는 Map을 리턴합니다.
	 */
	@RequestMapping(value = "/boards/version/{board_id}/{version}", method = RequestMethod.DELETE)
	public Map<String,Object> versionDestory(@PathVariable(value = "board_id") int board_id,
			@PathVariable(value = "version") int version) {
		
		Map<String,Object> resultMap = new HashMap<String,Object>();
		try {
			NodePtr deletePtr = new NodePtr(board_id,version);

			versionManagementService.deleteVersion(deletePtr);
			
			resultMap.put("result","success");
		}catch(Exception e){
			e.printStackTrace();
			resultMap.put("result",e.getMessage());
			return resultMap;
		}
		
		return resultMap;
	}
	
	/***
	 * 버전관리 페이지 이동시 호출되는 메쏘드입니다.
	 * @param board_id 버전관리를 원하는 LeafNode의 board_id
	 * @param version 버전관리를 원하는 LeafNode의 version
	 * @return modelAndView LeafNode의 이력 List를 프론트에 전송합니다.
	 * @throws Exception
	 */
	@RequestMapping(value = "/boards/management/{board_id}/{version}", method = RequestMethod.GET)
	public ModelAndView versionManagement(@PathVariable(value = "board_id") int board_id, 
			@PathVariable(value = "version") int version ) throws Exception {
		
		NodePtr leapPtr = new NodePtr(board_id,version);
		List<BoardHistory> boardHistory = versionManagementService.getRelatedHistory(leapPtr);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("list", boardHistory);
		modelAndView.setViewName("versionManagement");
		return modelAndView;
	}
	
	/***
	 * 버전관리 페이지에서 버전 복구 버튼을 눌렀을 때 호출되는 메쏘드 입니다.
	 * @param board_id 복원을 원하는 버전 board_id
	 * @param version 복원을 원하는 버전 version
	 * @param leafBoard_id 복원을 원하는 버전의 LeafNode의 board_id
	 * @param leafVersion  복원을 원하는 버전의 LeafNode의 version
	 * @return resultMap 버전 복구 후 url 주소와 메쏘드 실행 성공 유무를 알려주는 Map을 리턴합니다.
	 * @throws Exception
	 */
	@RequestMapping(value = "/boards/recover/{board_id}/{version}/{leafBoard_id}/{leafVersion}", method = RequestMethod.GET)
	public Map<String,Object> versionRecover(@PathVariable(value = "board_id") int board_id, 
			@PathVariable(value = "version") int version, 
			@PathVariable(value = "leafBoard_id") int leafBoard_id, 
			@PathVariable(value = "leafVersion") int leafVersion) throws Exception { 
		
		Map<String,Object> resultMap = new HashMap<String,Object>();
		NodePtr newLeapNode = null;
		try {
			NodePtr recoverPtr = new NodePtr(board_id,version);
			NodePtr leapNodePtr = new NodePtr();

			leapNodePtr.setBoard_id(leafBoard_id);
			leapNodePtr.setVersion(leafVersion);
			
			newLeapNode =versionManagementService.recoverVersion(recoverPtr, leapNodePtr);
			resultMap.put("result","success");
		}catch (Exception e) {
			resultMap.put("result",e.getMessage());
			return resultMap;
		}
		resultMap.put("board_id", newLeapNode.getBoard_id());
		resultMap.put("version", newLeapNode.getVersion());
		return resultMap;
	}
	
	/***
	 * 이력 상세보기 입니다.
	 * @param board_id 상세 조회 할 게시물의 board_id
	 * @param version 상세 조회 할 게시물의 version
	 * @return modelAndView 이력의 상세 내용, board-boardHistory 구분자 , file 데이터 , viewName을 리턴합니다.
	 */
	@RequestMapping(value = "/history/{board_id}/{version}", method = RequestMethod.GET)
	public ModelAndView history(@PathVariable(value = "board_id") int board_id, 
			@PathVariable(value = "version") int version) {
		
		NodePtr node = new NodePtr(board_id,version);
		
		BoardHistory boardHistory = boardHistoryMapper.getHistory(node);
		Board board = new Board(boardHistory);
		File file = fileMapper.getFile(board.getFile_id());
		
		String deCompreesedContent = "";
		try {
			deCompreesedContent = Compress.deCompress(boardHistory.getHistory_content());
		} catch (IOException e) {
			e.printStackTrace();
			deCompreesedContent = "압축 해제 실패";
			throw new RuntimeException(e);
		}
		board.setContent(deCompreesedContent);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("board", board);
		modelAndView.addObject("isHistory", 1);
		modelAndView.addObject("file", file);
		modelAndView.setViewName("boardDetail");
		
		return modelAndView;
		
	}
	
	/***
	 * 
	 * @param board
	 * @param req
	 * @param attachment
	 * @return
	 */
	@RequestMapping(value = "/boards/autosave", method = RequestMethod.POST)
	public Map<String,Object> tempArticle(Board board, 
			HttpServletRequest req, 
			MultipartHttpServletRequest attachment) {	
		
		Map<String,Object> resultMap = new HashMap<>();
		
		File file = fileService.uploadFile(attachment);
		if (file == null) {
			board.setFile_id(0);
		}else {
			fileMapper.createFile(file);
			board.setFile_id(file.getFile_id());
		}

		board.setCookie_id((getCookie(req).getValue()));
		versionManagementService.createTempArticleOverwrite(board);
		resultMap.put("result", "success");

		return resultMap;
	}
	
	
	/***
	 * 자동 저장 게시클 리스트 페이지로, 사용자가 요청한 페이지에 해당하는 자동 저장 게시물을 보여줍니다.
	 * @param req pages 파라미터에 사용자가 요청한 페이지 번호가 있습니다.
	 * 
	 */	
    @RequestMapping(value = "autos/{board_id}/{version}", method = RequestMethod.GET)
	public ModelAndView autoList(@PathVariable(value = "board_id") int board_id, 
			@PathVariable(value = "version") int version,  HttpServletRequest req, HttpServletResponse res) throws Exception{
    	
    	int currentPageNo = Paging.CURRENT_PAGE_NO; 
		
		if(req.getParameter("pages") != null)											 
			currentPageNo = Integer.parseInt(req.getParameter("pages")); 	
		
		Paging paging = new Paging(currentPageNo, Paging.MAX_POST); 
		
		int offset = (paging.getCurrentPageNo() -1) * paging.getMaxPost(); 
		
		ArrayList<Board> board = new ArrayList<Board>(); 
		
		HashMap<String, Integer> params = new HashMap<String, Integer>(); 
		params.put("offset", offset); 
		params.put("noOfRecords", paging.getMaxPost()); 
		params.put("board_id", board_id); 
		params.put("version", version); 
		
		 // TODO : 메퍼 생성
		
		board = (ArrayList<Board>) boardMapper.autoList(params); 
																			
		paging.setNumberOfRecords(boardMapper.articleGetCount()); 
		paging.makePaging(); 
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("board", board);
		modelAndView.addObject("paging", paging);
		modelAndView.setViewName("autoList");
		
		return modelAndView;
	}

		
}
