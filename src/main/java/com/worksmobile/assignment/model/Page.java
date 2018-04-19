package com.worksmobile.assignment.model;

import lombok.Getter;
import lombok.Setter;
/***
 * 페이징을 위한 모델입니다.
 * @author rws
 *
 */
public class Page {
	@Getter@Setter private int maxPost;
	@Getter@Setter private int firstPageNo;
	@Getter@Setter private int prevPageNo;
	@Getter@Setter private int startPageNo;
	@Getter@Setter private int currentPageNo;
	@Getter@Setter private int endPageNo;
	@Getter@Setter private int nextPageNo;
	@Getter@Setter private int finalPageNo;
	@Getter@Setter private int numberOfRecords;
	@Getter@Setter private int sizeOfPage;
	public final static int CURRENT_PAGE_NO = 1;
	public final static int SIZE_OF_PAGE = 5;
	public final static int MAX_POST = 10;

	public Page() {
		this.currentPageNo = CURRENT_PAGE_NO;
		this.sizeOfPage = SIZE_OF_PAGE;
		this.maxPost = MAX_POST;
	}

}
