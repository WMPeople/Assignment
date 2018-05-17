package com.worksmobile.assignment.bo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.aop.framework.adapter.ThrowsAdviceInterceptor;

public class APIConnection {

	protected Object getObjectData(HttpURLConnection con) throws IOException, ParseException { 
		BufferedReader br = getInputStream(con);
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = br.readLine()) != null) {
			response.append(inputLine);
		}
		
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(response.toString());
		
		br.close();
		return obj;
	}

	private BufferedReader getInputStream(HttpURLConnection con) throws IOException {
		int responseCode = con.getResponseCode();
		if (responseCode != 200) {
			throw new RuntimeException("responseCod의 값이 200이 아닙니다.");
		} else {
			BufferedReader br;
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			return br;
		}
		
	}

}