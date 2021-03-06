﻿package com.worksmobile.assignment.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.worksmobile.assignment.bo.BoardTempService;
import com.worksmobile.assignment.bo.CookieService;
import com.worksmobile.assignment.bo.FileService;
import com.worksmobile.assignment.bo.PageService;
import com.worksmobile.assignment.bo.VersionManagementService;
import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.mapper.FileMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.File;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.model.Page;
import com.worksmobile.assignment.util.FilterUtils;
import com.worksmobile.assignment.util.JsonUtils;

/***
 * @RestController 어노테이션은 각 메소드에 @ResponseBody 어노테이션을 추가 할 필요 없이, 
 * @Controller와 @ResponseBody의 조합으로 기능합니다.
 * 
 * 페이지 이동 및 게시판 관련한 CRUD를 처리하는 컨트롤러 입니다.
 * @author rws
 *
 */
@RestController
public class BoardController {

	@Autowired
	private BoardMapper boardMapper;

	@Autowired
	private FileMapper fileMapper;

	@Autowired
	private VersionManagementService versionManagementService;

	@Autowired
	private FileService fileService;

	@Autowired
	private PageService pageService;

	@Autowired
	private CookieService cookieService;

	@Autowired
	private BoardTempService boardTempService;

	/***
	 * 게시물 작성입니다. 글쓰기 폼 페이지로 이동합니다.
	 */
	@RequestMapping(value = "/boards", method = RequestMethod.GET)
	public ModelAndView writeForm() throws Exception {
		return new ModelAndView("boardCreate");
	}

	/***
	 * 게시물 수정 버튼을 눌렀을 때 자동 저장 게시글이 없으면 만들고, 수정 페이지로 이동합니다.
	 */
	@RequestMapping(value = "/boards/update", method = RequestMethod.POST)
	public ModelAndView updateForm(Board board, HttpServletRequest req) throws Exception {
		board.setSubject(FilterUtils.dirtyToClean(board.getSubject()));
		board.setContent(FilterUtils.dirtyToClean(board.getContent()));
		boardTempService.makeTempBoard(board.getBoard_id(), board.getVersion(), cookieService.getCookie(req).getValue(),
			board.getCreated_time(), board.getContent(), board.getFile_id(), board.getSubject());
		return new ModelAndView("boardUpdate");
	}

	/***
	 * 첫 화면으로, 사용자가 요청한 페이지에 해당하는 게시물을 보여줍니다.
	 * @param req pages 파라미터에 사용자가 요청한 페이지 번호가 있습니다.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView boardList(HttpServletRequest req, HttpServletResponse res) throws Exception {
		Cookie cookie;
		if (req.getCookies() == null || cookieService.getCookie(req) == null ||  cookieService.getCookie(req).getValue() == null) {
			cookie = cookieService.creteCookie(res);
		} else {
			cookie = cookieService.getCookie(req);
		}

		Page page = pageService.getPage(req.getParameter("pages"));
		ArrayList<Board> board = new ArrayList<Board>();

		HashMap<String, Integer> params = new HashMap<String, Integer>();
		int offset = (page.getCurrentPageNo() - 1) * page.getMaxPost();
		params.put("offset", offset);
		params.put("noOfRecords", page.getMaxPost());

		board = (ArrayList<Board>)boardMapper.articleList(params);

		page.setNumberOfRecords(boardMapper.articleGetCount());
		page = pageService.makePaging(page);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("board", board);
		modelAndView.addObject("paging", page);
		modelAndView.addObject("cookie_id", cookie.getValue());
		modelAndView.setViewName("boardList");

		return modelAndView;
	}

	/***
	 * 게시물 상세보기 입니다.
	 * @param board_id 상세 조회 할 게시물의 board_id
	 * @param version 상세 조회 할 게시물의 version
	 * @return 상세보기 화면과 게시물 내용이 맵 형태로 리턴됩니다.
	 */
	@RequestMapping(value = "/boards/{board_id}/{version}", method = RequestMethod.GET)
	public ModelAndView show(@PathVariable(value = "board_id") int board_id,
		@PathVariable(value = "version") int version) {

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("board_id", board_id);
		params.put("version", version);

		Board board = boardMapper.viewDetail(params);
		if (board == null) {
			String json = JsonUtils.jsonStringIfExceptionToString(board);
			throw new RuntimeException("show 메소드에서 viewDetail 메소드 실행 에러" + json);
		}

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
		HttpServletRequest req,
		HttpServletResponse res) throws Exception {

		fileService.downloadFile(file_id, req, res);
	}

	/***
	 * 글 생성을 합니다. VersionManagementService의 createArticle 함수를 호출하여 board_histry 테이블과 board 테이블에 데이터를 삽입합니다.
	 * @param board 사용자가 작성한 board 데이터를 받습니다.
	 * @param attachment 첨부파일 데이터를 받습니다.
	 */
	@RequestMapping(value = "/boards", method = RequestMethod.POST)
	public Map<String, Object> create(Board board, MultipartHttpServletRequest attachment) {
		Map<String, Object> resultMap = new HashMap<>();

		File file = fileService.multiFileToFile(attachment);
		if (file == null) {
			board.setFile_id(0);
		} else {
			fileMapper.createFile(file);
			board.setFile_id(file.getFile_id());
		}
		board.setSubject(FilterUtils.dirtyToClean(board.getSubject()));
		board.setContent(FilterUtils.dirtyToClean(board.getContent()));
		versionManagementService.createArticle(board);
		resultMap.put("result", "success");
		return resultMap;
	}

	/***
	 * 게시물에 첨부파일을 유지하고 싶은 경우를 제외하고는 글수정 데이터를 받아 DB에 등록합니다.
	 * @param board 사용자가 수정한 board 데이터를 받습니다.
	 * @param attachment 첨부파일 데이터를 받습니다.
	 */
	@RequestMapping(value = "/boards/updatewithoutattachment", method = RequestMethod.POST)
	public Map<String, Object> updateWithoutAttachment(Board board, MultipartHttpServletRequest attachment,
		HttpServletRequest req) {
		Map<String, Object> resultMap = new HashMap<>();
		
		File file = fileService.multiFileToFile(attachment);

		if (file != null) {
			fileMapper.createFile(file);
			board.setFile_id(file.getFile_id());
		}
		board.setSubject(FilterUtils.dirtyToClean(board.getSubject()));
		board.setContent(FilterUtils.dirtyToClean(board.getContent()));
		NodePtr leapPtr = new NodePtr(board.getBoard_id(), board.getVersion());
		NodePtr newNode = versionManagementService.modifyVersion(board, leapPtr,
			cookieService.getCookie(req).getValue());

		if (newNode == null) {
			resultMap.put("result", "수정 실패");
		} else {

			resultMap.put("result", "success");
			resultMap.put("updatedBoard", newNode);
		}

		return resultMap;
	}

	/***
	 * 게시물에 첨부파일을 유지하고 싶은 경우를 글수정 데이터를 받아 DB에 등록합니다.
	 * @param board 사용자가 수정한 board 데이터를 받습니다.
	 * 글 수정 전 자신의 첨부파일을 DB에서 가져와 새로운 게시물에 등록합니다.
	 */
	@RequestMapping(value = "/boards/updatemaintainattachment", method = RequestMethod.POST)
	public Map<String, Object> updateMaintainAttachment(Board board, HttpServletRequest req) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			Board pastBoard = boardMapper.viewDetail(board.toMap());
			if (pastBoard == null) {
				String json = JsonUtils.jsonStringIfExceptionToString(pastBoard);
				throw new RuntimeException("updatemaintainattachment 메소드에서 viewDetail 메소드 실행 에러" + json);
			}
			board.setFile_id(pastBoard.getFile_id());
			board.setSubject(FilterUtils.dirtyToClean(board.getSubject()));
			board.setContent(FilterUtils.dirtyToClean(board.getContent()));
			NodePtr leapPtr = new NodePtr(board.getBoard_id(), board.getVersion());
			NodePtr newNode = versionManagementService.modifyVersion(board, leapPtr,
				cookieService.getCookie(req).getValue());
			if (newNode == null) {
				resultMap.put("result", "버전 수정 실패");
			} else {
				resultMap.put("result", "success");
				resultMap.put("updatedBoard", newNode);
			}

		} catch (Exception e) {
			resultMap.put("result", e.getMessage());
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
	public Map<String, Object> destroy(@PathVariable(value = "board_id") int board_id,
		@PathVariable(value = "version") int version) throws Exception {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			NodePtr leapPtr = new NodePtr(board_id, version);
			versionManagementService.deleteArticle(leapPtr);

			resultMap.put("result", "success");
		} catch (Exception e) {
			resultMap.put("result", e.getMessage());
		}
		return resultMap;
	}

}
