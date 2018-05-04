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
	public HashMap<String, Object> getNaverCrawlingResult(String category, String text) throws Exception {

		String encodedText = URLEncoder.encode(text, "UTF-8");
		String url = null;
		if ("geocode".equals(category)) {
			url = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=" + encodedText; 
		} else if ("dictionary".equals(category)) {
			url = "http://endic.naver.com/search.nhn?sLn=kr&isOnlyViewEE=N&query=" + encodedText;
		} else if ("place".equals(category)) {
			url = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=맛집 "+ encodedText;
		} 
		Document document = Jsoup.connect(url).userAgent(
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
			.get();
		if (null != document) {
			JSONArray items = new JSONArray();
			JSONParser parser = new JSONParser();
			Object obj;
			
			Elements firstElements = null;
			Elements secondElements = null;
			
			if ("geocode".equals(category)) {
				firstElements = document.select("#lcs_greenmap > div.local_map > div.detail > ul > li");
			} else if ("dictionary".equals(category)) {
				firstElements = document.select("#content > div:nth-child(4) > dl > dt");
				secondElements = document.select("#content > div:nth-child(4) > dl > dd");
			} else if ("place".equals(category)) {
				firstElements = document.select("#place_main_ct > div > div > div.sc_box.place_list > div.list_area > ul > li");
			} 
			
			for (int i = 0; i < firstElements.size(); i++) {
				String json =null;
				
				if ("geocode".equals(category)) {
					json = "{ \"title\":\"" + firstElements.get(i).select("li > dl > dt > a").attr("title")
						+ "\",\"address\":\"" + firstElements.get(i).select("dl > dd:nth-child(3) > span").attr("title") + "\""
						+ ",\"link\":\"" + firstElements.get(i).select("dl > dd.txt_inline > a:nth-child(1)").attr("href") + "\""
						+ ",\"hyper\":\"" + firstElements.get(i).select("dl > dt > a").attr("href") + "\"" + "}";
				} else if ("dictionary".equals(category)) {
					String expression = secondElements.get(i).select("div > p:nth-child(1) > span:nth-child(1)").text();
					String meaning = secondElements.get(i).select("div > p:nth-child(1) > span.fnt_k05").text();
					if (expression.equals(meaning)) {
						json = "{ \"title\":\"" + firstElements.get(i).select("span.fnt_e30 > a").text()
							+ "\",\"expression\":\""
							+ expression + "\""
							+ ",\"link\":\"" +"http://endic.naver.com"+ firstElements.get(i).select("span.fnt_e30 > a").attr("href") + "\""
							+ ",\"meaning\":\"" + ""
							+ "\"" + "}";
					} else {
						json = "{ \"title\":\"" + firstElements.get(i).select("span.fnt_e30 > a").text()
							+ "\",\"expression\":\""
							+ secondElements.get(i).select("div > p:nth-child(1) > span:nth-child(1)").text() + "\""
							+ ",\"link\":\"" +"http://endic.naver.com"+ firstElements.get(i).select("span.fnt_e30 > a").attr("href") + "\""
							+ ",\"meaning\":\"" + secondElements.get(i).select("div > p:nth-child(1) > span.fnt_k05").text()
							+ "\"" + "}";
					}
					
					
				} else if ("place".equals(category)) {
					json = "{ \"title\":\""
						+ firstElements.get(i).select("div > div > div.tit > span > a > span").text() + "\",\"link\":\""
						+ firstElements.get(i).select("div > div > div.tit > span > a").attr("href") + "\""
						+ ",\"image\":\"" + firstElements.get(i).select("div > img").attr("src") + "\""
						+ ",\"description\":\"" + firstElements.get(i).select("div > div > div:nth-child(2)").text() + "\""
						+ "}";
				} 
				
				obj = parser.parse(json);
				System.out.println(json);
				items.add(obj);
			}
			HashMap<String, Object> param = new HashMap<>();
			param.put("items", items);
			param.put("type", category);
			return param;
		} else {
			return null;
		}

	}

}
