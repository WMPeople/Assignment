package com.worksmobile.assignment.bo;

//네이버 검색 API 예제 - blog 검색
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class KakaoAPIService extends APIConnection{

	public static final String clientSecret = "f82a92ab82aed869e2ec2ab799c17958";
	
	@Cacheable(value="findKakaoAPICache", key="{#apiName, #category, #text, #pageNo}")
	public HashMap<String, Object> getSearchResult(String apiName, String category, String text, String pageNo) {

		try {
			String encodedText = URLEncoder.encode(text, "UTF-8");
			String apiURL = "https://dapi.kakao.com/v2/" + category + "/" + apiName + "/keyword.json?query=" + encodedText + "&page=" + pageNo; 
			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization","KakaoAK "+clientSecret);
			
			JSONObject jsonObj = (JSONObject)getObjectData(con);
			JSONArray documents = (JSONArray)jsonObj.get("documents");
			jsonObj = (JSONObject)jsonObj.get("meta");
			
			HashMap<String, Object> param = new HashMap<>();
			param.put("items", documents);
			param.put("type", category);
			param.put("total",jsonObj.get("pageable_count"));
			return param;
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;

	}

}