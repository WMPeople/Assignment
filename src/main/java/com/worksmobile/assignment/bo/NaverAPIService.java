package com.worksmobile.assignment.bo;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class NaverAPIService extends APIConnection {

	public static final String clientId = "TFUBwdm3MrMuN3_1TYil";

	public static final String clientSecret = "1C2ihcUVm1";
	/***
	 * 네이버 검색 api를 이용하여 데이터를 가져온다.
	 * @param apiName ex) search
	 * @param category ex) book
	 * @param text ex) 어린왕자
	 * @return
	 */
	@Cacheable(value="findNaverAPICache", key="{#apiName, #category, #text, #startCnt}")
	public HashMap<String, Object> getSearchResult(String apiName, String category, String text, int startCnt) {

		try {
			String encodedText = URLEncoder.encode(text, "UTF-8");
			String apiURL = "https://openapi.naver.com/v1/" + apiName + "/" + category + "?start=" + startCnt +"&query=" + encodedText;
			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("X-Naver-Client-Id", clientId);
			con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
			JSONObject jsonObj = (JSONObject)getObjectData(con);
			JSONArray items = (JSONArray)jsonObj.get("items");
			
			HashMap<String, Object> param = new HashMap<>();
			param.put("items", items);
			param.put("type", category);
			param.put("total", jsonObj.get("total"));
			return param;
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;

	}
}