package com.worksmobile.assignment.model;

import lombok.Getter;
import lombok.Setter;

public class Kakao {
	
	//책
	@Setter @Getter private String title;
	@Setter @Getter private String contents;
	@Setter @Getter private String url;
	@Setter @Getter private String isbn;
	@Setter @Getter private String datetime;
	@Setter @Getter private String[] authors;
	@Setter @Getter private String publisher;
	@Setter @Getter private String[] translators;
	@Setter @Getter private int price;
	@Setter @Getter private int sale_price;
	@Setter @Getter private String category;
	@Setter @Getter private String thumbnail;
	@Setter @Getter private String status;
	
	//카페
	@Setter @Getter private String title;
	@Setter @Getter private String contents;
	@Setter @Getter private String url;
	@Setter @Getter private String cafename;
	@Setter @Getter private String thumbnail;
	@Setter @Getter private String datetime;
	
	//웹문서
	@Setter @Getter private String title;
	@Setter @Getter private String contents;
	@Setter @Getter private String url;
	@Setter @Getter private String datetime;
	
	
	

}
