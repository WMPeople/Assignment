package com.worksmobile.Assignment.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Mapper.BoardMapper;

 
@RestController
public class BoardController {
    
    @Autowired
    private BoardMapper boardMapper;
    
    @RequestMapping("/")
    public ModelAndView  start() throws Exception {
    	return boardList();
    }
 
    @RequestMapping("/test")
    public ModelAndView  test() throws Exception {
    	return new ModelAndView("test", "test","test");
    }
    
    //게시판 조회
    @RequestMapping("/boardList")
    public ModelAndView boardList() throws Exception{
        List<BoardDTO> boardList = boardMapper.boardList();
        System.out.println("test");
        return new ModelAndView("boardList","list",boardList);
    }
    
    //게시판 생성
    @RequestMapping(value="/boardCreate",method=RequestMethod.GET)
    public ModelAndView writeForm() throws Exception{
        
        return new ModelAndView("userCreate");
    }

    @RequestMapping(path = "/boardCreate",method=RequestMethod.POST)
    public int boardCreate(BoardDTO board) throws Exception {
    	try {
    		//에러 핸들링 전용
//    		HashMap<String, Object> map = new HashMap<>();
//    		map.put("abcaa", "ddeeed");
//    		return map;	
    		System.out.println(board.getId());
    		return boardMapper.boardCreate(board);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("에러");
		}
    	return 0 ;
    }
    
    //게시판 삭제
    @RequestMapping("/boardDelete")
    public ModelAndView boardDelete(int id) throws Exception {
    	try {
    		System.out.println(id);
    		boardMapper.boardDelete(id);
		} catch (Exception e) {
			// TODO: handle exception
		}
    	// 에러 핸들링 예정
    	return boardList();
    }
    
    //게시판 수정
    @RequestMapping(value="/boardUpdate",method=RequestMethod.GET)
    public ModelAndView updateForm(String id) throws Exception{
        return new ModelAndView("boardUpdate","id",id);
    }
    
    @RequestMapping(path = "/boardUpdate",method=RequestMethod.POST)
    public ModelAndView boardUpdate(BoardDTO board) throws Exception {
    	try {
    		//에러 핸들링 전용
//    		HashMap<String, Object> map = new HashMap<>();
//    		map.put("abcaa", "ddeeed");
//    		return map;	
    		System.out.println(board.getId()+"업데이트 예정");
    		boardMapper.boardUpdate(board);
		} catch (Exception e) {
			System.out.println("error");
			// TODO: handle exception
		}
    	return boardList();
    }
    
}