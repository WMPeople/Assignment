package com.worksmobile.assignment.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.nhncorp.lucy.security.xss.XssPreventer;
import com.worksmobile.assignment.bo.BoardTempService;
import com.worksmobile.assignment.bo.CookieService;
import com.worksmobile.assignment.bo.FileService;
import com.worksmobile.assignment.bo.PageService;
import com.worksmobile.assignment.mapper.BoardTempMapper;
import com.worksmobile.assignment.mapper.FileMapper;
import com.worksmobile.assignment.model.BoardTemp;
import com.worksmobile.assignment.model.File;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.model.Page;
import com.worksmobile.assignment.util.JsonUtils;

/***
 * 자동 저장 관련한 컨트롤러입니다.
 * @author rws
 *
 */
@RestController
public class AutoController {

	@Autowired
	private BoardTempMapper boardTempMapper;

	@Autowired
	private FileMapper fileMapper;

	@Autowired
	private BoardTempService boardTempService;

	@Autowired
	private FileService fileService;

	@Autowired
	private PageService pageService;

	@Autowired
	private CookieService cookieService;

	/***
	 * 게시글 수정시 자동저장이 작동되면 호출되는 메쏘드 입니다.
	 * @param board
	 * @param req
	 * @param attachment
	 * @return
	 */
	@RequestMapping(value = "/autos/autosavewithfile", method = RequestMethod.POST)
	public Map<String, Object> tempArticleWithFile(BoardTemp boardTemp,
		HttpServletRequest req,
		MultipartHttpServletRequest attachment) {

		Map<String, Object> resultMap = new HashMap<>();

		File file = fileService.multiFileToFile(attachment);
		if (file == null) {
			boardTemp.setFile_id(0);
		} else {
			fileMapper.createFile(file);
			boardTemp.setFile_id(file.getFile_id());
		}

		boardTemp.setCookie_id((cookieService.getCookie(req).getValue()));
		boardTempService.createTempArticleOverwrite(boardTemp, "withfile");
		resultMap.put("result", "success");

		return resultMap;
	}

	@RequestMapping(value = "/autos/autosavewithoutfile", method = RequestMethod.POST)
	public Map<String, Object> tempArticleWithoutFile(BoardTemp boardTemp,
		HttpServletRequest req, MultipartHttpServletRequest attachment) {

		Map<String, Object> resultMap = new HashMap<>();
		boardTemp.setCookie_id((cookieService.getCookie(req).getValue()));
		boardTempService.createTempArticleOverwrite(boardTemp, "withoutfile");
		resultMap.put("result", "success");

		return resultMap;
	}
	
	/***
	 * 자동 저장 상세보기 입니다.
	 * @param board_id 상세 조회 할 임시 게시물의 board_id
	 * @param version 상세 조회 할 임시 게시물의 version
	 * @return 상세보기 화면과 게시물 내용이 맵 형태로 리턴됩니다.
	 */
	@RequestMapping(value = "/autos/{board_id}/{version}/{cookie_id}", method = RequestMethod.GET)
	public ModelAndView show(@PathVariable(value = "board_id") int board_id,
		@PathVariable(value = "version") int version,
		@PathVariable(value = "cookie_id") String cookie_id) {

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("board_id", board_id);
		params.put("version", version);
		params.put("cookie_id", cookie_id);

		BoardTemp boardTemp = boardTempMapper.viewDetail(params);
		if (boardTemp == null) {
			String json = JsonUtils.jsonStringIfExceptionToString(boardTemp);
			throw new RuntimeException("show 메소드에서 viewDetail 메소드 실행 에러" + json);
		}
		String dirty = boardTemp.getContent();
		String clean = XssPreventer.escape(dirty);
		boardTemp.setContent(clean);

		File file = fileMapper.getFile(boardTemp.getFile_id());

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("board", boardTemp);
		modelAndView.addObject("isHistory", 0);
		modelAndView.addObject("file", file);
		modelAndView.setViewName("boardDetail");
		return modelAndView;
	}

	/***
	 * 자동 저장 게시클 리스트 페이지로, 사용자가 요청한 페이지에 해당하는 자동 저장 게시물을 보여줍니다.
	 * @param req pages 파라미터에 사용자가 요청한 페이지 번호가 있습니다.
	 * 
	 */
	@RequestMapping(value = "autos/{board_id}/{version}", method = RequestMethod.GET)
	public ModelAndView autoList(@PathVariable(value = "board_id") int board_id,
		@PathVariable(value = "version") int version, HttpServletRequest req, HttpServletResponse res)
		throws Exception {

		Page page = pageService.getPage(req.getParameter("pages"));
		ArrayList<BoardTemp> boardTempList = new ArrayList<BoardTemp>();

		HashMap<String, Integer> params = new HashMap<String, Integer>();
		int offset = (page.getCurrentPageNo() - 1) * page.getMaxPost();
		params.put("offset", offset);
		params.put("noOfRecords", page.getMaxPost());
		params.put("board_id", board_id);
		params.put("version", version);

		boardTempList = (ArrayList<BoardTemp>)boardTempMapper.autoList(params);

		NodePtr nodePtr = new NodePtr(board_id, version);
		page.setNumberOfRecords(boardTempMapper.autoGetCount(nodePtr.toMap()));
		page = pageService.makePaging(page);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("boardTemp", boardTempList);
		modelAndView.addObject("paging", page);
		modelAndView.addObject("cookie_id", cookieService.getCookie(req).getValue());
		modelAndView.setViewName("autoList");

		return modelAndView;
	}

}
