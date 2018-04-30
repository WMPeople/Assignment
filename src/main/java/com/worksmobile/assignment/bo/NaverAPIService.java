package com.worksmobile.assignment.bo;

//네이버 검색 API 예제 - blog 검색
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.springframework.stereotype.Service;

@Service
public class NaverAPIService {

	public static final String clientId = "TFUBwdm3MrMuN3_1TYil";

	public static final String clientSecret = "1C2ihcUVm1";

	public HashMap<String, Object> main(String temp1, String temp2, String temp3) {

		//		String first = temp1;
		//		String second = temp2;
		//		String apiName = null;
		//		String category = null;
		//		if ("찾아가기 ".equals(first)) {
		//			apiName = "map";
		//			category = "geocode";
		//		}
		//
		//		if ("책 ".equals(first)) {
		//			apiName = "search";
		//			category = "book";
		//		}	
		//
		//		if ("영화 ".equals(first)) {
		//			apiName = "search";
		//			category = "movie";
		//		}
		//
		//		if ("맛집 ".equals(first)) {
		//			apiName = "search";
		//			category = "local";
		//		}
		//		
		//		if ("백과사전 ".equals(first)) {
		//			apiName = "search";
		//			category = "encyc";
		//		}
		//
		//		if ("쇼핑 ".equals(first)) {
		//			apiName = "search";
		//			category = "shop";
		//		}
		//		
		//		if ("뉴스 ".equals(first)) {
		//			apiName = "search";
		//			category = "shop";
		//		}

		try {
			String text = URLEncoder.encode(temp3, "UTF-8");
			/**
			 * 뉴스 https://openapi.naver.com/v1/search/news.xml?query=%EC%A3%BC%EC%8B%9D&display=10&start=1&sort=sim
			 * 책 https://openapi.naver.com/v1/search/book.xml?query=%EC%A3%BC%EC%8B%9D&display=10&start=1
			 * 백과사전 https://openapi.naver.com/v1/search/encyc.xml?query=%EC%A3%BC%EC%8B%9D&display=10&start=1&sort=sim
			 * 영화 https://openapi.naver.com/v1/search/movie.xml?query=%EC%A3%BC%EC%8B%9D&display=10&start=1&genre=1
			 * 지역 https://openapi.naver.com/v1/search/local.xml?query=%EC%A3%BC%EC%8B%9D&display=10&start=1&sort=random
			 * 쇼핑 https://openapi.naver.com/v1/search/shop.xml?query=%EC%A3%BC%EC%8B%9D&display=10&start=1&sort=sim
			 *  String apiURL = "https://openapi.naver.com/v1/map/geocode?query=" + addr; //json
			 * 
			 */

			String apiURL = "https://openapi.naver.com/v1/" + temp1 + "/" + temp2 + "?query=" + text; // json 결과
			System.out.println(apiURL);
			//String apiURL = "https://openapi.naver.com/v1/search/blog.xml?query="+ text; // xml 결과
			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("X-Naver-Client-Id", clientId);
			con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
			int responseCode = con.getResponseCode();
			BufferedReader br;
			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;

			StringBuffer response = new StringBuffer();
			JSONParser parser = new JSONParser();

			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			Object obj = parser.parse(response.toString());
			JSONObject jsonObj = (JSONObject)obj;
			//			System.out.println(jsonObj);
			JSONArray items = (JSONArray)jsonObj.get("items");
			for (int i = 0; i < items.size(); i++) {
				System.out.println(items.get(i));
			}
			br.close();
			HashMap<String, Object> param = new HashMap<>();
			param.put("items", items);
			param.put("type", temp2);
			return param;
			//			System.out.println(response.toString());
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;

	}
}