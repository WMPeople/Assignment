package com.worksmobile.assignment.util;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class JsoupUtils {
	public static long getCleanTotal(Document document, String selector) {
		Elements totalCountElements = document.select(selector);
		String dirtyTotal = totalCountElements.text();
		String cleanTotal = dirtyTotal.replaceAll("[^0-9]", "");
		long total = 0;
		if ("".equals(cleanTotal)) {
			total = 0;
		} else {
			total = Integer.parseInt(cleanTotal);
		}
		return total;
	}
	
	public static Document getDocument(String url) throws IOException {
		Document document = Jsoup.connect(url).userAgent(
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
			.get();
		return document;
	}

}
