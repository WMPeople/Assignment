package com.worksmobile.Assignment.Mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.FileDTO;

@Mapper
public interface BoardMapper {

	public int boardCreate(BoardDTO board) ;

	public int boardDelete(HashMap<String, Integer> parmas) ;

	public int boardUpdate(HashMap<String, Integer> parmas) ;
	
	public BoardDTO viewDetail(HashMap<String, Integer> parmas) ;
	
	public FileDTO boardFileDownload(HashMap<String,Integer> params) ;

	public int getMaxBoardId();
	
	public int getMaxCookieId();
	
	public List<BoardDTO> articleList(HashMap<String, Integer> params);
	
	public int articleGetCount();
	
}
