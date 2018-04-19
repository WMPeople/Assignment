package com.worksmobile.assignment.util;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
	public static String jsonStringIfExceptionToString(Object object) {
		String json = "";
		try {
			json = JsonUtils.jsonStringFromObject(object);
		} catch (JsonProcessingException jsonErr) {
			jsonErr.printStackTrace();
			json = object.toString();
		}
		return json;
	}

	public static String jsonStringFromObject(Object object) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(object);
	}

	public static void assertConvertToJsonObject(Object expect, Object actual) throws JsonProcessingException {
		assertEquals(jsonStringFromObject(expect), jsonStringFromObject(actual));
	}
}
