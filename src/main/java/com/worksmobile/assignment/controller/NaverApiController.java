package com.worksmobile.assignment.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.worksmobile.assignment.bo.NaverAPIService;

/***
 * naver와 kakao의 오픈 api 및 네이버 브라우져 크롤링 담당 컨트롤러입니다.
 * @author khh
 * @author rws
 *
 */
@RestController
public class NaverApiController {

	@Autowired
	private NaverAPIService naverAPIService;

	/***
	 * 네이버  API 입니다.
	 * 
	 * @param apiName ex) "search"
	 * @param category ex) "book"
	 * @param text ex) "어린왕자"
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "api/naver/{apiName}/{category}/{text}", method = RequestMethod.GET)
	public ModelAndView naverApi(@PathVariable String apiName, @PathVariable String category,
		@PathVariable String text) throws Exception {

		HashMap<String, Object> param = naverAPIService.getSearchResult(apiName, category, text);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("jsonArray", param.get("items"));
		modelAndView.addObject("type", param.get("type"));
		modelAndView.setViewName("crawling");
		return modelAndView;
	}

}
