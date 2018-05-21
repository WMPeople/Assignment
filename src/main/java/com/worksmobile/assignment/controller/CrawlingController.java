package com.worksmobile.assignment.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.worksmobile.assignment.bo.CrawlingService;
import com.worksmobile.assignment.bo.NaverCrawlingService;

@RestController
public class CrawlingController {

	@Autowired
	private CrawlingService naverCrawlingService;

	/***
	 * 네이버 브라우져 크롤링입니다. 위치, 영어사전, 맛집 데이터를 가져옵니다.
	 * @param category ex) geocode
	 * @param text ex) 그린팩토리
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "api/browser/crawling/{category}/{text}/{pageNo}", method = RequestMethod.GET)
	public ModelAndView naverCrawling(@PathVariable String category, @PathVariable String text, @PathVariable String pageNo) throws Exception {
		HashMap<String, Object> param = null;
		long sum = 0;
		for (int i = 0 ; i< 10 ; i ++) {
			Integer temp = i + 1;
			long CacheStart = System.currentTimeMillis(); // 수행시간 측정
			naverCrawlingService.getCrawlingResult("dictionary","a",temp.toString());
			long CacheEnd = System.currentTimeMillis();
			sum += (CacheEnd-CacheStart);
		}
		System.out.println("Cache 수행시간 : "+ Long.toString(sum));
		
		sum = 0;
		for (int i = 0 ; i< 10 ; i ++) {
			Integer temp = i + 1;
			long noCacheStart = System.currentTimeMillis(); // 수행시간 측정
			naverCrawlingService.getCrawlingResultNoCache("dictionary","a",temp.toString());
			long noCacheEnd = System.currentTimeMillis();
			sum += (noCacheEnd-noCacheStart);
		}
		System.out.println("NoCache 수행시간 : "+ Long.toString(sum));
		
		return null;
		
		
//		ModelAndView modelAndView = new ModelAndView();
//		modelAndView.addObject("jsonArray", param.get("items"));
//		modelAndView.addObject("type", param.get("type"));
//		modelAndView.addObject("total", param.get("total"));
//		if ((Long)param.get("total") == 0) {
//			modelAndView.setViewName("noData");
//		} else {
//			modelAndView.setViewName("crawling");
//		}
//		return modelAndView;
	}

}
