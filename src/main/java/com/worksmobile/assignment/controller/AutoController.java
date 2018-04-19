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
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

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

/***
 * 자동 저장 관련한 컨트롤러입니다.
 * @author khh
 * @author rws
 *
 */
@org.springframework.web.bind.annotation.RestController
public class AutoController {

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

	/***
	 * 게시글 수정시 자동저장이 작동되면 호출되는 메쏘드 입니다.
	 * @param board
	 * @param req
	 * @param attachment
	 * @return
	 */
	@RequestMapping(value = "/boards/autosavewithfile", method = RequestMethod.POST)
	public Map<String, Object> tempArticleWithFile(Board board,
		HttpServletRequest req,
		MultipartHttpServletRequest attachment) {

		Map<String, Object> resultMap = new HashMap<>();

		File file = fileService.multiFileToFile(attachment);
		if (file == null) {
			board.setFile_id(0);
		} else {
			fileMapper.createFile(file);
			board.setFile_id(file.getFile_id());
		}

		board.setCookie_id((cookieService.getCookie(req).getValue()));
		versionManagementService.createTempArticleOverwrite(board, "withfile");
		resultMap.put("result", "success");

		return resultMap;
	}

	@RequestMapping(value = "/boards/autosavewithoutfile", method = RequestMethod.POST)
	public Map<String, Object> tempArticleWithoutFile(Board board,
		HttpServletRequest req, MultipartHttpServletRequest attachment) {

		Map<String, Object> resultMap = new HashMap<>();
		board.setCookie_id((cookieService.getCookie(req).getValue()));
		versionManagementService.createTempArticleOverwrite(board, "withoutfile");
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
		@PathVariable(value = "version") int version, HttpServletRequest req, HttpServletResponse res)
		throws Exception {

		Page page = pageService.getPage(req.getParameter("pages"));
		ArrayList<Board> board = new ArrayList<Board>();

		HashMap<String, Integer> params = new HashMap<String, Integer>();
		int offset = (page.getCurrentPageNo() - 1) * page.getMaxPost();
		params.put("offset", offset);
		params.put("noOfRecords", page.getMaxPost());
		params.put("board_id", board_id);
		params.put("version", version);

		board = (ArrayList<Board>)boardMapper.autoList(params);

		NodePtr nodePtr = new NodePtr(board_id, version);
		page.setNumberOfRecords(boardMapper.autoGetCount(nodePtr.toMap()));
		page = pageService.makePaging(page);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("board", board);
		modelAndView.addObject("paging", page);
		modelAndView.setViewName("autoList");

		return modelAndView;
	}

}
