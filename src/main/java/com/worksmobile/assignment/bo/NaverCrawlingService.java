package com.worksmobile.assignment.bo;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worksmobile.assignment.util.JsoupUtils;

/***
 * 브라우저 크롤링 서비스입니다.
 * @author khh, rws
 *
 */
@Service
public class NaverCrawlingService implements CrawlingService{
	
	@Override
	@Cacheable(value="findCrawlingedCache", key="{#category, #text, #pageNo}")
	public HashMap<String, Object> getCrawlingResult(String category, String text, String pageNo) throws Exception {
		String encodedText = URLEncoder.encode(text, "UTF-8");
		String url = "http://endic.naver.com/search.nhn?sLn=kr&searchOption=entry_idiom&query=" + encodedText + "&pageNo="
			+ pageNo;
		String selector = "#content > div.word_num_nobor > h3 > span";
		return getHashMapDataByJsoup(category, url, selector);
	}
	
	private HashMap<String, Object> getHashMapDataByJsoup(String category, String url, String selector)
		throws IOException, JsonProcessingException, ParseException {
		Document document = JsoupUtils.getDocument(url);
		if (null != document) {
			long total = JsoupUtils.getCleanTotal(document, selector);
			HashMap<String, Object> param = getDataByHTMLParsing(category, document, total);
			return param;
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private HashMap<String, Object> getDataByHTMLParsing(String category, Document document, long total)
		throws JsonProcessingException, ParseException {
		Object obj;
		JSONArray items = new JSONArray();
		JSONParser parser = new JSONParser();
		HashMap<String, Object> param = new HashMap<>();

		Elements firstElements = document.select("#content > div.word_num_nobor > dl > dt");
		Elements secondElements = document.select("#content > div.word_num_nobor > dl > dd");

		for (int j = 0; j < firstElements.size(); j++) {
			
			ObjectMapper mapper = new ObjectMapper(); 
			String json = null;
			HashMap<String, Object> mapForJson = new HashMap<String, Object>(); 
			
			String expression = secondElements.get(j).select("div > p:nth-child(1) > span:nth-child(1)").text();
			String meaning = secondElements.get(j).select("div > p:nth-child(1) > span.fnt_k05").text();
			
			if (expression.equals(meaning)) {
				mapForJson.put("title", firstElements.get(j).select("span.fnt_e30 > a").text()); 
				mapForJson.put("expression", expression);
				mapForJson.put("meaning", "");
				mapForJson.put("link", "http://endic.naver.com"+ firstElements.get(j).select("span.fnt_e30 > a").attr("href"));
			} else {
				mapForJson.put("title", firstElements.get(j).select("span.fnt_e30 > a").text()); 
				mapForJson.put("expression",  secondElements.get(j).select("div > p:nth-child(1) > span:nth-child(1)").text());
				mapForJson.put("meaning", secondElements.get(j).select("div > p:nth-child(1) > span.fnt_k05").text());
				mapForJson.put("link", "http://endic.naver.com"+ firstElements.get(j).select("span.fnt_e30 > a").attr("href"));
			}
			json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapForJson);
			obj = parser.parse(json);
			items.add(obj);
		}
		param.put("total", total);
		param.put("items", items);
		param.put("type", category);
		return param;
	}



}
