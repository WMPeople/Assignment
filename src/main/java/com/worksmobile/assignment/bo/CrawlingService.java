package com.worksmobile.assignment.bo;

import java.util.HashMap;

public interface CrawlingService {
	
	public HashMap<String, Object> getCrawlingResult(String category, String text, String pageNo) throws Exception;
	
	public HashMap<String, Object> getCrawlingResultNoCache(String category, String text, String pageNo) throws Exception;

}
