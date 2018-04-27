package com.worksmobile.assignment.crawling;

import lombok.Getter;
import lombok.Setter;

/***
 * 파일 업로드 , 다운로드를 위한 모델입니다.
 * @author rws
 *
 */
public class Naver {
	//책
	@Setter @Getter private String image;
	@Setter @Getter private String author;
	@Setter @Getter private int price;
	@Setter @Getter private String isbn;
	@Setter @Getter private String link;
	@Setter @Getter private int discount;
	@Setter @Getter private String publisher;
	@Setter @Getter private String title;
	@Setter @Getter private String pubdate;	
	
	//영화 책이랑 조금 겹침
	@Setter @Getter private String actor;	
	@Setter @Getter private String director;	
	@Setter @Getter private String subtitle;
	@Setter @Getter private String userRating;	
	@Setter @Getter private String pubDate;	
	
	//지역
	@Setter @Getter private String address;	
	@Setter @Getter private String roadAddress;	
	@Setter @Getter private String link;	
	@Setter @Getter private String description;	
	@Setter @Getter private String telephone;	
	@Setter @Getter private String title;	
	@Setter @Getter private String category;	
	@Setter @Getter private String mapy;	
	@Setter @Getter private String mapx;	

}
