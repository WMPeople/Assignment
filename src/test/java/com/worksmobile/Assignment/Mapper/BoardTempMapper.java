package com.worksmobile.Assignment.Mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.worksmobile.Assignment.Domain.BoardDTO;

@Mapper
public interface BoardTempMapper {
	public List<BoardDTO> boardList() ;

	public int boardCreate(BoardDTO board) ;

	public int boardDelete(Map<String, Integer> parmas) ;

	public int boardUpdate(BoardDTO board) ;
	
	public BoardDTO viewDetail(Map<String, Integer> parmas) ;
	
	public BoardDTO boardFileDownload(Map<String,Integer> params) ;
}
