package com.worksmobile.assignment.crawling;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

public class NaverBrowserCrawlingTest {
	
	
    public String testBrowser() throws Exception {
        String url = "https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&query=%EC%B1%85+%EA%BD%83&oquery=%EC%B1%85+%EB%B0%94%EB%9E%8C&tqi=TXt6RspySEKsscnNjshssssssGd-492066";
        Document document = Jsoup.connect("https://www.naver.com").userAgent("Chrome").get();
       System.out.println("ㅁㅁㅁ");
        if (null != document) {
            // id가 realrank 인 ol 태그 아래 id가 lastrank인 li 태그를 제외한 모든 li 안에 존재하는
            // a 태그의 내용을 가져옵니다.
//            Elements elements = document.select("ul#type01 > li > dl > dt > a");
            Elements elements = document.select("ul#ah_l > li > a");
            for (int i = 0; i < elements.size(); i++) {
                System.out.println("------------------------------------------");
                System.out.println("주소 : " + elements.get(i).attr("href"));
//                System.out.println("랭킹 : " + (i + 1));
//                System.out.println("상승여부 : " + elements.get(i).select("span.tx").text());
//                System.out.println("상승단계 : " + elements.get(i).select("span.rk").text());
//                System.out.println("링크 URL : " + elements.get(i).attr("href"));
                System.out.println("------------------------------------------");
            }
        }
		return url;
    }
}