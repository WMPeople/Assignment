package com.worksmobile.assignment.bo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

@Service
public class NaverAPIService {

	public static final String clientId = "TFUBwdm3MrMuN3_1TYil";

	public static final String clientSecret = "1C2ihcUVm1";

	public HashMap<String, Object> getSearchResult(String apiName, String category, String notEncodeText) {

		try {
			String text = URLEncoder.encode(notEncodeText, "UTF-8");

			String apiURL = "https://openapi.naver.com/v1/" + apiName + "/" + category + "?query=" + text; // json 결과
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
			System.out.println(jsonObj);
			JSONArray items;
			if ("map".equals(apiName)) {
				br.close();
				HashMap<String, Object> param = new HashMap<>();
				obj = parser.parse(jsonObj.get("result").toString());
				jsonObj = (JSONObject)obj;
				items = (JSONArray)jsonObj.get("items");
				System.out.println(items);
				param.put("items",items);
				param.put("type", category);
				return param;

			} else {
				items = (JSONArray)jsonObj.get("items");
				for (int i = 0; i < items.size(); i++) {
					System.out.println(items.get(i));
				}

				br.close();
				HashMap<String, Object> param = new HashMap<>();
				param.put("items", items);
				param.put("type", category);
				return param;

			}

			//			System.out.println(response.toString());
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;

	}
}