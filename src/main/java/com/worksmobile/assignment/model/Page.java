package com.worksmobile.assignment.model;

import lombok.Data;
/***
 * 페이징을 위한 모델입니다.
 * @author rws
 *
 */
@Data
public class Page {
	private int maxPost;
	private int firstPageNo;
	private int prevPageNo;
	private int startPageNo;
	private int currentPageNo;
	private int endPageNo;
	private int nextPageNo;
	private int finalPageNo;
	private int numberOfRecords;
	private int sizeOfPage;
	public final static int CURRENT_PAGE_NO = 1;
	public final static int SIZE_OF_PAGE = 5;
	public final static int MAX_POST = 10;

	public Page() {
		this.currentPageNo = CURRENT_PAGE_NO;
		this.sizeOfPage = SIZE_OF_PAGE;
		this.maxPost = MAX_POST;
	}

}
