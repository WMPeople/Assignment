package com.worksmobile.assignment.bo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class BrowserCrawlingService {
	public String testBrowser() throws Exception {

		String url = "https://search.naver.com/search.naver?query=%EB%8F%84%EC%84%9C+%EC%96%B4%EB%A6%B0%EC%99%95%EC%9E%90";
		Document document = Jsoup.connect(url).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36").get();
		System.out.println("ㅁㅁㅁ");
		if (null != document) {
			// id가 realrank 인 ol 태그 아래 id가 lastrank인 li 태그를 제외한 모든 li 안에 존재하는
			// a 태그의 내용을 가져옵니다.
			//            Elements elements = document.select("ul#type01 > li > dl > dt > a");
			Elements elements = document.select("div.book_list.section > ul.type01 > li.sh_book_top");
			
			for (int i = 0; i < elements.size(); i++) {
				System.out.println("------------------------------------------");
				System.out.println("url : " + elements.get(i).select("dt > a").attr("href"));
				//                System.out.println("랭킹 : " + (i + 1));
				System.out.println("제목 : " + elements.get(i).select("dt > a > strong.hl ").text()); // .은 클래스를 찾는다. #은 id를 찾는다.
				System.out.println("작가 : " + elements.get(i).select("dd.author > span[id*=author] > span> a").text()); //id 값에 author가 있는것을 찾는다.
				//                System.out.println("상승단계 : " + elements.get(i).select("span.rk").text());
				//                System.out.println("링크 URL : " + elements.get(i).attr("href"));
				System.out.println("------------------------------------------");
			}
		}
		return url;
	}

}
