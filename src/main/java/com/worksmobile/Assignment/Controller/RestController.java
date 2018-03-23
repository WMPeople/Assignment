package com.worksmobile.Assignment.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Mapper.BoardMapper;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    @Autowired
    private BoardMapper boardMapper;
    
    //게시판 조회
    @RequestMapping(value = "/notApi/articles", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView boardList() throws Exception{
        List<BoardDTO> boardList = boardMapper.boardList();
        return new ModelAndView("boardList","list",boardList);
    }
    
	@RequestMapping(value = "/api/articles", method = RequestMethod.GET)
	@ResponseBody
	public List<BoardDTO> getArticles() throws Exception {
		return boardMapper.boardList();
	}
	
	@RequestMapping(value = "/api/articles/{id}", method = RequestMethod.GET)
	@ResponseBody
	public BoardDTO show(@PathVariable(value = "id") int id) {
		return boardMapper.getArticle(id);
	}
	
	@RequestMapping(value = "/api/articles", method = RequestMethod.POST)
	@ResponseBody
	public BoardDTO create(@RequestBody BoardDTO user) {
		return user;
	}
	
	@RequestMapping(value = "/api/articles/{id}", method = RequestMethod.PATCH)
	@ResponseBody
	public BoardDTO patch(@PathVariable(value = "id") String id, @RequestBody BoardDTO user) {
		return user;
	}
	
	@RequestMapping(value = "/api/articles/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public BoardDTO update(@PathVariable(value = "id") String id, @RequestBody BoardDTO user) {
		return user;
	}
	
	// TODO : make Service and input destroy.
	@RequestMapping(value = "/api/articles/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public List<BoardDTO> destroy(@PathVariable(value = "id") int id) throws Exception {
		int rtn = boardMapper.boardDelete(id);
		System.out.println(rtn);
		return boardMapper.boardList();
	}
}
