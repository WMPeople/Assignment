package com.worksmobile.assignment.crawling;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
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
public class CrawlingController {

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
	 * 자동 저장 게시클 리스트 페이지로, 사용자가 요청한 페이지에 해당하는 자동 저장 게시물을 보여줍니다.
	 * @param req pages 파라미터에 사용자가 요청한 페이지 번호가 있습니다.
	 * 
	 */
	@RequestMapping(value = "navercrawling/{text1}/{text2}", method = RequestMethod.GET)
	public ModelAndView naverCrawling(@PathVariable String text1, @PathVariable String text2) throws Exception {

		NaverAPIExampleTest naverAPIExampelTest = new NaverAPIExampleTest();
		HashMap<String, Object> param = naverAPIExampelTest.main(text1, text2);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("jsonArray", param.get("items"));
		modelAndView.addObject("type", param.get("type"));
		modelAndView.setViewName("crawling");

		return modelAndView;
	}
	
	@RequestMapping(value = "kakaocrawling/{text1}/{text2}", method = RequestMethod.GET)
	public ModelAndView kakaoCrawling(@PathVariable String text1, @PathVariable String text2) throws Exception {

		KakaoAPIExampleTest kakaoAPIExampelTest = new KakaoAPIExampleTest();
		HashMap<String, Object> param = kakaoAPIExampelTest.main(text1, text2);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("jsonArray", param.get("items"));
		modelAndView.addObject("type", param.get("type"));
		modelAndView.setViewName("crawling");

		return modelAndView;
	}

}
