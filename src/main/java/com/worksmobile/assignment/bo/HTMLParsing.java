package com.worksmobile.assignment.bo;

import java.util.HashMap;
import org.json.simple.parser.ParseException;
import org.jsoup.nodes.Document;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface HTMLParsing {

	/***
	 * 네이버 브라우져 크롤링 결과를 hashmap을 이용하여 컨트롤러에 리턴합니다.
	 * @param category geocode, dictionary, place가 옵니다.
	 * @param text utf-8로 인코딩 되기 전 text입니다.
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, Object> getDataByHTMLParsing(String category, Document document, long total)
		throws JsonProcessingException, ParseException;

}