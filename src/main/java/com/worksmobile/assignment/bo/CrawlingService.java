package com.worksmobile.assignment.bo;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

/***
 * 브라우저 크롤링 서비스입니다.
 * @author khh, rws
 *
 */
@Service
public class CrawlingService {
	public static String getCurrentData() {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

		return sdf.format(new Date());

	}

	/***
	 * 네이버 브라우져 크롤링 결과를 hashmap을 이용하여 컨트롤러에 리턴합니다.
	 * @param category geocode, dictionary, place가 옵니다.
	 * @param text utf-8로 인코딩 되기 전 text입니다.
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getNaverCrawlingResult(String category, String text, String pageNo) throws Exception {
		String encodedText = URLEncoder.encode(text, "UTF-8");
		String url = null;
		url = "http://endic.naver.com/search.nhn?sLn=kr&searchOption=entry_idiom&query=" + encodedText + "&pageNo="
			+ pageNo;
		Document document = Jsoup.connect(url).userAgent(
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
			.get();
		if (null != document) {
			Elements totalCountElements = document.select("#content > div.word_num_nobor > h3 > span");

			String dirtyTotal = totalCountElements.text();
			String cleanTotal = dirtyTotal.replaceAll("[^0-9]", "");
			int total = Integer.parseInt(cleanTotal);

			JSONArray items = new JSONArray();
			JSONParser parser = new JSONParser();
			Object obj;
			HashMap<String, Object> param = new HashMap<>();

			Elements firstElements = null;
			Elements secondElements = null;

			firstElements = document.select("#content > div.word_num_nobor > dl > dt");
			secondElements = document.select("#content > div.word_num_nobor > dl > dd");

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
				System.out.println(json);
			}
			param.put("total", total);
			param.put("items", items);
			param.put("type", category);
			return param;

		} else {
			return null;
		}

	}

}
