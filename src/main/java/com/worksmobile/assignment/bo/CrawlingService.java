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
	 * 위치 정보를 html 파싱을 통해 가져옵니다.
	 * @param notEncodeText
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getNaverGeocodeCrawlingResult(String notEncodeText) throws Exception {

		String text = URLEncoder.encode(notEncodeText, "UTF-8");
		String apiURL = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=" + text; // json 결과
		Document document = Jsoup.connect(apiURL).userAgent(
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
			.get();
		if (null != document) {
			Elements elements = document.select("#lcs_greenmap > div.local_map > div.detail > ul > li");
			JSONArray items = new JSONArray();
			JSONParser parser = new JSONParser();
			Object obj;
			for (int i = 0; i < elements.size(); i++) {
				String json = "{ \"title\":\"" + elements.get(i).select("li > dl > dt > a").attr("title")
					+ "\",\"address\":\"" + elements.get(i).select("dl > dd:nth-child(3) > span").attr("title") + "\""
					+ ",\"link\":\"" + elements.get(i).select("dl > dd.txt_inline > a:nth-child(1)").attr("href") + "\""
					+ ",\"hyper\":\"" + elements.get(i).select("dl > dt > a").attr("href") + "\"" + "}";
				obj = parser.parse(json);
				System.out.println(json);
				items.add(obj);
			}
			HashMap<String, Object> param = new HashMap<>();
			param.put("items", items);
			param.put("type", "geocode");
			return param;
		} else {
			return null;
		}

	}

	/***
	 * 영어사전 정보를 html 파싱을 통해 가져옵니다.
	 * @param notEncodeText
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getNaverDictionaryCrawlingResult(String notEncodeText) throws Exception {

		String text = URLEncoder.encode(notEncodeText, "UTF-8");
		String apiURL = "http://endic.naver.com/search.nhn?sLn=kr&isOnlyViewEE=N&query=" + text; // json 결과
		Document document = Jsoup.connect(apiURL).userAgent(
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
			.get();
		if (null != document) {
			Elements firstElements = document.select("#content > div:nth-child(4) > dl > dt");
			Elements secondElements = document.select("#content > div:nth-child(4) > dl > dd");
			JSONArray items = new JSONArray();

			JSONParser parser = new JSONParser();
			Object obj;
			System.out.println(firstElements.size());
			for (int i = 0; i < firstElements.size(); i++) {
				String json = "{ \"title\":\"" + firstElements.get(i).select("span.fnt_e30 > a").text()
					+ "\",\"expression\":\""
					+ secondElements.get(i).select("div > p:nth-child(1) > span:nth-child(1)").text() + "\""
					+ ",\"link\":\"" + firstElements.get(i).select("span.fnt_e30 > a").attr("href") + "\""
					+ ",\"meaning\":\"" + secondElements.get(i).select("div > p:nth-child(1) > span.fnt_k05").text()
					+ "\"" + "}";
				obj = parser.parse(json);
				System.out.println(json);
				items.add(obj);
			}
			HashMap<String, Object> param = new HashMap<>();
			param.put("items", items);
			param.put("type", "dictionary");
			return param;
		} else {
			return null;
		}

	}

	/***
	 * 맛집정보를 html 파싱을 통해 가져옵니다.
	 * @param notEncodeText
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getNaverPlaceCrawlingResult(String notEncodeText) throws Exception {

		String text = URLEncoder.encode(notEncodeText, "UTF-8");
		String apiURL = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=맛집 "
			+ text; // json 결과
		Document document = Jsoup.connect(apiURL).userAgent(
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
			.get();
		if (null != document) {
			Elements firstElements = document
				.select("#place_main_ct > div > div > div.sc_box.place_list > div.list_area > ul > li");
			JSONArray items = new JSONArray();

			JSONParser parser = new JSONParser();
			Object obj;
			System.out.println(firstElements.size());
			for (int i = 0; i < firstElements.size(); i++) {
				String json = "{ \"title\":\""
					+ firstElements.get(i).select("div > div > div.tit > span > a > span").text() + "\",\"link\":\""
					+ firstElements.get(i).select("div > div > div.tit > span > a").attr("href") + "\""
					+ ",\"image\":\"" + firstElements.get(i).select("div > img").attr("src") + "\""
					+ ",\"description\":\"" + firstElements.get(i).select("div > div > div:nth-child(2)").text() + "\""
					+ "}";
				obj = parser.parse(json);
				System.out.println(json);
				items.add(obj);
			}
			HashMap<String, Object> param = new HashMap<>();
			param.put("items", items);
			param.put("type", "place");
			return param;
		} else {
			return null;
		}

	}

}
