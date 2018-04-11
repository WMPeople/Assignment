package com.worksmobile.assignment.bo;

import lombok.Data;

@Data
public class Paging {
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
	public final static int MAX_POST = 10;

	public Paging(int currentPageNo, int maxPost) {
		this.currentPageNo = currentPageNo;
		this.sizeOfPage = 5;
		this.maxPost = (maxPost != 0) ? maxPost : 10;

	}

	public void makePaging() {
		if (currentPageNo == 0)
			setCurrentPageNo(1);

		if (numberOfRecords == 0)
			return;

		if (maxPost == 0)
			setMaxPost(10);

		int finalPage = (numberOfRecords + (maxPost - 1)) / maxPost;

		if (currentPageNo > finalPage)
			setCurrentPageNo(finalPage);

		if (currentPageNo < 0)
			currentPageNo = 1;

		boolean isNowFirst = currentPageNo == 1 ? true : false;
		boolean isNowFinal = currentPageNo == finalPage ? true : false;

		int startPage = ((currentPageNo - 1) / sizeOfPage) * sizeOfPage + 1;

		int endPage = startPage + sizeOfPage - 1;

		if (endPage > finalPage)
			endPage = finalPage;

		setFirstPageNo(1);

		if (!isNowFirst)
			setPrevPageNo(((startPage - 1) < 1 ? 1 : (startPage - 1)));

		setStartPageNo(startPage);
		setEndPageNo(endPage);

		if (!isNowFinal)
			setNextPageNo(((endPage + 1 > finalPage ? finalPage : (endPage + 1))));

		setFinalPageNo(finalPage);
	}
}