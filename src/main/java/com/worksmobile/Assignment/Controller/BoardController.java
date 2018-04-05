package com.worksmobile.Assignment.Controller;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.NodePtrDTO;
import com.worksmobile.Assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.Assignment.Mapper.BoardMapper;
import com.worksmobile.Assignment.Mapper.FileMapper;
import com.worksmobile.Assignment.Service.Compress;
import com.worksmobile.Assignment.Service.VersionManagementService;

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
		
		NodePtrDTO left= new NodePtrDTO(board_id1,version1);
		NodePtrDTO right= new NodePtrDTO(board_id2,version2);
		
		String leftContent = Compress.deCompress(boardHistoryMapper.getHistory(left).getHistory_content());
		String rightContent = Compress.deCompress(boardHistoryMapper.getHistory(right).getHistory_content());
		
		//압출 해결 후 리턴 , 맵으로 리턴
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("leftContent", leftContent);
		modelAndView.addObject("rightContent", rightContent);
		modelAndView.setViewName("diff");
		
		return modelAndView;
	
	}

}