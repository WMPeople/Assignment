package com.worksmobile.assignment.bo;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.worksmobile.assignment.model.Page;

/***
 * 
 * @author rws
 *
 */
@Service
public class PageService {
	
	
	public Page getPage(HttpServletRequest req) {
	 	Page page = new Page();
	 	
		if (req.getParameter("pages") != null) {											 
			page.setCurrentPageNo(Integer.parseInt(req.getParameter("pages")));
		}
		return page; 
	}
	
	public Page makePaging(Page page) {
		int currentPageNo = page.getCurrentPageNo();
		int sizeOfPage = page.getSizeOfPage();
		int numberOfRecords = page.getNumberOfRecords();
		int maxPost = page.getMaxPost();
		if (currentPageNo == 0) {
			page.setCurrentPageNo(1);	
		}
			
		if (numberOfRecords == 0) {
			return null;
		}
			
		if (maxPost == 0) {
			page.setMaxPost(10);
		}
			
		int finalPage = (numberOfRecords + (maxPost - 1)) / maxPost;

		if (currentPageNo > finalPage) {
			page.setCurrentPageNo(finalPage);
		}
			

		if (currentPageNo < 0) {
			currentPageNo = 1;
		}
		boolean isNowFirst;
		boolean isNowFinal;
		
		if (currentPageNo == 1) {
			isNowFirst = true;
			
		} else {
			isNowFirst = false;
		}
		
		if (currentPageNo == finalPage) {
			isNowFinal = true;
			
		} else {
			isNowFinal = false;
		}	

		int startPage = ((currentPageNo - 1) / sizeOfPage) * sizeOfPage + 1;
		int endPage = startPage + sizeOfPage - 1;

		if (endPage > finalPage) {
			endPage = finalPage;
		}
			
		page.setFirstPageNo(1);

		if (!isNowFirst) {
			if ((startPage - 1) < 1) {
				page.setPrevPageNo(1);
				
			}else {
				page.setPrevPageNo((startPage - 1));
			}
		}
			
		page.setStartPageNo(startPage);
		page.setEndPageNo(endPage);

		if (!isNowFinal) {
			if (endPage + 1 > finalPage) {
				page.setNextPageNo(finalPage);
			}else {
				page.setNextPageNo(endPage + 1);
			}
			
		}
		page.setFinalPageNo(finalPage);
		return page;
	}

}
