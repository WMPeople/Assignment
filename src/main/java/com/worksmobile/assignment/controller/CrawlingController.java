package com.worksmobile.assignment.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.worksmobile.assignment.bo.BrowserCrawlingService;
import com.worksmobile.assignment.bo.KakaoAPIService;
import com.worksmobile.assignment.bo.NaverAPIService;

/***
 * 자동 저장 관련한 컨트롤러입니다.
 * @author khh
 * @author rws
 *
 */
@org.springframework.web.bind.annotation.RestController
public class CrawlingController {

	@Autowired
	private BrowserCrawlingService browserCrawlingService;

	@Autowired
	private NaverAPIService naverAPIService;

	@Autowired
	private KakaoAPIService kakaoAPIService;

	@RequestMapping(value = "api/browser/crawling")
	public String test() throws Exception {
		return browserCrawlingService.testBrowser();
	}

	/***
	 * 네이버 검색 API
	 * @param text1
	 * @param text2
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "api/naver/{text1}/{text2}/{text3}", method = RequestMethod.GET)
	public ModelAndView naverCrawling(@PathVariable String text1, @PathVariable String text2,
		@PathVariable String text3) throws Exception {

		HashMap<String, Object> param = naverAPIService.main(text1, text2, text3);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("jsonArray", param.get("items"));
		modelAndView.addObject("type", param.get("type"));
		modelAndView.setViewName("crawling");

		return modelAndView;
	}

	/***
	 * 카카오 API
	 * @param text1
	 * @param text2
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "api/kakao/{text1}/{text2}/{text3}", method = RequestMethod.GET)
	public ModelAndView kakaoCrawling(@PathVariable String text1, @PathVariable String text2,
		@PathVariable String text3) throws Exception {

		HashMap<String, Object> param = kakaoAPIService.main(text1, text2, text3);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("jsonArray", param.get("items"));
		modelAndView.addObject("type", param.get("type"));
		modelAndView.setViewName("crawling");

		return modelAndView;
	}

}
