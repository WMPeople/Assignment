package com.worksmobile.Assignment.Mapper;

import java.util.HashMap;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.worksmobile.Assignment.Domain.BoardDTO;

@Mapper
public interface BoardMapper {
	public List<BoardDTO> boardList() ;

	public int boardCreate(BoardDTO board) ;

	public int boardDelete(HashMap<String, Integer> parmas) ;

	public int boardUpdate(BoardDTO board) ;
	
	public BoardDTO viewDetail(HashMap<String, Integer> parmas) ;
	
	public BoardDTO boardFileDownload(int board_id, int version, int branch) ;
	

}
