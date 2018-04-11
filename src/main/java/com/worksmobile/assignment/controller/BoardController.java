package com.worksmobile.assignment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.worksmobile.assignment.bo.Compress;
import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.model.NodePtr;

@Controller
public class BoardController {

	@Autowired
	private BoardHistoryMapper boardHistoryMapper;
	
	/***
	 * 게시물 작성입니다. 글쓰기 폼 페이지로 이동합니다.
	 */
	@RequestMapping(value = "/boards", method = RequestMethod.GET)
	public ModelAndView writeForm() throws Exception {
		return new ModelAndView("boardCreate");
	}
	
	/***
	 * 게시물 수정입니다. 글수정 폼 페이지로 이동합니다.
	 */
	@RequestMapping(value = "/boards/update", method = RequestMethod.POST)
	public ModelAndView updateForm() throws Exception {
		return new ModelAndView("boardUpdate");
	}
	
	/***
	 * 두 버전의 content를 비교할 때 호출되는 메쏘드 입니다.
	 * @param board_id1 첫번째 버전의 board_id
	 * @param version1 첫번째 버전의 version
	 * @param board_id2 두번째 버전의 board_id
	 * @param version2 두번째 버전의 version
	 * @return modelAndView 객체로 viewName과, content를 프론트에 전송합니다.
	 * @throws Exception
	 */
	@RequestMapping(value = "/boards/diff", method = RequestMethod.POST)
	public ModelAndView diff(int board_id1, 
			 int version1,
			 int board_id2, 
			 int version2 ) throws Exception {
		
		NodePtr left= new NodePtr(board_id1,version1);
		NodePtr right= new NodePtr(board_id2,version2);
		
		String leftContent = Compress.deCompress(boardHistoryMapper.selectHistory(left).getHistory_content());
		String rightContent = Compress.deCompress(boardHistoryMapper.selectHistory(right).getHistory_content());
		
		//압출 해결 후 리턴 , 맵으로 리턴
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("leftContent", leftContent);
		modelAndView.addObject("rightContent", rightContent);
		modelAndView.setViewName("diff");
		
		return modelAndView;
	
	}

}